import type { RouteRecordRaw } from 'vue-router'

// Define routes
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  // 新建工作流路由 - 放在前面以优先匹配
  {
    path: '/workflow/editor/new',
    name: 'NewWorkflow',
    component: () => import('../views/WorkflowView.vue')
  },
  // 编辑工作流路由
  {
    path: '/workflow/editor/:id',
    name: 'WorkflowEditor',
    component: () => import('../views/WorkflowView.vue')
  }
]

export default routes 