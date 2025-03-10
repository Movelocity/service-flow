<template>
  <div class="form-group">
    <label class="form-label">条件配置</label>
    <ConditionBuilder
      v-if="selectedNode"
      v-model="nodeCondition"
      @change="updateCondition"
    />
    <small class="form-text text-muted">
      选择变量并设置条件，支持字符串、数字和布尔值类型的比较
    </small>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import ConditionBuilder from '@/components/node-editors/ConditionBuilder.vue';
import type { Node } from '@/types/workflow';

interface Condition {
  leftOperand: string;
  operator: string;
  rightOperand: string;
  type: 'VARIABLE' | 'CONSTANT';
}

const store = useWorkflowStore();
const selectedNode = computed(() => store.selectedNode as Node);

const nodeCondition = computed({
  get: () => {
    if (!selectedNode.value?.context?.condition) {
      return {
        leftOperand: '',
        operator: '==',
        rightOperand: '',
        type: 'CONSTANT' as const
      };
    }
    return selectedNode.value.context.condition as Condition;
  },
  set: (value: Condition) => {
    if (selectedNode.value) {
      store.updateNode(selectedNode.value.id, {
        ...selectedNode.value,
        context: {
          ...selectedNode.value.context,
          condition: value
        }
      });
    }
  }
});

function updateCondition() {
  if (selectedNode.value) {
    store.updateNode(selectedNode.value.id, selectedNode.value);
  }
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
.form-group {
  margin-bottom: 1rem;
}

.form-text {
  font-size: 0.875rem;
  margin-top: 0.25rem;
  color: var(--text-color);
  opacity: 0.7;
}
</style> 