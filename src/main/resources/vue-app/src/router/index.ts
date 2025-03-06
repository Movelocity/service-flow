import { createRouter, createWebHistory } from 'vue-router';
import WorkflowEditor from '../views/WorkflowEditor.vue';
import { useWorkflowStore } from '../stores/workflow';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/workflows'
    },
    {
      path: '/workflows',
      name: 'workflows',
      component: () => import('../views/WorkflowList.vue')
    },
    {
      path: '/workflows/:id',
      name: 'workflow-editor',
      component: WorkflowEditor
    }
  ]
});

// 添加全局导航守卫
router.beforeEach((to, from, next) => {
  // 只在离开工作流编辑器页面时检查
  if (from.name === 'workflow-editor' && to.name !== 'workflow-editor') {
    // 获取编辑器组件实例
    const workflowEditor = from.matched[0].components?.default as typeof WorkflowEditor;
    if (workflowEditor?.__v_isVNode) {
      const isDirty = (workflowEditor.component?.exposed as any)?.isDirty?.value;
      if (isDirty && !window.confirm('有未保存的更改，确定要离开吗？')) {
        next(false);
        return;
      }
    }
  }
  next();
});

export default router; 