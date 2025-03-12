<template>
  <div class="condition-editor">
    <template v-for="(caseData, index) in conditionBlocks" :key="index">
      <div class="condition-case">
        <div class="case-header">
          <div class="case-type">
            <span class="case-label">{{ index === 0 ? 'IF' : 'ELIF' }}</span>
            <button 
              class="type-toggle-btn" 
              @click="toggleConditionType(index)"
              type="button"
            >
              {{ caseData.type.toUpperCase() }}
            </button>
          </div>
          <el-icon 
            v-if="index > 0"
            class="icon-btn"
            @click="removeCase(index)"
          >
            <Delete />
          </el-icon>
        </div>

        <div class="case-content">
          <div v-for="(condition, conditionIndex) in caseData.conditions" :key="conditionIndex" class="condition-item">
            <div class="condition-wrapper">
              <ConditionBuilder
                :modelValue="condition"
                @update:modelValue="(updatedCondition) => updateSingleCondition(index, conditionIndex, updatedCondition)"
              />
              <el-icon 
                v-if="canDeleteCondition(caseData)"
                class="icon-btn"
                @click="() => removeCondition(index, conditionIndex)"
                title="删除条件"
              >
                <Delete />
              </el-icon>
            </div>
          </div>
          <div
            class="add-condition-btn" 
            @click="() => addCondition(index)"
            title="添加条件"
          >
            添加条件
          </div>
        </div>
      </div>
    </template>

    <div class="add-case-wrapper">
      <button 
        class="add-case-btn" 
        @click="addCase"
        type="button"
      >
        添加{{ conditionBlocks.length === 0 ? 'IF' : 'ELIF' }}条件分支
      </button>
    </div>
  
    <!-- ELSE branch -->
    <div v-if="showElse" class="condition-case">
      <div class="case-header">
        <div class="case-type">
          <span class="case-label">ELSE</span>
        </div>
      </div>
      <div class="case-content">
        <div class="else-block">
          <small class="form-text text-muted">
            所有条件不满足时执行的分支
          </small>
        </div>
      </div>
    </div>

    
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import ConditionBuilder from '@/components/node-editors/ConditionBuilder.vue';
import type { ConditionCase, Condition } from '@/types/fields';
import type { Node } from '@/types/workflow';
import { Delete } from '@element-plus/icons-vue';
import { VariableType } from '@/types/fields';

interface WorkflowStore {
  selectedNode: Node | null;
  updateNode(id: string, updates: Partial<Node>): void;
}

const store = useWorkflowStore() as WorkflowStore;
const selectedNode = computed(() => store.selectedNode);

// 使用响应式数组管理条件组
const conditionBlocks = ref<ConditionCase[]>([]);

// 创建默认条件块
const createDefaultConditionBlock = (): ConditionCase => ({
  conditions: [{
    leftOperand: {
      name: '',
      type: VariableType.STRING,
      description: '',
      parent: ''
    },
    operator: '==',
    rightOperand: {
      name: '',
      type: VariableType.STRING,
      description: '',
      defaultValue: '',
      parent: ''
    },
    type: 'CONSTANT'
  }],
  type: 'and',
  hint: ''
});

// 更新条件组的hint，综合所有条件生成预览文本
const updateCaseHint = (caseIndex: number) => {
  if (caseIndex < conditionBlocks.value.length) {
    const caseData = conditionBlocks.value[caseIndex];
    const conditionTexts = caseData.conditions.map(condition => {
      const { leftOperand, operator, rightOperand, type } = condition;
      let hintString = `${leftOperand.name} ${operator} `
      if(type === 'CONSTANT') {
        if(leftOperand.type.toLowerCase() === VariableType.STRING) {
          hintString += `"${rightOperand.value}"`
        } else {
          hintString += `${rightOperand.value}`
        }
      } else {
        hintString += `${rightOperand.name}`
      }
      return hintString;
    });
    
    // 使用条件组的类型（and/or）连接所有条件
    caseData.hint = conditionTexts.join(` ${caseData.type.toUpperCase()} `);
  }
};

// 初始化条件块
const initializeConditionBlocks = () => {
  const node = selectedNode.value;
  if (!node) {
    conditionBlocks.value = [];
    return;
  }

  if (Array.isArray(node.conditions) && node.conditions.length > 0) {
    // 过滤掉ELSE分支，只保留IF和ELIF分支
    conditionBlocks.value = JSON.parse(JSON.stringify(
      node.conditions.filter(block => block.conditions.length > 0)
    ));
  } else {
    // 初始化时只创建默认的 IF 条件块
    conditionBlocks.value = [createDefaultConditionBlock()];
  }
  
  // 为每个条件组更新hint
  conditionBlocks.value.forEach((_, index) => {
    updateCaseHint(index);
  });
};

// 监听选中节点的变化，更新本地状态
watch(
  () => selectedNode.value,
  (newNode) => {
    if (newNode) {
      initializeConditionBlocks();
    }
  },
  { immediate: true }
);

