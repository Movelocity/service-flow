import type { RouteRecordRaw } from 'vue-router'

// Define routes
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/workflow/editor/:id',
    name: 'WorkflowView',
    component: () => import('../views/WorkflowView.vue'),
    props: (route) => ({
      id: route.params.id,
      isNew: route.query.new === 'true'
    })
  }
]

export default routes 