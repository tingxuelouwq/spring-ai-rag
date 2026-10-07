<template>
  <el-upload
      class="upload-demo"
      drag
      multiple
      v-model:file-list="fileList"
      :auto-upload="false"
      v-loading="isUploading"
  >
    <el-icon class="el-icon--upload"><upload-filled /></el-icon>
    <div class="el-upload__text">
      拖拽文件至此或<em>点击选择文件</em>进行上传
    </div>
    <template #tip>
      <div style="text-align: center">
        <el-text
        >文件支持 <i>pdf、doc、md、excel、text</i>等，最大可上传<em
            style="color: blue"
        >100MB</em
        ></el-text
        >
      </div>
    </template>
  </el-upload>

  <el-form
      style="display: flex; justify-content: space-between; align-items: center; margin: 20px 0"
      :model="queryFileDto"
  >
    <div style="display: flex; gap: 10px; align-items: center">
      <el-button
          type="danger"
          @click="batchDelete"
          :disabled="selectedFiles.length === 0"
      >
        批量删除
      </el-button>
      <el-button
          type="primary"
          @click="batchDownload"
          :disabled="selectedFiles.length === 0"
      >
        批量下载
      </el-button>
      <el-form-item label="文件名:" style="margin-bottom: 0">
        <el-input placeholder="请输入文件名称" v-model="queryFileDto.fileName" />
      </el-form-item>
      <el-form-item style="margin-bottom: 0">
        <el-button type="primary" @click="loadStoreFileData" :disabled="isLoading"
        >搜索</el-button
        >
      </el-form-item>
    </div>

    <div style="display: flex; gap: 10px; align-items: center">
      <span class="strategy-label">分片策略：</span>
      <el-select v-model="chunkStrategy" size="small" style="width: 140px;">
        <el-option label="按Token切分" value="token" />
        <el-option label="递归拆分" value="recursive" />
        <el-option label="基于文档结构" value="structure" />
        <el-option label="按段落切分" value="paragraph" />
        <el-option label="按句子切分" value="sentence" />
      </el-select>
      <el-button
          type="info"
          @click="handleChunkPreview"
          :disabled="!fileList?.length || isPreviewing"
          :loading="isPreviewing"
      >
        分片预览
      </el-button>
      <el-button
          type="warning"
          @click="uploadFile"
          :disabled="isUploading"
      >
        全部上传
      </el-button>
    </div>
  </el-form>

  <el-table
      :data="storeFileData"
      border
      v-loading="isLoading"
      height="calc(100vh - 400px)"
      @selection-change="handleSelectionChange"
  >
    <el-table-column type="selection" width="55" />
    <el-table-column label="序号" width="80">
      <template #default="scope">
        {{ (queryFileDto.page - 1) * queryFileDto.pageSize + scope.$index + 1 }}
      </template>
    </el-table-column>
    <el-table-column prop="fileName" label="文件名" width="580" />
    <el-table-column label="上传时间">
      <template #default="scope">
        {{ format(new Date(scope.row.createTime), "yyyy-MM-dd HH:mm") }}
      </template>
    </el-table-column>
    <el-table-column label="更新时间">
      <template #default="scope">
        {{ format(new Date(scope.row.updateTime), "yyyy-MM-dd HH:mm") }}
      </template>
    </el-table-column>
    <el-table-column label="操作" width="150" fixed="right">
      <template #default="scope">
        <el-button
            @click="deleteStoreFile(scope.row)"
            type="danger"
            size="small"
        >删除</el-button
        >
        <el-button
            @click="openFilePreview(scope.row)"
            type="primary"
            size="small"
        >下载</el-button
        >
      </template>
    </el-table-column>
  </el-table>

  <div style="margin-top: 20px; display: flex; justify-content: center;">
    <el-pagination
        v-model:current-page="queryFileDto.page"
        v-model:page-size="queryFileDto.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="storeFileTotal"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        layout="total, sizes, prev, pager, next, jumper"
    />
  </div>

  <!-- 分片预览对话框 -->
  <el-dialog
      v-model="previewVisible"
      title="分片预览"
      width="70%"
      :close-on-click-modal="false"
  >
    <div class="chunk-preview-header">
      <span>共 {{ chunkPreviewList.length }} 个分片</span>
      <span class="strategy-tag">策略：{{ getStrategyLabel(chunkStrategy) }}</span>
    </div>

    <div class="chunk-list">
      <div
          v-for="chunk in chunkPreviewList"
          :key="chunk.index"
          class="chunk-item"
      >
        <div class="chunk-header">
          <span class="chunk-index">#{{ chunk.index }}</span>
          <span class="chunk-length">{{ chunk.length }} 字符</span>
        </div>
        <div class="chunk-content">{{ chunk.content }}</div>
      </div>
    </div>

    <template #footer>
      <el-button @click="previewVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleConfirmUpload">确认并上传</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { type UploadUserFile, ElMessage, ElMessageBox } from "element-plus";
