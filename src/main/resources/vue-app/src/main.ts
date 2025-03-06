import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
import App from './App.vue'
import routes from './router'

// Import Bootstrap and Bootstrap Icons
// import 'bootstrap/dist/css/bootstrap.min.css'
// import 'bootstrap-vue-next/dist/bootstrap-vue-next.css'
// import 'bootstrap-icons/font/bootstrap-icons.css'
// import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Import global styles
import './style.css'

// Create the router instance
const router = createRouter({
  history: createWebHistory(),
  routes
})

// Create the Pinia store
const pinia = createPinia()

// Create and mount the app
const app = createApp(App)
app.use(router)
app.use(pinia)
app.mount('#app')
