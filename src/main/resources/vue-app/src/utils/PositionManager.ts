import type { Node } from '@/types/workflow';
import { NodeType } from '@/types/workflow';

export interface Point {
  x: number;
  y: number;
}

export enum PortType {
  INPUT = 'input',
  OUTPUT = 'output'
}

// Constants for node dimensions and spacing
export const NODE_LAYOUT = {
  WIDTH: 200,
  BASE_HEIGHT: 80,
  CONDITION_HEADER_HEIGHT: 30,
  CONDITION_PORT_SPACING: 24,
  PORT_OFFSET: 5,
  
  // Port positions relative to node
  INPUT_PORT_X: 0,
  OUTPUT_PORT_X: 200,
  STANDARD_PORT_Y: 40,
  
  // Additional constants for condition node layout
  CONDITION_FIRST_PORT_Y: 70, // Y position of the first condition port
  CONDITION_EXPRESSION_HEIGHT: 24, // Height of each condition expression
} as const;

export class PositionManager {
  /**
   * Calculate node height based on its type and content
   */
  static getNodeHeight(node: Node): number {
    if (node.type === NodeType.CONDITION && node.conditions) {
      return NODE_LAYOUT.BASE_HEIGHT + 
             node.conditions.length * NODE_LAYOUT.CONDITION_PORT_SPACING;
    }
    return NODE_LAYOUT.BASE_HEIGHT;
  }

  /**
   * Get position for a specific port on a node
   */
  static getPortPosition(node: Node, portType: PortType, condition?: string): Point {
    console.log(node, portType, condition);
    const basePosition = {
      [PortType.INPUT]: {
        x: node.position.x + NODE_LAYOUT.INPUT_PORT_X,
        y: node.position.y + NODE_LAYOUT.STANDARD_PORT_Y
      },
      [PortType.OUTPUT]: {
        x: node.position.x + NODE_LAYOUT.OUTPUT_PORT_X,
        y: node.position.y + NODE_LAYOUT.STANDARD_PORT_Y
      }
    };

    // Handle condition node output ports
    if (this.isConditionOutputPort(node, portType, condition)) {
      const index = this.getConditionIndex(condition);
      // console.log(node, portType, condition, index);
      if (index >= 0 && node.conditions) {
        // Calculate Y position based on condition index
        const p = {
          x: node.position.x + NODE_LAYOUT.OUTPUT_PORT_X,
          y: node.position.y + NODE_LAYOUT.CONDITION_FIRST_PORT_Y + 
             (index * NODE_LAYOUT.CONDITION_EXPRESSION_HEIGHT)
        };
        // console.log(p);
        return p;
      } else if (index === -2 && node.conditions) { // ELSE case
        // Position the ELSE port after all other conditions
        const p = {
          x: node.position.x + NODE_LAYOUT.OUTPUT_PORT_X,
          y: node.position.y + NODE_LAYOUT.CONDITION_FIRST_PORT_Y + 
             ((node.conditions.length - 1) * NODE_LAYOUT.CONDITION_EXPRESSION_HEIGHT)
        };
        // console.log(p);
        return p;
      }
    }
    // console.log(portType, basePosition[portType]);
    return basePosition[portType];
  }

  /**
   * Get position for connection label
   */
  static getConnectionLabelPosition(sourceNode: Node, targetNode: Node, condition: string): Point {
    const sourcePoint = this.getPortPosition(sourceNode, PortType.OUTPUT, condition);
    const targetPoint = this.getPortPosition(targetNode, PortType.INPUT);
    
    return {
      x: (sourcePoint.x + targetPoint.x) / 2,
      y: (sourcePoint.y + targetPoint.y) / 2
    };
  }

  /**
   * Calculate node style including position and dimensions
   */
  static getNodeStyle(node: Node, _scale: number = 1): Record<string, string> {
    return {
      transform: `translate(${node.position.x}px, ${node.position.y}px)`,
      width: `${NODE_LAYOUT.WIDTH}px`,
      minHeight: `${this.getNodeHeight(node)}px`
    };
  }

  /**
   * Calculate port style based on its type and position
   */
  static getPortStyle(node: Node, portType: PortType, condition?: string): Record<string, string> {
    const position = this.getPortPosition({ ...node, position: { x: 0, y: 0 } }, portType, condition);
    
    return {
      [portType === PortType.INPUT ? 'left' : 'right']: `-${NODE_LAYOUT.PORT_OFFSET}px`,
      top: `${position.y}px`,
      transform: 'translateY(-50%)'
    };
  }

  private static isConditionOutputPort(node: Node, portType: PortType, condition?: string): boolean {
    return portType === PortType.OUTPUT &&
           node.type === NodeType.CONDITION &&
           node.conditions != null &&
           condition != null &&
           condition !== 'default';
  }

  private static getConditionIndex(condition: string | undefined): number {
    if (condition && condition.startsWith('case')) {
      return parseInt(condition.slice(4)) - 1;
    } else if (condition === 'else') {
      return -2; // Special case for else
    }
    return -1;
  }
} 