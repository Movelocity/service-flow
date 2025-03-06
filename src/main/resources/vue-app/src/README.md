# Vue Application Structure

This document outlines the recommended structure for the Vue application.

## Directory Structure

```
src/
├── api/              # API related code (client, endpoints, types)
├── assets/           # Static assets (images, fonts, etc.)
├── components/       # Reusable UI components
├── hooks/            # Custom composition API hooks
├── pinia/            # Pinia store modules
├── router/           # Vue Router configuration and routes
├── style/            # CSS styles and variables
│   ├── variables.css # CSS variables (colors, spacing, etc.)
│   ├── reset.css     # CSS reset and base styles
│   ├── utility.css   # Utility classes
│   ├── components.css# Component-specific styles
│   └── index.css     # Main style entry point
├── views/            # Page components
├── workflow/         # Business process and workflow components
├── App.vue           # Root component
└── main.ts           # Application entry point
```

## Style Organization

The styles are organized in a modular way to promote consistency and maintainability:

1. **variables.css**: Contains all CSS variables including the color palette, spacing, typography, etc.
2. **reset.css**: Normalizes styles across browsers and sets base styles.
3. **utility.css**: Provides utility classes for common styling needs.
4. **components.css**: Contains styles for common UI components.
5. **index.css**: Imports all style files in the correct order.

## Color Palette

The application uses a predefined color palette to ensure consistency:

- Primary: #3498db (Blue)
- Secondary: #2ecc71 (Green)
- Accent: #e74c3c (Red)
- Neutral shades: From #f8f9fa (lightest) to #212529 (darkest)
- Semantic colors: Success, Info, Warning, Danger

## Best Practices

1. **Component Organization**: Keep components small and focused on a single responsibility.
2. **API Calls**: Use the API client for all external API calls.
3. **State Management**: Use Pinia for global state management.
4. **Styling**: Use the predefined CSS variables and utility classes.
5. **TypeScript**: Use TypeScript interfaces and types for better type safety.
6. **Composition API**: Use the Composition API with `<script setup>` for components.
7. **Workflow Components**: Keep business process logic in the workflow directory. 