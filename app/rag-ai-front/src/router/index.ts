import { createRouter, createWebHistory } from "vue-router";
import routes from "./config";
import { ElMessage } from "element-plus";

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean;
    roles?: string[];
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 全局前置守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  const userRole = localStorage.getItem('userRole');

  if (to.meta.requiresAuth) {
    if (!token) {
      ElMessage({ message: '请先登录', type: 'warning' });
      next({ name: 'login' });
    } else if (
        to.meta.roles &&
        Array.isArray(to.meta.roles) &&
        userRole !== null &&
        !to.meta.roles.includes(userRole)
    ) {
      ElMessage({ message: '您没有权限访问该页面', type: 'error' });
      next({ name: 'index' });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;