<template>
  <div class="dual-editor">
    <div v-if="$slots.toolbar" class="editor-toolbar">
      <slot name="toolbar"></slot>
    </div>
    <div class="convert-panels">
      <section class="panel-card">
        <div class="panel-header">{{ inputLabel }}</div>
        <el-input
          :model-value="inputModelValue"
          type="textarea"
          :rows="14"
          resize="none"
          class="panel-textarea"
          :placeholder="inputPlaceholder"
          @update:model-value="$emit('update:inputModelValue', $event)"
        />
      </section>
      <section class="panel-card">
        <div class="panel-header">{{ outputLabel }}</div>
        <slot name="output">
          <el-input
            :model-value="outputValue"
            type="textarea"
            :rows="14"
            resize="none"
            readonly
            class="panel-textarea"
          />
        </slot>
      </section>
    </div>
  </div>
</template>

<script setup>
defineProps({
  inputLabel: {
    type: String,
    required: true
  },
  outputLabel: {
    type: String,
    required: true
  },
  inputPlaceholder: {
    type: String,
    default: ''
  },
  inputModelValue: {
    type: String,
    default: ''
  },
  outputValue: {
    type: String,
    default: ''
  }
})

defineEmits(['update:inputModelValue'])
</script>

<style scoped>
.dual-editor {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.convert-panels {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.panel-card {
  border-radius: 22px;
  border: 1px solid rgba(210, 230, 246, 0.88);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(243, 249, 255, 0.92));
  box-shadow: 0 14px 28px rgba(179, 206, 229, 0.12);
  overflow: hidden;
}

.panel-header {
  padding: 16px 18px;
  border-bottom: 1px solid rgba(218, 234, 247, 0.86);
  color: #314255;
  font-size: 15px;
  font-weight: 700;
}

.panel-card :deep(.el-textarea__inner) {
  min-height: 320px !important;
  border: none;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  color: #233140;
  font-size: 14px;
  line-height: 1.75;
  padding: 18px;
}

.panel-card :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

@media (max-width: 900px) {
  .convert-panels {
    grid-template-columns: 1fr;
  }
}
</style>
