

/**
 * 变量类型枚举
 */
export enum VariableType {
  STRING = 'STRING',
  NUMBER = 'NUMBER',
  BOOLEAN = 'BOOLEAN',
  OBJECT = 'OBJECT',
  ARRAY = 'ARRAY'
}

/**
 * 变量定义接口
 */
export interface VariableDefinition {
  // definition
  name: string;
  type: VariableType;
  description: string;
  defaultValue?: any;
  // runtime
  value?: any;
  parent?: string;
}

/**
 * Represents a single condition with operands and operator
 */
export interface Condition {
  leftOperand: VariableDefinition;
  operator: string;
  rightOperand: VariableDefinition;
  type: 'VARIABLE' | 'CONSTANT';
}

/**
 * Represents a group of conditions with a logical operator type
 */
export interface ConditionCase {
  conditions: Condition[];
  type: 'and' | 'or';
  hint?: string;
}