import { uploadFileApi, queryFileApi, deleteFileApi, downloadBatchApi, previewChunkApi } from "@/api/KnowHubApi";
import { StoreFile } from "@/api/data";
import { QueryFileDto } from "@/api/dto";
import { format } from "date-fns";

const storeFileData = ref<StoreFile[]>([]);
const queryFileDto = ref<QueryFileDto>({
  page: 1,
  pageSize: 10,
  fileName: "",
});
const isUploading = ref(false);
const isLoading = ref(false);
const storeFileTotal = ref(0);
const selectedFiles = ref<any[]>([])

// 分片策略与预览
const chunkStrategy = ref('token')
const previewVisible = ref(false)
const chunkPreviewList = ref<any[]>([])
const isPreviewing = ref(false)

// 加载文件列表
const loadStoreFileData = () => {
  isLoading.value = true;
  const params = { ...queryFileDto.value, page: queryFileDto.value.page - 1 }
  queryFileApi(params)
      .then((res) => {
        if (res.code == 0) {
          const data = res.data;
          storeFileTotal.value = data.totalElements;
          storeFileData.value = data.content;
        } else {
          ElMessage({
            type: "error",
            message: res.message,
          });
        }
      })
      .catch((err) => {
        ElMessage({
          type: "error",
          message: err,
        });
      })
      .finally(() => {
        isLoading.value = false;
      });
};

const fileList = ref<UploadUserFile[]>();

