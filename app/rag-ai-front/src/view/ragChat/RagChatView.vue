<template>
  <div class="chat-container">
    <el-card class="box-card">
      <div class="chat-messages" ref="messageContainer">
        <div v-for="(message, index) in messages" :key="index"
             :class="['message', message.role === 'user' ? 'user-message' : 'assistant-message']">
          <div class="message-wrapper">
            <div
                class="message-content"
                :class="{ 'typing': message.isTyping }"
                v-html="renderMarkdown(message.content, index)"
            >
            </div>

            <el-button
                class="copy-button"
                type="text"
                size="small"
                @click="copyMessage(message.content)"
            >
              <el-icon><Document /></el-icon>
            </el-button>
          </div>

          <!-- 引用文档 -->
          <div v-if="message.sources && message.sources.length > 0" class="message-sources">
            <span class="sources-label">引用文档：</span>
            <div
                v-for="(source, idx) in message.sources"
                :key="idx"
                class="source-item"
                @click="handleSourceClick(source)"
            >
              <span class="source-index">[{{ idx + 1 }}]</span>
              <span class="source-name">{{ source }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="input-container">
        <el-input
            v-model="userInput"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题..."
            @keyup.enter="handleRagSend"
        />
      </div>

      <div class="button-group">
        <!-- 模型选择 -->
        <div class="model-selection-inline">
          <span class="model-selection-label">选择模型：</span>
          <el-select v-model="selectedModel" size="small" style="width: 180px;">
            <el-option label="Qwen 3.8 Max" value="qwenChatModel" />
            <el-option label="DeepSeek V4.1 Flash" value="deepseekChatModel" />
            <el-option label="GLM 5.3" value="glmChatModel" />
            <el-option label="Kimi K3" value="kimiChatModel" />
          </el-select>
        </div>

        <div class="file-selection-inline">
          <span class="file-selection-label">选择知识库文件：</span>
          <el-select
              v-model="selectedFiles"
              multiple
              collapse-tags
              collapse-tags-tooltip
              placeholder="选择文件"
              style="width: 200px; margin-right: 10px;"
              size="small"
              @change="handleFileSelectionChange"
          >
            <el-option
                v-for="file in knowledgeFiles"
                :key="file.id"
                :label="file.fileName"
                :value="file.id"
            />
          </el-select>
          <el-popover
              v-if="selectedFiles.length > 0"
              trigger="hover"
              placement="bottom"
              :width="300"
          >
            <template #reference>
              <el-tag
                  type="info"
                  size="small"
                  style="margin-right: 10px; cursor: pointer;"
              >
                已选{{ selectedFiles.length }}个
              </el-tag>
            </template>
            <div class="selected-files-popover">
              <div v-for="fileId in selectedFiles" :key="fileId" style="margin-bottom: 5px;">
                <el-tag
                    :title="getFileNameById(fileId)"
                    size="small"
                    closable
                    @close="removeSelectedFile(fileId)"
                    style="margin-right: 5px; margin-bottom: 5px;"
                >
                  {{ getFileNameByLength(fileId, 15) }}
                </el-tag>
              </div>
            </div>
          </el-popover>
        </div>
        <el-button type="primary" @click="handleRagSend" :loading="isLoading">RAG回答</el-button>
        <el-button type="warning" @click="clearMessages">清空对话</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { marked } from 'marked'
import { Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { ChatApi, type ChatMessage } from '@/api/ChatApi'
import { getStreamChat } from '@/api/StreamApi'
import { queryFileApi } from '@/api/KnowHubApi'
import { StoreFile } from '@/api/data'

const messages = ref<ChatMessage[]>([])
const userInput = ref('')
const isLoading = ref(false)
const messageContainer = ref<HTMLElement | null>(null)
const knowledgeFiles = ref<StoreFile[]>([])
const selectedFiles = ref<string[]>([])
const selectedModel = ref('qwen3.8-max-0902')

const handleSourceClick = (fileName: string) => {
  const file = knowledgeFiles.value.find(f => f.fileName === fileName)
  if (file) {
    window.open(`/api/v1/knowledge/preview/${file.id}`, '_blank')
  }
}

// 加载知识库文件列表
const loadKnowledgeFiles = () => {
  const params = {
    page: 0,
    pageSize: 100,
    fileName: ""
  }

  queryFileApi(params)
      .then((res) => {
        if (res.code == 0) {
          const data = res.data;
          knowledgeFiles.value = data.content || [];
        } else {
          ElMessage({
            type: 'error',
            message: res.message,
          });
        }
      })
      .catch((err) => {
        ElMessage({
          type: 'error',
          message: err,
        });
      });
};

// 从回答内容中提取引用来源
const extractSources = (content: string): string[] => {
  const regex = /\[来源[:：]\s*([^\]]+)\]/g
  const sources: string[] = []
  let match
  while ((match = regex.exec(content)) !== null) {
    const source = match[1].trim()
    if (source && !sources.includes(source)) {
      sources.push(source)
    }
  }
  return sources
}

// 处理普通对话
const handleSend = async () => {
  if (!userInput.value.trim() || isLoading.value) return
  await sendMessage(ChatApi.Chat)
}

// 处理RAG对话
const handleRagSend = async () => {
  if (!userInput.value.trim() || isLoading.value) return
  await sendMessage(ChatApi.RagChat, selectedFiles.value, selectedModel.value)
}

// 发送消息通用方法
const sendMessage = async (url: string, selectedFileIds: string[] = [], model: string = '') => {
  messages.value.push({
    role: 'user',
    content: userInput.value
  })

  const currentInput = userInput.value
  userInput.value = ''
  isLoading.value = true

  messages.value.push({
    role: 'assistant',
    content: '正在思考中...',
    isTyping: true
  })

  const lastIndex = messages.value.length - 1
  const reactiveMessage = messages.value[lastIndex]

  let isFirstChunk = true;

  // 获取选中的文件名
  const fileSources = selectedFileIds.map(id => {
    const file = knowledgeFiles.value.find(f => f.id === id)
    return file ? file.fileName : ''
  }).filter(name => name !== '')

  // 完成回调
  const onComplete = () => {
    isLoading.value = false
    reactiveMessage.isTyping = false
    reactiveMessage.sources = extractSources(reactiveMessage.content)
  }

  // 错误回调
  const onError = (error: any) => {
    console.error('Error:', error)
    reactiveMessage.content = '抱歉，发生了错误，请稍后重试。'
    isLoading.value = false
    reactiveMessage.isTyping = false
  }

  // 数据回调
  const onMessage = (value: any) => {
    const text = value.data;
    if (isFirstChunk && reactiveMessage.content === '正在思考中...') {
      reactiveMessage.content = '';
      isFirstChunk = false;
    }
    reactiveMessage.content += text
    scrollToBottom()
  }

  if (fileSources.length > 0) {
    getStreamChat(currentInput, url, onMessage, onError, onComplete, fileSources, model)
  } else {
    getStreamChat(currentInput, url, onMessage, onError, onComplete, undefined, model)
  }
};

// 滚动到底部
const scrollToBottom = () => {
  if (!messageContainer.value) return
  const container = messageContainer.value
  container.scrollTop = container.scrollHeight
  setTimeout(() => {
    container.scrollTop = container.scrollHeight
  }, 100)
}

// 复制消息
const copyMessage = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage({
      message: '复制成功',
      type: 'success',
      duration: 2000
    })
  } catch (err) {
    ElMessage({
      message: '复制失败',
      type: 'error',
      duration: 2000
    })
  }
}

