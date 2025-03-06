# Workflow Directory

This directory contains workflow-related components and logic for the application.

## Purpose

The workflow directory is used to organize components and logic related to business processes and workflows in the application. This can include:

- State machines for multi-step processes
- Workflow diagrams and visualizations
- Process management components
- Step-by-step wizards
- Task management logic

## Structure

- `components/` - Reusable workflow-specific components
- `machines/` - State machines for workflow management
- `hooks/` - Custom hooks for workflow state management
- `utils/` - Utility functions for workflow operations

## Usage

Import workflow components and logic as needed in your views or other components:

```typescript
import { WorkflowStepper } from '@/workflow/components/WorkflowStepper.vue';
import { useWorkflowState } from '@/workflow/hooks/useWorkflowState';
``` 