<template>
  <div id="basic-layout">
    <el-container>
      <el-aside :style="asideStyle" class="layout-aside">
        <BasicAside @change-aside="changeAside" />
      </el-aside>
      <el-main class="layout-main">
        <RouterView />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const asideStyle = ref({
  height: "95vh",
  width: "220px",
})

const changeAside = (isCollapse: boolean) => {
  asideStyle.value.width = isCollapse ? "64px" : "220px"
}
</script>

<style scoped lang="less">
#basic-layout {
  margin: 12px;

  .layout-aside {
    background-color: #ffffff;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(31, 45, 61, 0.06);
    transition: width 0.3s ease;
    overflow: hidden;
  }

  .layout-main {
    margin-left: 12px;
    background-color: #f7f8fc;
    border-radius: 16px;
    padding: 20px;
    box-sizing: border-box;
    overflow: hidden;
  }
}
</style>