// 清空对话
const clearMessages = () => {
  messages.value = [{
    role: 'assistant',
    content: '你好！我是AI助手，请问有什么可以帮助你的吗？'
  }]
}

// 存储当前消息的引用映射
const sourceIndexMap = ref<Record<string, number>>({})

const renderMarkdown = (content: string, messageIndex: number) => {
  try {
    const sources: string[] = []
    const sourceMap: Record<string, number> = {}
    const regex = /\[来源[:：]\s*([^\]]+)\]/g
    let match
    while ((match = regex.exec(content)) !== null) {
      const source = match[1].trim()
      if (!sourceMap[source]) {
        sources.push(source)
        sourceMap[source] = sources.length
      }
    }

    let html = content.replace(regex, (_, source) => {
      const idx = sourceMap[source.trim()]
      return `<sup class="source-ref" data-source="${source.trim()}" title="来源: ${source.trim()}">[${idx}]</sup>`
    })

    html = marked(html, { breaks: true, gfm: true })

    if (sources.length > 0) {
      sourceIndexMap.value[messageIndex] = sourceMap as any
    }

    return html
  } catch (error) {
    console.error('Markdown parsing error:', error)
    return content
  }
}

// 处理文件选择变化
const handleFileSelectionChange = (value: string[]) => {
  selectedFiles.value = value
}

// 移除选中的文件
const removeSelectedFile = (fileId: string) => {
  const index = selectedFiles.value.indexOf(fileId)
  if (index > -1) {
    selectedFiles.value.splice(index, 1)
  }
}

