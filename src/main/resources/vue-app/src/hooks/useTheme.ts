import { ref, onMounted } from 'vue';

export type Theme = 'light' | 'dark';

export function useTheme() {
  const theme = ref<Theme>('light');

  // 检测系统主题
  const getSystemTheme = (): Theme => {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return 'dark';
    }
    return 'light';
  };

  // 应用主题
  const applyTheme = (newTheme: Theme) => {
    document.documentElement.setAttribute('class', newTheme);
    theme.value = newTheme;
    localStorage.setItem('theme', newTheme);
  };

  // 切换主题
  const toggleTheme = () => {
    const newTheme = theme.value === 'light' ? 'dark' : 'light';
    applyTheme(newTheme);
  };

  // 监听系统主题变化
  const watchSystemTheme = () => {
    if (window.matchMedia) {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
      mediaQuery.addEventListener('change', (e) => {
        if (!localStorage.getItem('theme')) {
          // 只有当用户没有手动设置主题时，才跟随系统主题
          applyTheme(e.matches ? 'dark' : 'light');
        }
      });
    }
  };

  // 初始化主题
  onMounted(() => {
    const savedTheme = localStorage.getItem('theme') as Theme | null;
    if (savedTheme) {
      applyTheme(savedTheme);
    } else {
      applyTheme(getSystemTheme());
    }
    watchSystemTheme();
  });

  return {
    theme,
    toggleTheme,
    applyTheme
  };
} 