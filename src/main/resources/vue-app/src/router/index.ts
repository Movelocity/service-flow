import { createRouter, createWebHistory } from 'vue-router';
import WorkflowEditor from '../views/WorkflowEditor.vue';

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

export default router; 