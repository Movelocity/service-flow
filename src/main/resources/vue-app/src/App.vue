<template>
  <router-view />
</template>

<script setup lang="ts">
// App component is just a router view container
</script>

<style>
@import './assets/workflow.css';

:root {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.5;
  font-weight: 400;
}

body {
  margin: 0;
  min-width: 320px;
  min-height: 100vh;
  background-color: #f8f9fa;
}

#app {
  height: 100vh;
}

* {
  box-sizing: border-box;
}

/* 基础按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  font-weight: 400;
  line-height: 1.5;
  text-align: center;
  white-space: nowrap;
  vertical-align: middle;
  cursor: pointer;
  user-select: none;
  border: 1px solid transparent;
  border-radius: 0.25rem;
  transition: color 0.15s ease-in-out,
              background-color 0.15s ease-in-out,
              border-color 0.15s ease-in-out,
              box-shadow 0.15s ease-in-out;
}

.btn:disabled {
  opacity: 0.65;
  pointer-events: none;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
  border-radius: 0.2rem;
}

.btn-primary {
  color: #fff;
  background-color: var(--node-selected);
  border-color: var(--node-selected);
}

.btn-primary:hover {
  background-color: #0056b3;
  border-color: #0056b3;
}

.btn-secondary {
  color: #fff;
  background-color: #6c757d;
  border-color: #6c757d;
}

.btn-secondary:hover {
  background-color: #5a6268;
  border-color: #5a6268;
}

.btn-danger {
  color: #fff;
  background-color: var(--node-end);
  border-color: var(--node-end);
}

.btn-danger:hover {
  background-color: #c82333;
  border-color: #c82333;
}

.btn-outline-secondary {
  color: #6c757d;
  border-color: #6c757d;
  background-color: transparent;
}

.btn-outline-secondary:hover {
  color: #fff;
  background-color: #6c757d;
}

.btn-outline-danger {
  color: var(--node-end);
  border-color: var(--node-end);
  background-color: transparent;
}

.btn-outline-danger:hover {
  color: #fff;
  background-color: var(--node-end);
}

.btn-link {
  font-weight: 400;
  color: var(--node-selected);
  text-decoration: none;
  background-color: transparent;
  border: none;
}

.btn-link:hover {
  color: #0056b3;
  text-decoration: underline;
}

/* 表单控件样式 */
.form-control {
  display: block;
  width: 100%;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  font-weight: 400;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  border-radius: 0.25rem;
  transition: border-color 0.15s ease-in-out,
              box-shadow 0.15s ease-in-out;
}

.form-control:focus {
  color: #495057;
  background-color: #fff;
  border-color: var(--node-selected);
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.form-control-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
  line-height: 1.5;
  border-radius: 0.2rem;
}

/* 工具类 */
.me-2 {
  margin-right: 0.5rem;
}

.ms-2 {
  margin-left: 0.5rem;
}

.text-muted {
  color: #6c757d;
}
</style> 