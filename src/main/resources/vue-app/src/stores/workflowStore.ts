import { defineStore } from 'pinia'
import workflowApi from '../services/workflowApi'

interface Workflow {
  id: string
  name: string
  description: string
  isActive: boolean
  nodes: any[]
  connections: any[]
}

interface WorkflowState {
  workflows: Workflow[]
  currentWorkflow: Workflow | null
  loading: boolean
  error: string | null
}

export const useWorkflowStore = defineStore('workflow', {
  state: (): WorkflowState => ({
    workflows: [],
    currentWorkflow: null,
    loading: false,
    error: null
  }),
  
  getters: {
    getWorkflowById: (state) => (id: string) => {
      return state.workflows.find(workflow => workflow.id === id) || null
    }
  },
  
  actions: {
    /**
     * Fetch all workflows from the server
     */
    async fetchWorkflows() {
      this.loading = true
      this.error = null
      
      try {
        const workflowIds = await workflowApi.fetchWorkflows()
        this.workflows = []
        
        // Fetch details for each workflow
        for (const id of workflowIds) {
          try {
            const workflow = await workflowApi.fetchWorkflow(id)
            this.workflows.push(workflow)
          } catch (error) {
            console.error(`Error fetching workflow ${id}:`, error)
          }
        }
      } catch (error: any) {
        this.error = error.message || 'Failed to fetch workflows'
        console.error('Error in fetchWorkflows:', error)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * Fetch a single workflow by ID
     * @param {string} id - The ID of the workflow to fetch
     */
    async fetchWorkflow(id: string) {
      this.loading = true
      this.error = null
      
      try {
        const workflow = await workflowApi.fetchWorkflow(id)
        
        // Update the workflow in the list if it exists
        const index = this.workflows.findIndex(w => w.id === id)
        if (index !== -1) {
          this.workflows[index] = workflow
        } else {
          this.workflows.push(workflow)
        }
        
        this.currentWorkflow = workflow
      } catch (error: any) {
        this.error = error.message || `Failed to fetch workflow ${id}`
        console.error('Error in fetchWorkflow:', error)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * Save a workflow to the server
     * @param {Workflow} workflow - The workflow to save
     */
    async saveWorkflow(workflow: Workflow) {
      this.loading = true
      this.error = null
      
      try {
        const savedWorkflow = await workflowApi.saveWorkflow(workflow)
        
        // Update the workflow in the list if it exists
        const index = this.workflows.findIndex(w => w.id === savedWorkflow.id)
        if (index !== -1) {
          this.workflows[index] = savedWorkflow
        } else {
          this.workflows.push(savedWorkflow)
        }
        
        this.currentWorkflow = savedWorkflow
        return savedWorkflow
      } catch (error: any) {
        this.error = error.message || 'Failed to save workflow'
        console.error('Error in saveWorkflow:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    /**
     * Delete a workflow from the server
     * @param {string} id - The ID of the workflow to delete
     */
    async deleteWorkflow(id: string) {
      this.loading = true
      this.error = null
      
      try {
        await workflowApi.deleteWorkflow(id)
        
        // Remove the workflow from the list
        this.workflows = this.workflows.filter(w => w.id !== id)
        
        // Clear the current workflow if it was deleted
        if (this.currentWorkflow && this.currentWorkflow.id === id) {
          this.currentWorkflow = null
        }
        
        return true
      } catch (error: any) {
        this.error = error.message || `Failed to delete workflow ${id}`
        console.error('Error in deleteWorkflow:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    /**
     * Create a new workflow
     * @returns {Workflow} - The new workflow
     */
    createNewWorkflow(): Workflow {
      const newWorkflow: Workflow = {
        id: 'workflow_' + Date.now(),
        name: 'New Workflow',
        description: '',
        isActive: false,
        nodes: [],
        connections: []
      }
      
      this.currentWorkflow = newWorkflow
      return newWorkflow
    }
  }
}) 