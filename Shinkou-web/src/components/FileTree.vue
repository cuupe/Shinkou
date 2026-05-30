<script setup lang="ts">
import { ref } from "vue";
import { ChevronDown, ChevronRight, FileCode2, Folder } from "lucide-vue-next";
import type { FileTreeNode } from "@/types/domain";

const props = defineProps<{ nodes: FileTreeNode[]; activePath?: string }>();
const emit = defineEmits<{ select: [node: FileTreeNode] }>();
const open = ref<Set<string>>(new Set());

function toggle(node: FileTreeNode) {
  if (node.type === "directory") {
    open.value.has(node.path)
      ? open.value.delete(node.path)
      : open.value.add(node.path);
    open.value = new Set(open.value);
  } else {
    emit("select", node);
  }
}

function isOpen(path: string) {
  return open.value.has(path);
}
</script>

<template>
  <ul class="space-y-0.5 text-sm">
    <li v-for="node in props.nodes" :key="node.path">
      <button
        class="flex w-full items-center gap-2 rounded-lg px-2 py-1.5 text-left hover:bg-slate-100"
        :class="
          activePath === node.path
            ? 'bg-brand-50 font-semibold text-brand-700'
            : 'text-slate-700'
        "
        type="button"
        @click="toggle(node)"
      >
        <ChevronDown
          v-if="node.type === 'directory' && isOpen(node.path)"
          :size="15"
        />
        <ChevronRight v-else-if="node.type === 'directory'" :size="15" />
        <span v-else class="w-[15px]"></span>
        <Folder
          v-if="node.type === 'directory'"
          :size="15"
          class="text-slate-500"
        />
        <FileCode2 v-else :size="15" class="text-slate-500" />
        <span class="truncate">{{ node.name }}</span>
      </button>
      <FileTree
        v-if="
          node.type === 'directory' &&
          isOpen(node.path) &&
          node.children?.length
        "
        class="ml-5 border-l border-slate-100 pl-2"
        :nodes="node.children"
        :active-path="activePath"
        @select="emit('select', $event)"
      />
    </li>
  </ul>
</template>