// 更新节点中的条件数据
const updateNodeConditions = () => {
  const node = selectedNode.value;
  if (!node) return;

  // 确保条件数组有效，并添加ELSE分支
  const conditions = [
    ...conditionBlocks.value.map(block => ({
      conditions: block.conditions.map(condition => ({
        leftOperand: condition.leftOperand,
        operator: condition.operator,
        rightOperand: condition.rightOperand,
        type: condition.type
      })),
      type: block.type,
      hint: block.hint
    })),
    // 添加ELSE分支，确保type是 'and' | 'or'
    {
      conditions: [] as never[],
      type: 'and' as const,
      hint: ''
    }
  ];

  store.updateNode(node.id, {
    conditions
  });
};

// 更新单个条件
const updateSingleCondition = (caseIndex: number, conditionIndex: number, updatedCondition: Condition) => {
  if (caseIndex < conditionBlocks.value.length && 
      conditionIndex < conditionBlocks.value[caseIndex].conditions.length) {
    // 创建新的条件对象以确保响应性
    const newCondition = {
      leftOperand: updatedCondition.leftOperand,
      operator: updatedCondition.operator,
      rightOperand: updatedCondition.rightOperand,
      type: updatedCondition.type
    };
    
    conditionBlocks.value[caseIndex].conditions[conditionIndex] = newCondition;
    
    // 更新整个条件组的hint
    updateCaseHint(caseIndex);
    
    updateNodeConditions();
  }
};

// 添加新条件到指定条件组
function addCondition(index: number) {
  if (index < conditionBlocks.value.length) {
    conditionBlocks.value[index].conditions.push({
      leftOperand: {
        name: '',
        type: VariableType.STRING,
        description: '',
        defaultValue: '',
        parent: ''
      },
      operator: '==',
      rightOperand: {
        name: '',
        type: VariableType.STRING,
        description: '',
        defaultValue: '',
        parent: ''
      },
      type: 'CONSTANT'
    });
    
    // 更新条件组的hint
    updateCaseHint(index);
    
    updateNodeConditions();
  }
}

// 添加新的条件分支
function addCase() {
  const newCase = createDefaultConditionBlock();
  conditionBlocks.value.push(newCase);
  updateNodeConditions();
}

// 切换条件组类型（AND/OR）
function toggleConditionType(index: number) {
  if (index < conditionBlocks.value.length) {
    conditionBlocks.value[index].type = conditionBlocks.value[index].type === 'and' ? 'or' : 'and';
    
    // 更新条件组的hint
    updateCaseHint(index);
    
    updateNodeConditions();
  }
}

// 显示ELSE分支的计算属性
const showElse = computed(() => conditionBlocks.value.length > 0);

// 删除单个条件
function removeCondition(caseIndex: number, conditionIndex: number) {
  if (caseIndex < conditionBlocks.value.length) {
    const caseData = conditionBlocks.value[caseIndex];
    if (conditionIndex < caseData.conditions.length) {
      // 如果是最后一个条件，不允许删除
      if (caseData.conditions.length === 1) {
        return;
      }
      
      // 删除指定条件
      caseData.conditions.splice(conditionIndex, 1);
      
      // 更新条件组的hint
      updateCaseHint(caseIndex);
      
      updateNodeConditions();
    }
  }
}

// 删除条件分支
function removeCase(index: number) {
  if (index > 0 && index < conditionBlocks.value.length) {
    conditionBlocks.value.splice(index, 1);
    updateNodeConditions();
  }
}

// 判断是否可以删除条件
function canDeleteCondition(caseData: ConditionCase): boolean {
  return caseData.conditions.length > 1;
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
  border-radius: 8px;
}

.condition-case {
  border-radius: 8px;
  overflow: hidden;
}

.case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  background-color: var(--bg-tertiary);
}

.case-type {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.case-label {
  font-weight: 600;
  font-size: 1rem;
  color: var(--text-color);
}

.case-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.condition-wrapper {
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
  align-items: center;
}

.type-toggle-btn {
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 0.8rem;
  cursor: pointer;
  color: var(--text-color);
}

.type-toggle-btn:hover {
  background-color: var(--hover-bg-color);
}

.add-condition-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color);
  padding: 0.25rem;
  width: 100px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.add-condition-btn:hover {
  background-color: var(--hover-bg-color);
  border-style: solid;
}

.add-case-wrapper {
  padding: 0.5rem;
  border-radius: 8px;
}

.add-case-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background: none;
  border: 1px dashed var(--border-color);
  color: var(--text-color);
  padding: 0.75rem;
  width: 100%;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.add-case-btn:hover {
  background-color: var(--hover-bg-color);
  border-style: solid;
}

.else-block {
  /* padding: 1rem; */
  background-color: var(--bg-primary);
  border-radius: 4px;
}

.form-text {
  font-size: 0.875rem;
  color: var(--text-color);
  opacity: 0.7;
}
</style> 