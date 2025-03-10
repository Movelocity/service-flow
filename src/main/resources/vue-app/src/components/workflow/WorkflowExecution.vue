<template>
  <div class="workflow-execution">
    <!-- Workflow Inputs -->
    <div v-if="workflow" class="workflow-inputs">
      <h3>Workflow Inputs</h3>
      <div v-for="(def, key) in workflow.inputs" :key="key" class="input-field">
        <label :for="`input-${key}`">{{ key }}</label>
        <template v-if="def.type === VariableType.BOOLEAN">
          <input
            :id="`input-${key}`"
            type="checkbox"
            v-model="executionInputs[key]"
          />
        </template>
        <template v-else-if="def.type === VariableType.NUMBER">
          <input
            :id="`input-${key}`"
            type="number"
            v-model.number="executionInputs[key]"
          />
        </template>
        <template v-else>
          <input
            :id="`input-${key}`"
            type="text"
            v-model="executionInputs[key]"
          />
        </template>
        <div class="input-description">{{ def.description }}</div>
      </div>
      <button @click="executeWorkflow" :disabled="isExecuting">
        {{ isExecuting ? 'Executing...' : 'Execute Workflow' }}
      </button>
    </div>

    <!-- Execution Status -->
    <div v-if="executionId" class="execution-status">
      <h3>Execution Status</h3>
      <p>Status: {{ executionStatus }}</p>
      
      <!-- Execution Results -->
      <div v-if="executionStatus === 'COMPLETED'" class="execution-results">
        <h4>Results</h4>
        <div v-for="(value, key) in executionOutputs" :key="key" class="output-field">
          <strong>{{ key }}:</strong> {{ value }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { Workflow, VariableDefinition } from '@/types/workflow';
import { VariableType } from '@/types/workflow';
import { useWorkflowStore } from '@/stores/workflow';
import { workflowApi } from '@/services/workflowApi';

const props = defineProps<{
  workflow: Workflow;
}>();

const store = useWorkflowStore();
const executionInputs = ref<Record<string, any>>({});
const executionOutputs = ref<Record<string, any>>({});
const executionId = ref<string | null>(null);
const executionStatus = ref<string | null>(null);
const isExecuting = ref(false);

// Initialize inputs with default values
const initializeInputs = () => {
  if (props.workflow) {
    Object.entries(props.workflow.inputs).forEach(([key, def]) => {
      executionInputs.value[key] = def.defaultValue ?? getDefaultValue(def);
    });
  }
};

// Get default value based on variable type
const getDefaultValue = (def: VariableDefinition) => {
  switch (def.type) {
    case VariableType.STRING:
      return '';
    case VariableType.NUMBER:
      return 0;
    case VariableType.BOOLEAN:
      return false;
    case VariableType.OBJECT:
      return {};
    case VariableType.ARRAY:
      return [];
    default:
      return null;
  }
};

// Get input type for HTML input element
const getInputType = (type: VariableType) => {
  switch (type) {
    case VariableType.NUMBER:
      return 'number';
    case VariableType.BOOLEAN:
      return 'checkbox';
    default:
      return 'text';
  }
};

// Execute the workflow
const executeWorkflow = async () => {
  try {
    isExecuting.value = true;
    executionId.value = await workflowApi.executeWorkflow(props.workflow.id, executionInputs.value);
    
    // Poll for execution status
    const pollStatus = async () => {
      if (!executionId.value) return;
      
      const status = await workflowApi.getExecutionStatus(props.workflow.id, executionId.value);
      executionStatus.value = status;
      
      if (status === 'COMPLETED' || status === 'FAILED') {
        isExecuting.value = false;
      } else {
        setTimeout(pollStatus, 1000); // Poll every second
      }
    };
    
    pollStatus();
  } catch (error) {
    console.error('Failed to execute workflow:', error);
    isExecuting.value = false;
    executionStatus.value = 'FAILED';
  }
};

// Initialize inputs when component is mounted
initializeInputs();
</script>

<style scoped>
.workflow-execution {
  padding: 1rem;
}

.workflow-inputs,
.execution-status {
  margin-bottom: 1rem;
  padding: 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.input-field {
  margin-bottom: 1rem;
}

.input-field label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.input-field input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.input-field input[type="checkbox"] {
  width: auto;
  margin-right: 0.5rem;
}

.input-description {
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: #666;
}

button {
  padding: 0.5rem 1rem;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  background-color: #e0e0e0;
  cursor: not-allowed;
}

.execution-results {
  margin-top: 1rem;
}

.output-field {
  margin-bottom: 0.5rem;
}
</style> 