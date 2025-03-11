/**
 * Represents a single condition with operands and operator
 */
export interface Condition {
  leftOperand: string;
  operator: string;
  rightOperand: string;
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