/// <reference types="vite/client" />

/**
 * Represents a tool field definition
 */
export interface ToolField {
  name: string;
  description: string;
  type: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'ARRAY' | 'OBJECT' | 'DATE';
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
  inputFields: Record<string, ToolField>;
  outputFields: Record<string, ToolField>;
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

/**
 * Tool API Service
 * Handles all tool-related API calls
 */
export class ToolApi {
  /**
   * Base URL for tool API endpoints
   * Uses localhost:8080 in development and relative path in production
   */
  private baseUrl = import.meta.env.DEV 
    ? 'http://localhost:8080/api/tools'
    : '/api/tools';

  /**
   * List all available tools
   */
  async listTools(): Promise<{ name: string; description: string }[]> {
    const response = await fetch(this.baseUrl);
    if (!response.ok) {
      throw new Error(`Failed to list tools: ${response.statusText}`);
    }
    return await response.json();
  }

  /**
   * Get detailed information about a specific tool
   */
  async getToolDetails(toolName: string): Promise<Tool> {
    const response = await fetch(`${this.baseUrl}/${toolName}`);
    if (!response.ok) {
      throw new Error(`Failed to get tool details: ${response.statusText}`);
    }
    return await response.json();
  }

  /**
   * Execute a tool with the provided parameters，
   * 返回结果中，data为工具执行结果，message为工具执行日志，errorCode为工具执行错误码
   * 仅用于测试，工作流一般只在后端执行
   */
  async executeTool<T = any>(toolName: string, params: Record<string, any>): Promise<ToolExecutionResult<T>> {
    const response = await fetch(`${this.baseUrl}/${toolName}/execute`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(params)
    });

    if (!response.ok) {
      throw new Error(`Failed to execute tool: ${response.statusText}`);
    }

    return await response.json();
  }
}

// Export a singleton instance
export const toolApi = new ToolApi(); 