// 上传文件（带策略）
const uploadFile = () => {
  const files: File[] = [];
  fileList.value?.forEach((e) => {
    files.push(e.raw as File);
  });

  if (files.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  // 文件大小限制 100MB
  const maxSize = 100 * 1024 * 1024;
  for (const file of files) {
    if (file.size > maxSize) {
      ElMessage({
        type: "error",
        message: `文件 ${file.name} 超过了最大上传大小限制 (100MB)`,
      });
      return;
    }
  }

  isUploading.value = true;
  uploadFileApi(files, chunkStrategy.value)
      .then((res) => {
        if (res.code == 0) {
          ElMessage.success('文件上传成功')
          fileList.value = [];
          loadStoreFileData();
        } else {
          ElMessage.error(res.message || '上传失败')
        }
      })
      .catch((err) => {
        console.log(err);
        ElMessage.error('上传失败')
      })
      .finally(() => {
        isUploading.value = false;
      });
};

// 分片预览
const handleChunkPreview = async () => {
  if (!fileList.value?.length) {
    ElMessage.warning('请先选择文件')
    return
  }

  const file = fileList.value[0].raw as File
  if (!file) {
    ElMessage.warning('文件读取失败')
    return
  }

  isPreviewing.value = true
  try {
    const res = await previewChunkApi(file, chunkStrategy.value)
    if (res.code === 0) {
      chunkPreviewList.value = res.data || []
      previewVisible.value = true
    } else {
      ElMessage.error(res.message || '分片预览失败')
    }
  } catch (err) {
    console.error('分片预览失败:', err)
    ElMessage.error('分片预览失败')
  } finally {
    isPreviewing.value = false
  }
}

// 预览后确认上传
const handleConfirmUpload = () => {
  previewVisible.value = false
  uploadFile()
}

// 策略显示名称
const getStrategyLabel = (code: string) => {
  const map: Record<string, string> = {
    token: '按Token切分',
    paragraph: '按段落切分',
    sentence: '按句子切分',
    recursive: '递归拆分',
    structure: '基于文档结构'
  }
  return map[code] || code
}

const deleteStoreFile = (e: any) => {
  ElMessageBox.confirm("确定要删除这个知识库吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
      .then(() => {
        deleteFileApi({
          ids: e.id,
        })
            .then((res) => {
              if (res.code == 0) {
                ElMessage.success('删除成功')
                loadStoreFileData();
              } else {
                ElMessage.error(res.message || '删除失败')
              }
            })
            .catch((err) => {
              ElMessage.error('删除失败')
            });
      })
      .catch(() => {});
};

const openFilePreview = (row: any) => {
  const link = document.createElement('a')
  link.href = `/api/v1/knowledge/download/${row.id}`
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleSizeChange = (val: number) => {
  queryFileDto.value.pageSize = val
  loadStoreFileData()
}

const handleCurrentChange = (val: number) => {
  queryFileDto.value.page = val
  loadStoreFileData()
}

const handleSelectionChange = (selection: any[]) => {
  selectedFiles.value = selection
}

const batchDelete = () => {
  if (selectedFiles.value.length === 0) return

  ElMessageBox.confirm("确定要删除选中的文件吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
      .then(() => {
        const ids = selectedFiles.value.map(file => file.id).join(',')
        deleteFileApi({
          ids: ids,
        })
            .then((res) => {
              if (res.code == 0) {
                ElMessage.success('删除成功')
                selectedFiles.value = [];
                loadStoreFileData();
              } else {
                ElMessage.error(res.message || '删除失败')
              }
            })
            .catch((err) => {
              ElMessage.error('删除失败')
            });
      })
      .catch(() => {});
}

const batchDownload = async () => {
  if (selectedFiles.value.length === 0) return

  const ids = selectedFiles.value.map(file => file.id)
  try {
    const response = await downloadBatchApi(ids) as any
    const blob = new Blob([response], { type: 'application/zip' })
    const url = window.URL.createObjectURL(blob)

    const now = new Date()
    const timestamp = now.getFullYear().toString()
        + String(now.getMonth() + 1).padStart(2, '0')
        + String(now.getDate()).padStart(2, '0')
        + String(now.getHours()).padStart(2, '0')
        + String(now.getMinutes()).padStart(2, '0')
        + String(now.getSeconds()).padStart(2, '0')

    const link = document.createElement('a')
    link.href = url
    link.download = `知识库文件_${timestamp}.zip`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载已开始')
  } catch (err) {
    console.error('批量下载失败:', err)
    ElMessage.error('下载失败，请稍后重试')
  }
}

onMounted(() => {
  loadStoreFileData();
});
</script>

<style scoped lang="less">
.el-table {
  ::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  ::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 3px;
  }
  ::-webkit-scrollbar-track {
    background: #f5f5f5;
  }
}

.upload-demo {
  margin-bottom: 20px;
}

.el-form {
  background-color: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.strategy-label {
  color: #606266;
  font-size: 14px;
}

.chunk-preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 12px;
  background-color: #f7f8fc;
  border-radius: 8px;
  font-size: 13px;
  color: #5a6474;

  .strategy-tag {
    color: #4a6cf7;
    font-weight: 500;
  }
}

.chunk-list {
  max-height: 60vh;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: #dcdfe6;
    border-radius: 3px;
  }
}

.chunk-item {
  margin-bottom: 12px;
  padding: 12px 14px;
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  transition: all 0.2s;

  &:hover {
    border-color: #c6d4ff;
    box-shadow: 0 2px 8px rgba(74, 108, 247, 0.08);
  }

  .chunk-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    padding-bottom: 6px;
    border-bottom: 1px dashed #ebeef5;

    .chunk-index {
      font-size: 12px;
      font-weight: 600;
      color: #4a6cf7;
      background-color: #ecf0ff;
      padding: 2px 8px;
      border-radius: 4px;
    }

    .chunk-length {
      font-size: 12px;
      color: #909399;
    }
  }

  .chunk-content {
    font-size: 13px;
    line-height: 1.6;
    color: #2c3e50;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 200px;
    overflow-y: auto;
  }
}
</style>