<script setup lang="ts">
import { onMounted, watch } from "vue";
import { RouterView, useRoute } from "vue-router";
import { useProjectStore } from "@/stores/project.store";

const route = useRoute();
const project = useProjectStore();

function loadProject() {
  if (!route.params.workspaceId || !route.params.projectId) return;
  project
    .load(String(route.params.workspaceId), String(route.params.projectId))
    .catch(() => {});
}

onMounted(loadProject);
watch(() => [route.params.workspaceId, route.params.projectId], loadProject);
</script>

<template>
  <RouterView />
</template>
