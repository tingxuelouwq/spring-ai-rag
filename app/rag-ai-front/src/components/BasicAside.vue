<template>
  <div id="basic-aside">
    <el-menu
        :default-active="defaultPath"
        class="aside-menu"
        :collapse="isCollapse"
    >
      <div class="menu-header">
        <div class="logo-badge">R</div>
        <div class="logo-text">
          <h1>RAG-AI</h1>
          <span>知识库AI问答系统</span>
        </div>
      </div>

      <el-menu-item
          v-for="item in menuRouterList"
          :key="item.path"
          :index="item.path"
          @click="handleSelect(item)"
      >
        <el-icon>
          <component :is="item.meta?.icon"></component>
        </el-icon>
        <template #title>{{ item.meta?.description }}</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import routes from "@/router/config.ts";
import router from "@/router";

const emit = defineEmits(["changeAside"]);
const isCollapse = ref(false);
const path = router.currentRoute.value.fullPath;
const defaultPath = ref(path === "/" ? "/chat" : path);

const menuRouterList = computed(() => {
  return routes.filter((item) => item.meta?.isMenu);
});

router.afterEach((to) => {
  defaultPath.value = to.path;
});

const handleSelect = (e: any) => {
  router.push({ path: e.path });
};
</script>

<style scoped lang="less">
#basic-aside {
  height: 100%;
}

.aside-menu {
  height: 100%;
  border-right: none;
  border: none;
  background-color: transparent;
  padding: 8px 12px;
  box-sizing: border-box;

  // 菜单项
  :deep(.el-menu-item) {
    height: 46px;
    line-height: 46px;
    margin-bottom: 4px;
    border-radius: 10px;
    color: #5a6474;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.2s ease;

    .el-icon {
      color: #8a94a6;
      font-size: 18px;
      transition: color 0.2s ease;
    }

    &:hover {
      background-color: #f0f4ff;
      color: #4a6cf7;

      .el-icon {
        color: #4a6cf7;
      }
    }

    &.is-active {
      background: linear-gradient(135deg, #e8efff 0%, #f0e8ff 100%);
      color: #4a6cf7;
      font-weight: 600;

      .el-icon {
        color: #4a6cf7;
      }

      // 左侧小竖条
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 20px;
        border-radius: 0 3px 3px 0;
        background: linear-gradient(180deg, #4a6cf7 0%, #a855f7 100%);
      }
    }
  }
}

// Logo 区域
.menu-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 12px 16px;
  margin-bottom: 8px;

  .logo-badge {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 700;
    color: #ffffff;
    background: linear-gradient(135deg, #4a6cf7 0%, #a855f7 100%);
    box-shadow: 0 4px 10px rgba(74, 108, 247, 0.3);
    flex-shrink: 0;
  }

  .logo-text {
    display: flex;
    flex-direction: column;
    overflow: hidden;

    h1 {
      margin: 0;
      font-size: 17px;
      font-weight: 700;
      color: #1f2d3d;
      letter-spacing: 0.5px;
      line-height: 1.2;
    }

    span {
      margin-top: 2px;
      font-size: 11px;
      color: #8a94a6;
      white-space: nowrap;
    }
  }
}

// 折叠时隐藏文字
:deep(.el-menu--collapse) {
  .menu-header .logo-text {
    display: none;
  }

  .menu-header {
    justify-content: center;
    padding: 18px 0 16px;
  }
}
</style>