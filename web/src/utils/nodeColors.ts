import { NodeType } from '@/types/workflow';

/**
 * Get the color for a specific node type
 * @param nodeType The type of node
 * @returns The color for the node type
 */
export function getNodeColor(nodeType: NodeType): string {
  switch (nodeType) {
    case NodeType.START:
      return '#4CAF50';
    case NodeType.FUNCTION:
      return '#2196F3';
    case NodeType.CONDITION:
      return '#FF9800';
    case NodeType.END:
      return '#F44336';
    default:
      return '#757575';
  }
}

/**
 * Get the color for connection labels
 * @param nodeType The type of the source node
 * @param condition The connection condition
 * @returns The color for the connection label
 */
export function getLabelColor(nodeType: NodeType, _condition: string): string {
  if (nodeType === NodeType.CONDITION) {
    return '#FF9800'; // Orange for condition node labels
  }
  return '#666666'; // Default gray for other nodes
}

/**
 * Get the display text for a connection condition
 * @param condition The connection condition
 * @returns The display text for the condition
 */
export function getDisplayLabel(condition: string): string {
  if (condition.startsWith('case')) {
    return `Case ${condition.slice(4)}`;
  } else if (condition === 'else') {
    return 'ELSE';
  }
  return condition;
}

/**
 * Calculate the width needed for a label
 * @param condition The connection condition
 * @returns The width for the label
 */
export function getLabelWidth(condition: string): number {
  const label = getDisplayLabel(condition);
  return Math.max(label.length * 8 + 16, 40); // Minimum width of 40px
} 