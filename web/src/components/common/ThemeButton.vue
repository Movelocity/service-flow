<template>
  <button class="text-btn" style="font-size: 1.2em;" @click="toggleTheme" title="切换主题">
    {{ theme === 'dark' ? '🌞' : '🌙' }}
  </button>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme';

const { theme, toggleTheme } = useTheme();
</script>