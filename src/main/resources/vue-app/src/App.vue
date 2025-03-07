<template>
  <router-view />
</template>

<script setup lang="ts">
import { useTheme } from '@/hooks/useTheme';

// 初始化主题
useTheme();
</script>

<style>
@import './assets/index.css';

:root {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.5;
  font-weight: 400;
}

body {
  margin: 0;
  min-width: 320px;
  min-height: 100vh;
  background-color: var(--background-color);
  color: var(--text-color);
  transition: background-color 0.3s, color 0.3s;
}

#app {
  height: 100vh;
}

* {
  box-sizing: border-box;
}
</style>