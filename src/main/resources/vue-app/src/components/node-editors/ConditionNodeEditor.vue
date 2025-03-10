<template>
  <div class="condition-editor">
    <div v-for="(block, blockIndex) in conditionBlocks" :key="blockIndex" class="condition-group">
      <div class="condition-header">{{ block.type }}</div>
      <div class="condition-content">
        <div v-for="(_condition, conditionIndex) in block.conditions" :key="conditionIndex" class="condition-item">
          <ConditionBuilder
            v-if="selectedNode"
            v-model="conditionBlocks[blockIndex].conditions[conditionIndex]"
            @change="updateCondition"
          />
        </div>
        <button class="add-condition-btn" @click="addCondition" v-if="block.type === 'IF'">
          + 添加条件
        </button>
      </div>
    </div>

    <div class="condition-group">
      <button class="elif-btn" @click="addElif">+ ELIF</button>
    </div>

    <div class="condition-group">
      <div class="condition-header">ELSE</div>
      <div class="condition-content">
        <small class="form-text text-muted">
          用于定义当 if 条件不满足时应执行的逻辑。
        </small>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import ConditionBuilder from '@/components/node-editors/ConditionBuilder.vue';
import type { Node } from '@/types/workflow';

interface Condition {
  leftOperand: string;
  operator: string;
  rightOperand: string;
  type: 'VARIABLE' | 'CONSTANT';
}

interface ConditionBlock {
  type: 'IF' | 'ELIF';
  conditions: Condition[];
}

const store = useWorkflowStore();
const selectedNode = computed(() => store.selectedNode as Node);

// 使用本地状态来管理条件组
const conditionBlocks = ref<ConditionBlock[]>([
  {
    type: 'IF',
    conditions: [{
      leftOperand: '',
      operator: '==',
      rightOperand: '',
      type: 'CONSTANT'
    }]
  }
]);

// 监听选中节点的变化，更新本地状态
watch(
  () => selectedNode.value?.context?.conditionBlocks,
  (newBlocks) => {
    if (newBlocks) {
      conditionBlocks.value = [...newBlocks];
    } else {
      conditionBlocks.value = [{
        type: 'IF',
        conditions: [{
          leftOperand: '',
          operator: '==',
          rightOperand: '',
          type: 'CONSTANT'
        }]
      }];
    }
  },
  { immediate: true }
);

function updateCondition() {
  if (selectedNode.value) {
    store.updateNode(selectedNode.value.id, {
      ...selectedNode.value,
      context: {
        ...selectedNode.value.context,
        conditionBlocks: [...conditionBlocks.value]
      }
    });
  }
}

function addCondition() {
  if (conditionBlocks.value[0]) {
    conditionBlocks.value[0].conditions.push({
      leftOperand: '',
      operator: '==',
      rightOperand: '',
      type: 'CONSTANT'
    });
  }
  updateCondition();
}

function addElif() {
  conditionBlocks.value.push({
    type: 'ELIF',
    conditions: [{
      leftOperand: '',
      operator: '==',
      rightOperand: '',
      type: 'CONSTANT'
    }]
  });
  updateCondition();
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
 *    - [ ] 支持多条件组合（AND/OR）
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
</style> 