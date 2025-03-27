import type { Position } from '../types/workflow';

/**
 * 计算两点之间的贝塞尔曲线控制点
 */
export function calculateControlPoints(start: Position, end: Position): [Position, Position] {
  const dx = end.x - start.x;
  const dy = end.y - start.y;
  const distance = Math.sqrt(dx * dx + dy * dy);
  
  // 控制点距离为线段长度的1/3
  const controlDistance = distance / 3;
  
  return [
    { x: start.x + controlDistance, y: start.y },
    { x: end.x - controlDistance, y: end.y }
  ];
}

/**
 * 生成贝塞尔曲线路径
 */
export function generateBezierPath(start: Position, end: Position): string {
  const dx = Math.abs(end.x - start.x);
  const controlPointOffset = Math.min(80, dx / 2);
  
  const controlPoint1: Position = {
    x: start.x + controlPointOffset,
    y: start.y
  };
  
  const controlPoint2: Position = {
    x: end.x - controlPointOffset,
    y: end.y
  };
  
  return `M ${start.x} ${start.y} C ${controlPoint1.x} ${controlPoint1.y}, ${controlPoint2.x} ${controlPoint2.y}, ${end.x} ${end.y}`;
}

/**
 * 计算节点连接点的位置
 */
export function calculateConnectionPoint(nodePosition: Position, nodeWidth: number, nodeHeight: number, isInput: boolean): Position {
  return {
    x: nodePosition.x + (isInput ? 0 : nodeWidth),
    y: nodePosition.y + nodeHeight / 2
  };
}

/**
 * 检查点是否在矩形区域内
 */
export function isPointInRect(point: Position, rectPos: Position, width: number, height: number): boolean {
  return point.x >= rectPos.x &&
         point.x <= rectPos.x + width &&
         point.y >= rectPos.y &&
         point.y <= rectPos.y + height;
}

/**
 * 应用缩放和平移变换到坐标
 */
export function applyTransform(point: Position, scale: number, offset: Position): Position {
  return {
    x: point.x * scale + offset.x,
    y: point.y * scale + offset.y
  };
}

/**
 * 反向应用变换（从屏幕坐标转换到画布坐标）
 */
export function reverseTransform(point: Position, scale: number, offset: Position): Position {
  return {
    x: (point.x - offset.x) / scale,
    y: (point.y - offset.y) / scale
  };
}

/**
 * 对齐到网格
 */
export function snapToGrid(position: Position, gridSize: number = 20): Position {
  return {
    x: Math.round(position.x / gridSize) * gridSize,
    y: Math.round(position.y / gridSize) * gridSize
  };
}

/**
 * 计算两个节点之间的距离
 */
export function calculateDistance(point1: Position, point2: Position): number {
  const dx = point2.x - point1.x;
  const dy = point2.y - point1.y;
  return Math.sqrt(dx * dx + dy * dy);
}

/**
 * 检查是否形成循环连接
 */
export function wouldCreateCycle(
  connections: Array<{ sourceNodeId: string; targetNodeId: string }>,
  sourceId: string,
  targetId: string
): boolean {
  const graph = new Map<string, Set<string>>();
  
  // 构建图
  for (const conn of connections) {
    if (!graph.has(conn.sourceNodeId)) {
      graph.set(conn.sourceNodeId, new Set());
    }
    graph.get(conn.sourceNodeId)!.add(conn.targetNodeId);
  }
  
  // 添加新的连接
  if (!graph.has(sourceId)) {
    graph.set(sourceId, new Set());
  }
  graph.get(sourceId)!.add(targetId);
  
  // DFS检查循环
  const visited = new Set<string>();
  const path = new Set<string>();
  
  function dfs(node: string): boolean {
    if (path.has(node)) return true;
    if (visited.has(node)) return false;
    
    visited.add(node);
    path.add(node);
    
    const neighbors = graph.get(node) || new Set();
    for (const neighbor of neighbors) {
      if (dfs(neighbor)) return true;
    }
    
    path.delete(node);
    return false;
  }
  
  // 从源节点开始检查
  return dfs(sourceId);
}

/**
 * 判断点是否在线段附近
 */
export function isPointNearLine(
  point: Position,
  lineStart: Position,
  lineEnd: Position,
  threshold = 5
): boolean {
  const lineLength = calculateDistance(lineStart, lineEnd);
  if (lineLength === 0) return false;

  const t = ((point.x - lineStart.x) * (lineEnd.x - lineStart.x) +
            (point.y - lineStart.y) * (lineEnd.y - lineStart.y)) / (lineLength * lineLength);

  if (t < 0) return calculateDistance(point, lineStart) <= threshold;
  if (t > 1) return calculateDistance(point, lineEnd) <= threshold;

  const projection: Position = {
    x: lineStart.x + t * (lineEnd.x - lineStart.x),
    y: lineStart.y + t * (lineEnd.y - lineStart.y)
  };

  return calculateDistance(point, projection) <= threshold;
} 