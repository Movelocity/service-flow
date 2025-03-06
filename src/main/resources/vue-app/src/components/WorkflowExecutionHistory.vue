<script setup lang="ts">
/**
 * WorkflowExecutionHistory component
 * Displays the execution history of a workflow
 */
import { ref, onMounted, watch } from 'vue'
import workflowApi from '../services/workflowApi'

const props = defineProps<{
  workflowId: string
}>()

// Execution history data
const executions = ref<any[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)

// Fetch execution history
const fetchExecutionHistory = async () => {
  if (!props.workflowId) return
  
  isLoading.value = true
  error.value = null
  
  try {
    // This would be implemented in the API service
    // For now, we'll use mock data
    executions.value = [
      {
        id: 'exec_001',
        startTime: new Date(Date.now() - 3600000).toISOString(),
        endTime: new Date(Date.now() - 3540000).toISOString(),
        status: 'COMPLETED',
        input: { orderId: '12345' },
        output: { status: 'PROCESSED' }
      },
      {
        id: 'exec_002',
        startTime: new Date(Date.now() - 7200000).toISOString(),
        endTime: new Date(Date.now() - 7140000).toISOString(),
        status: 'FAILED',
        input: { orderId: '12346' },
        error: 'Service unavailable'
      },
      {
        id: 'exec_003',
        startTime: new Date(Date.now() - 86400000).toISOString(),
        endTime: new Date(Date.now() - 86340000).toISOString(),
        status: 'COMPLETED',
        input: { orderId: '12347' },
        output: { status: 'PROCESSED' }
      }
    ]
  } catch (err: any) {
    error.value = err.message || 'Failed to fetch execution history'
    console.error('Error fetching execution history:', err)
  } finally {
    isLoading.value = false
  }
}

// Format date for display
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString()
}

// Get status badge class
const getStatusBadgeClass = (status: string) => {
  switch (status) {
    case 'COMPLETED':
      return 'bg-success'
    case 'FAILED':
      return 'bg-danger'
    case 'RUNNING':
      return 'bg-primary'
    case 'WAITING':
      return 'bg-warning'
    default:
      return 'bg-secondary'
  }
}

// View execution details
const selectedExecution = ref<any>(null)

const viewExecutionDetails = (execution: any) => {
  selectedExecution.value = execution
}

const closeExecutionDetails = () => {
  selectedExecution.value = null
}

// Load execution history when the component is mounted or workflowId changes
onMounted(fetchExecutionHistory)
watch(() => props.workflowId, fetchExecutionHistory)
</script>

<template>
  <div class="workflow-execution-history">
    <h5 class="mb-3">执行历史</h5>
    
    <div v-if="isLoading" class="text-center my-3">
      <div class="spinner-border spinner-border-sm" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    
    <div v-else-if="error" class="alert alert-danger" role="alert">
      {{ error }}
    </div>
    
    <div v-else-if="executions.length === 0" class="text-center text-muted my-3">
      No execution history found for this workflow.
    </div>
    
    <div v-else class="table-responsive">
      <table class="table table-hover">
        <thead>
          <tr>
            <th>执行 ID</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="execution in executions" :key="execution.id">
            <td>{{ execution.id }}</td>
            <td>{{ formatDate(execution.startTime) }}</td>
            <td>{{ formatDate(execution.endTime) }}</td>
            <td>
              <span class="badge" :class="getStatusBadgeClass(execution.status)">
                {{ execution.status }}
              </span>
            </td>
            <td>
              <button 
                class="btn btn-sm btn-outline-primary" 
                @click="viewExecutionDetails(execution)"
              >
                查看详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Execution Details Modal -->
    <div 
      class="modal fade" 
      :class="{ show: selectedExecution }" 
      tabindex="-1" 
      :style="{ display: selectedExecution ? 'block' : 'none' }"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">执行详情: {{ selectedExecution?.id }}</h5>
            <button 
              type="button" 
              class="btn-close" 
              @click="closeExecutionDetails" 
              aria-label="Close"
            ></button>
          </div>
          <div class="modal-body" v-if="selectedExecution">
            <div class="row mb-3">
              <div class="col-md-6">
                <div class="mb-2">
                  <strong>开始时间:</strong> {{ formatDate(selectedExecution.startTime) }}
                </div>
                <div class="mb-2">
                  <strong>结束时间:</strong> {{ formatDate(selectedExecution.endTime) }}
                </div>
                <div class="mb-2">
                  <strong>状态:</strong> 
                  <span class="badge" :class="getStatusBadgeClass(selectedExecution.status)">
                    {{ selectedExecution.status }}
                  </span>
                </div>
              </div>
            </div>
            
            <div class="row">
              <div class="col-md-6">
                <h6>输入数据</h6>
                <pre class="code-block">{{ JSON.stringify(selectedExecution.input, null, 2) }}</pre>
              </div>
              <div class="col-md-6">
                <h6>输出数据</h6>
                <pre v-if="selectedExecution.output" class="code-block">{{ JSON.stringify(selectedExecution.output, null, 2) }}</pre>
                <pre v-else-if="selectedExecution.error" class="code-block error">{{ selectedExecution.error }}</pre>
                <div v-else class="text-muted">No output data available</div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeExecutionDetails">关闭</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Modal backdrop -->
    <div 
      class="modal-backdrop fade" 
      :class="{ show: selectedExecution }" 
      v-if="selectedExecution"
    ></div>
  </div>
</template>

<style scoped>
.workflow-execution-history {
  margin-bottom: 30px;
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

/* Modal styles */
.modal.show {
  display: block;
}

.modal-backdrop.show {
  opacity: 0.5;
}
</style> 