// 根据文件ID获取文件名
const getFileNameById = (fileId: string) => {
  const file = knowledgeFiles.value.find(f => f.id === fileId)
  return file ? file.fileName : ''
}

// 根据文件ID获取截断的文件名
const getFileNameByLength = (fileId: string, maxLength: number) => {
  const fileName = getFileNameById(fileId)
  if (fileName.length <= maxLength) {
    return fileName
  }
  return fileName.substring(0, maxLength) + '...'
}

onMounted(() => {
  messages.value.push({
    role: 'assistant',
    content: '你好！我是AI助手，请问有什么可以帮助你的吗？'
  })
  loadKnowledgeFiles()
})
</script>

<style scoped lang="less">
.chat-container {
  height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;

  .box-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 20px;
      overflow: hidden;
    }
  }
}

.model-selection-inline {
  display: flex;
  align-items: center;
  padding: 5px;
  flex-wrap: nowrap;

  .model-selection-label {
    margin-right: 10px;
    font-weight: 500;
    color: #606266;
    font-size: 14px;
  }
}

.file-selection-inline {
  display: flex;
  align-items: center;
  padding: 5px;
  flex-wrap: nowrap;
}

.file-selection-inline .file-selection-label {
  margin-right: 10px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

.selected-files-popover {
  max-height: 200px;
  overflow-y: auto;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 0;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: #909399;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-track {
    background-color: #f0f2f5;
  }
}

.message {
  margin-bottom: 15px;
  max-width: 80%;

  &.user-message {
    margin-left: auto;
    text-align: right;

    .message-wrapper {
      flex-direction: row-reverse;
      justify-content: flex-start;
    }

    .message-content {
      background-color: #007AFF;
      color: white;
    }
  }

  &.assistant-message {
    margin-right: auto;
    text-align: left;
  }
}

.message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.message-content {
  display: inline-block;
  padding: 10px 15px;
  border-radius: 10px;
  background-color: #f0f0f0;
  word-break: break-word;
  font-size: 14px;

  :deep(p) {
    margin: 0;
    line-height: 1.5;
  }

  :deep(pre) {
    background-color: #f8f8f8;
    padding: 10px;
    border-radius: 4px;
    overflow-x: auto;
    font-size: 13px;
  }

  :deep(code) {
    font-family: Consolas, Monaco, 'Andale Mono', monospace;
    background-color: #f8f8f8;
    padding: 2px 4px;
    border-radius: 3px;
    font-size: 13px;
  }

  :deep(ul), :deep(ol) {
    padding-left: 20px;
    margin: 8px 0;
  }

  :deep(blockquote) {
    margin: 8px 0;
    padding-left: 10px;
    border-left: 4px solid #ddd;
    color: #666;
  }

  &.typing {
    &::after {
      content: '...';
      animation: ellipsis 1.5s infinite;
    }
  }
}

@keyframes ellipsis {
  0% { content: '.'; }
  33% { content: '..'; }
  66% { content: '...'; }
  100% { content: '.'; }
}

/* 正文里的角标 */
:deep(.source-ref) {
  display: inline-block;
  color: #409EFF;
  font-size: 10px;
  font-weight: bold;
  background-color: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 3px;
  padding: 0 3px;
  margin: 0 2px;
  cursor: pointer;
  vertical-align: super;
  line-height: 1.2;
  transition: all 0.2s;

  &:hover {
    background-color: #409EFF;
    color: white;
  }
}

/* 底部图例 */
.message-sources {
  margin-top: 8px;
  padding: 8px 10px;
  background-color: #f8f9fa;
  border-left: 3px solid #409EFF;
  border-radius: 4px;
  font-size: 12px;

  .sources-label {
    color: #909399;
    margin-right: 8px;
  }

  .source-item {
    display: inline-flex;
    align-items: center;
    margin-right: 10px;
    margin-bottom: 5px;
    cursor: pointer;

    &:hover .source-name {
      color: #409EFF;
      text-decoration: underline;
    }
  }

  .source-index {
    color: #409EFF;
    font-weight: bold;
    margin-right: 4px;
  }

  .source-name {
    color: #606266;
  }
}

.copy-button {
  opacity: 0;
  transition: opacity 0.3s;
  padding: 4px;
  height: auto;

  &:hover {
    opacity: 1;
  }
}

.input-container {
  margin-top: auto;
  display: flex;
  gap: 10px;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 100px;

  .el-textarea {
    flex: 1;
  }
}

.button-group {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  padding: 10px 0;
  flex-wrap: wrap;

  .el-button {
    width: 120px;
    height: 40px;
    font-size: 14px;
  }
}
</style>