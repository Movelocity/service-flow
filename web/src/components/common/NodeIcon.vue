<template>
  <el-icon :size="size" :class="['node-icon', `node-icon--${type.toLowerCase()}`]">
    <component :is="iconComponent" />
  </el-icon>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { 
  VideoPlay, 
  Tickets, 
  Filter, 
  CircleClose 
} from '@element-plus/icons-vue';
import { NodeType } from '@/types/workflow';

const props = defineProps<{
  type: NodeType;
  size?: number;
}>();

const iconComponent = computed(() => {
  switch (props.type) {
    case NodeType.START:
      return VideoPlay;
    case NodeType.FUNCTION:
      return Tickets;
    case NodeType.CONDITION:
      return Filter;
    case NodeType.END:
      return CircleClose;
    default:
      return null;
  }
});
</script> 
<style scoped>
.node-icon {
  padding: 2px;
  border-radius: 6px;
  color: white;
}

.node-icon--start {
  background-color: #8de254;
}

.node-icon--function {
  background-color: #57abe7;
}

.node-icon--condition {
  background-color: #e9c460;
}

.node-icon--end {
  background-color: #e26c68;
}

.dark {
  .node-icon--start {
    background-color: #6ba744;
  }

  .node-icon--function {
    background-color: #4482af;
  }

  .node-icon--condition {
    background-color: #bd9b3f;
  }

  .node-icon--end {
    background-color: #cc5856;
  }
}
</style>
