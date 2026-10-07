<template>
  <div v-highlight class="md-view" v-html="marked(content)"></div>
</template>

<script setup lang="ts">
import { marked } from "marked";

defineProps({
  content: {
    type: String,
    required: true,
  },
});
</script>

<style scoped lang="less">
.md-view {
  font-size: 14px;
  line-height: 1.7;
  color: #2c3e50;

  :deep(h1), :deep(h2), :deep(h3) {
    margin: 16px 0 10px;
    font-weight: 600;
    color: #1f2d3d;
  }

  :deep(p) {
    margin: 8px 0;
  }

  :deep(pre) {
    background-color: #f6f8fa;
    padding: 14px 16px;
    border-radius: 10px;
    overflow-x: auto;
    font-size: 13px;
  }

  :deep(code) {
    font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
    background-color: #f0f2f5;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 13px;
    color: #d6336c;
  }

  :deep(pre code) {
    background: transparent;
    color: inherit;
    padding: 0;
  }

  :deep(ul), :deep(ol) {
    padding-left: 22px;
    margin: 8px 0;
  }

  :deep(blockquote) {
    margin: 10px 0;
    padding: 8px 14px;
    border-left: 3px solid #4a6cf7;
    background-color: #f0f4ff;
    border-radius: 0 8px 8px 0;
    color: #5a6474;
  }

  :deep(table) {
    border-collapse: collapse;
    width: 100%;
    margin: 12px 0;

    th, td {
      border: 1px solid #ebeef5;
      padding: 8px 12px;
      text-align: left;
    }

    th {
      background-color: #f7f8fc;
      font-weight: 600;
    }
  }
}
</style>