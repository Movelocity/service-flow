<template>
  <button class="text-btn" @click="toggleTheme" title="切换主题">
    {{ theme === 'dark' ? '🌞' : '🌙' }}
  </button>
</template>

<script setup lang="ts">
import { useTheme } from '../hooks/useTheme';

const { theme, toggleTheme } = useTheme();
</script>