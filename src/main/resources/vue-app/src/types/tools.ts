import type { VariableType } from '@/types/fields';

/**
 * Represents a tool field definition
 */
export interface ToolField {
  name: string;
  description: string;
  type: VariableType;
  required: boolean;
  defaultValue?: any;
  constraints?: any;
}

/**
 * Represents a complete tool definition
 */
export interface Tool {
  name: string;
  description: string;
  inputs: Record<string, ToolField>;
  outputs: Record<string, ToolField>;
}

/**
 * Represents the execution result of a tool
 */
export interface ToolExecutionResult<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  errorCode?: string;
}