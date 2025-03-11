<template>
  <div class="condition-editor">
    <template v-for="(caseData, index) in conditionBlocks" :key="index">
      <div class="condition-group">
        <div class="condition-header">
          {{ index === 0 ? 'IF' : (isLastCase(index) ? 'ELSE' : 'ELIF') }}
          <button 
            v-if="!isLastCase(index)"
            class="type-toggle-btn" 
            @click="() => toggleConditionType(index)"
            type="button"
          >
            {{ caseData.type.toUpperCase() }}
          </button>
        </div>
        <div class="condition-content">
          <div v-if="!isLastCase(index)">
            <div v-for="(condition, conditionIndex) in caseData.conditions" :key="conditionIndex" class="condition-item">
              <ConditionBuilder
                v-if="selectedNode"
                v-model="conditionBlocks[index].conditions[conditionIndex]"
                @change="updateCondition"
              />
            </div>
            <button 
              class="add-condition-btn" 
              @click="() => addCondition(index)"
              type="button"
            >
              + 添加条件
            </button>
          </div>
          <small v-else class="form-text text-muted">
            用于定义当所有条件不满足时应执行的逻辑。
          </small>
        </div>
      </div>
    </template>

    <div class="condition-group" v-if="!hasElse">
      <button 
        class="add-case-btn" 
        @click="addCase"
        type="button"
      >
        + 添加条件分支
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import ConditionBuilder from '@/components/node-editors/ConditionBuilder.vue';
import type { ConditionCase } from '@/types/condition';
import type { Node } from '@/types/workflow';
interface WorkflowStore {
  selectedNode: Node | null;
  updateNode(id: string, updates: Partial<Node>): void;
}

const store = useWorkflowStore() as WorkflowStore;
const selectedNode = computed(() => store.selectedNode as Node);

// 使用本地状态来管理条件组
const conditionBlocks = ref<ConditionCase[]>([{
  conditions: [{
    leftOperand: '',
    operator: '==',
    rightOperand: '',
    type: 'CONSTANT'
  }],
  type: 'and'
}]);

// 监听选中节点的变化，更新本地状态
watch(
  () => selectedNode.value?.conditions as ConditionCase[] | undefined,
  (newBlocks) => {
    if (newBlocks) {
      conditionBlocks.value = { ...newBlocks };
    } else {
      conditionBlocks.value = [{
        conditions: [{
          leftOperand: '',
          operator: '==',
          rightOperand: '',
          type: 'CONSTANT'
        }],
        type: 'and'
      }];
    }
  },
  { immediate: true }
);

function updateCondition() {
  if (selectedNode.value) {
    store.updateNode(selectedNode.value.id, {
      ...selectedNode.value,
      conditions: { ...conditionBlocks.value }
    });
  }
}

function addCondition(index: number) {
  if (conditionBlocks.value[index]) {
    conditionBlocks.value[index].conditions.push({
      leftOperand: '',
      operator: '==',
      rightOperand: '',
      type: 'CONSTANT'
    });
  }
  updateCondition();
}

function addCase() {
  conditionBlocks.value.push({
    conditions: [{
      leftOperand: '',
      operator: '==',
      rightOperand: '',
      type: 'CONSTANT'
    }],
    type: 'and'
  });
  
  updateCondition();
}

function toggleConditionType(index: number) {
  if (conditionBlocks.value[index]) {
    conditionBlocks.value[index].type = conditionBlocks.value[index].type === 'and' ? 'or' : 'and';
    updateCondition();
  }
}

const hasElse = computed(() => {
  return conditionBlocks.value.length > 0 && isLastCase(conditionBlocks.value.length - 1);
});

function isLastCase(index: number): boolean {
  return index === conditionBlocks.value.length - 1;
}

/**
 * 条件节点编辑器
 * 
 * 当前功能:
 * 1. 基础条件配置
 *    - 支持选择左操作数（工作流输入、全局变量、节点输出）
 *    - 支持基础比较操作符（==, !=, >, >=, <, <=, contains, notContains）
 *    - 支持右操作数为常量或变量
 * 2. 数据存储
 *    - 条件配置存储在节点的 context.condition 中
 *    - 使用标准化的条件数据结构
 * 3. 实时预览
 *    - 提供条件表达式的可读性预览
 * 
 * TODO:
 * 1. 高优先级
 *    - [ ] 根据变量类型动态显示合适的操作符
 *    - [ ] 添加输入值类型验证
 *    - [ ] 优化变量选择器的显示和分组
 * 
 * 2. 功能增强
 *    - [x] 支持多条件组合（AND/OR）
 *    - [ ] 支持条件组嵌套
 *    - [ ] 添加条件模板功能
 * 
 * 3. 调试功能
 *    - [ ] 添加条件测试工具
 *    - [ ] 提供条件执行日志
 *    - [ ] 可视化展示条件评估过程
 * 
 * 4. 性能优化
 *    - [ ] 优化条件解析和执行效率
 *    - [ ] 添加条件缓存机制
 *    - [ ] 实现增量更新
 */
</script>

<style scoped>
.condition-editor {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem 0;
}

.condition-group {
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
}

.condition-header {
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.condition-content {
  flex-grow: 1;
}

.condition-item {
  background-color: var(--bg-secondary);
  border-radius: 0.5rem;
}

.add-condition-btn,
.elif-btn {
  background: none;
  border: none;
  color: var(--text-color);
  padding: 0.5rem;
  text-align: left;
  cursor: pointer;
  font-size: 0.9rem;
}

.add-condition-btn:hover,
.elif-btn:hover {
  background-color: var(--hover-bg-color, rgba(0, 0, 0, 0.05));
  border-radius: 4px;
}

.form-text {
  font-size: 0.875rem;
  color: var(--text-color);
  opacity: 0.7;
}

.type-toggle-btn {
  background: none;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 2px 6px;
  margin-left: 8px;
  font-size: 0.8rem;
  cursor: pointer;
}

.type-toggle-btn:hover {
  background-color: var(--hover-bg-color, rgba(0, 0, 0, 0.05));
}

.add-case-btn {
  background: none;
  border: none;
  color: var(--text-color);
  padding: 0.5rem;
  text-align: left;
  cursor: pointer;
  font-size: 0.9rem;
  width: 100%;
}

.add-case-btn:hover {
  background-color: var(--hover-bg-color, rgba(0, 0, 0, 0.05));
  border-radius: 4px;
}
</style> 