import { computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow'
import type { VariableDef } from '@/types/fields'

/**
 * Composable hook to get available context for a node
 * @param nodeId - The ID of the node to get context for
 * @returns computed object containing available context including global inputs and node context
 */
export function useAvailableContext(nodeId: string) {
  const workflowStore = useWorkflowStore()

  return computed(() => {
    const context: VariableDef[] = []
    
    // Add workflow inputs with 'global.' prefix
    if (workflowStore.currentWorkflow?.inputs) {
      workflowStore.currentWorkflow.inputs.forEach(input => {
        context.push({...input, parent: 'global'})
      })
    }
    
    // Get the current node
    const currentNode = workflowStore.currentWorkflow?.nodes.find(node => node.id === nodeId)
    
    // Add node context
    const nodeContext = currentNode?.context || []
    nodeContext.forEach(contextItem => {
      context.push(contextItem)
    })
    
    return context
  })
} 