import { ref, watch } from 'vue';

/**
 * Hook for using localStorage with Vue reactivity
 * 
 * @param key - The localStorage key to store the value under
 * @param initialValue - The initial value to use if no value is found in localStorage
 * @returns A tuple containing the reactive value and a setter function
 */
export function useLocalStorage<T>(key: string, initialValue: T) {
  // Create a reactive reference
  const storedValue = ref<T>(initialValue);
  
  // Initialize from localStorage if available
  try {
    const item = window.localStorage.getItem(key);
    if (item) {
      storedValue.value = JSON.parse(item);
    }
  } catch (error) {
    console.error(`Error reading localStorage key "${key}":`, error);
  }

  // Return a wrapped version of useState's setter function that persists the new value to localStorage
  const setValue = (value: T) => {
    try {
      // Save state
      storedValue.value = value;
      // Save to local storage
      window.localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error(`Error setting localStorage key "${key}":`, error);
    }
  };

  // Watch for changes to the reactive reference and update localStorage
  watch(storedValue, (newValue) => {
    window.localStorage.setItem(key, JSON.stringify(newValue));
  }, { deep: true });

  return [storedValue, setValue] as const;
} 