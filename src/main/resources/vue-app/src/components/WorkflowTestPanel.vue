<script setup lang="ts">
/**
 * WorkflowTestPanel component
 * Panel for testing a workflow with custom input data
 */
import { ref, computed } from 'vue'
import workflowApi from '../services/workflowApi'

const props = defineProps<{
  workflowId: string
  show: boolean
}>()

const emit = defineEmits<{
  'close': []
  'test-complete': [executionId: string]
}>()

// Test input data
const inputData = ref<string>('{\n  \n}')
const isValidJson = computed(() => {
  try {
    JSON.parse(inputData.value)
    return true
  } catch (e) {
    return false
  }
})

// Test execution state
const isExecuting = ref(false)
const executionError = ref<string | null>(null)
const executionResult = ref<any>(null)

// Execute the workflow test
const executeTest = async () => {
  if (!isValidJson.value) {
    executionError.value = 'Invalid JSON input'
    return
  }
  
  isExecuting.value = true
  executionError.value = null
  executionResult.value = null
  
  try {
    const input = JSON.parse(inputData.value)
    const executionId = await workflowApi.startWorkflowExecution(props.workflowId, input)
    
    // Poll for execution status
    const checkStatus = async () => {
      try {
        const status = await workflowApi.getWorkflowExecutionStatus(props.workflowId, executionId)
        
        if (status === 'COMPLETED' || status === 'FAILED') {
          // Execution completed or failed
          executionResult.value = {
            id: executionId,
            status,
            // In a real implementation, we would fetch the complete execution result
            output: status === 'COMPLETED' ? { result: 'Success' } : null,
            error: status === 'FAILED' ? 'Execution failed' : null
          }
          isExecuting.value = false
          emit('test-complete', executionId)
        } else {
          // Still running, check again after a delay
          setTimeout(checkStatus, 1000)
        }
      } catch (err: any) {
        executionError.value = err.message || 'Failed to check execution status'
        isExecuting.value = false
      }
    }
    
    // For demo purposes, simulate a completed execution after a delay
    setTimeout(() => {
      executionResult.value = {
        id: executionId,
        status: 'COMPLETED',
        output: { result: 'Success', processedData: input }
      }
      isExecuting.value = false
      emit('test-complete', executionId)
    }, 2000)
    
    // In a real implementation, we would use the polling mechanism
    // checkStatus()
  } catch (err: any) {
    executionError.value = err.message || 'Failed to start workflow execution'
    isExecuting.value = false
  }
}

// Close the panel
const closePanel = () => {
  emit('close')
}

// Format JSON for display
const formatJson = (json: any) => {
  return JSON.stringify(json, null, 2)
}
</script>

<template>
  <div v-if="show" class="workflow-test-panel">
    <div class="panel-header">
      <h5>测试工作流</h5>
      <button type="button" class="btn-close" @click="closePanel" aria-label="Close"></button>
    </div>
    
    <div class="panel-body">
      <div class="mb-3">
        <label for="inputData" class="form-label">输入数据 (JSON)</label>
        <textarea 
          id="inputData" 
          class="form-control" 
          v-model="inputData" 
          rows="10"
          :class="{ 'is-invalid': !isValidJson && inputData.trim() !== '' }"
        ></textarea>
        <div class="invalid-feedback" v-if="!isValidJson && inputData.trim() !== ''">
          Invalid JSON format
        </div>
      </div>
      
      <div v-if="executionError" class="alert alert-danger" role="alert">
        {{ executionError }}
      </div>
      
      <div v-if="executionResult" class="mt-4">
        <h6>执行结果</h6>
        <div class="card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <span>执行 ID: {{ executionResult.id }}</span>
            <span 
              class="badge" 
              :class="{
                'bg-success': executionResult.status === 'COMPLETED',
                'bg-danger': executionResult.status === 'FAILED'
              }"
            >
              {{ executionResult.status }}
            </span>
          </div>
          <div class="card-body">
            <div v-if="executionResult.output">
              <h6>输出数据</h6>
              <pre class="code-block">{{ formatJson(executionResult.output) }}</pre>
            </div>
            <div v-if="executionResult.error">
              <h6>错误信息</h6>
              <pre class="code-block error">{{ executionResult.error }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="panel-footer">
      <button type="button" class="btn btn-secondary me-2" @click="closePanel">关闭</button>
      <button 
        type="button" 
        class="btn btn-primary" 
        @click="executeTest" 
        :disabled="isExecuting || (!isValidJson && inputData.trim() !== '')"
      >
        <span v-if="isExecuting" class="spinner-border spinner-border-sm me-1" role="status"></span>
        {{ isExecuting ? '执行中...' : '执行测试' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.workflow-test-panel {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  max-width: 90vw;
  max-height: 90vh;
  background-color: white;
  border-radius: 6px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  z-index: 1050;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #dee2e6;
}

.panel-body {
  flex-grow: 1;
  padding: 15px;
  overflow-y: auto;
  max-height: 70vh;
}

.panel-footer {
  padding: 15px;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: flex-end;
}

.code-block {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 10px;
  max-height: 200px;
  overflow-y: auto;
  font-size: 0.875rem;
}

.code-block.error {
  background-color: #f8d7da;
  border-color: #f5c6cb;
  color: #721c24;
}
</style> 