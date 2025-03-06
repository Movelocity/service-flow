# Hooks Directory

This directory contains reusable Vue composition API hooks for the application.

## Purpose

The hooks directory centralizes all reusable composition functions that can be shared across components. This includes:

- State management hooks
- Lifecycle hooks
- DOM manipulation hooks
- Utility hooks

## Structure

Hooks are organized by their functionality:

- `useForm.ts` - Form handling hooks
- `useLocalStorage.ts` - Local storage interaction hooks
- `useWindowSize.ts` - Window size and responsive hooks
- `useClickOutside.ts` - Click outside detection hooks
- etc.

## Usage

Import hooks as needed in your components:

```typescript
import { useForm } from '@/hooks/useForm';
import { useLocalStorage } from '@/hooks/useLocalStorage';

// In your component setup
const { formData, handleSubmit } = useForm();
const [theme, setTheme] = useLocalStorage('theme', 'light');
``` 