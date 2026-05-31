<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { Check, ChevronDown } from "lucide-vue-next";

export interface SelectOption {
  label: string;
  value: string | number;
  description?: string;
  disabled?: boolean;
}

const props = withDefaults(
  defineProps<{
    modelValue?: string | number;
    options: SelectOption[];
    placeholder?: string;
    disabled?: boolean;
    size?: "default" | "sm";
    ariaLabel?: string;
  }>(),
  {
    modelValue: "",
    placeholder: "请选择",
    disabled: false,
    size: "default",
    ariaLabel: "选择",
  },
);

const emit = defineEmits<{
  "update:modelValue": [value: string | number];
  change: [value: string | number];
}>();

const root = ref<HTMLElement | null>(null);
const open = ref(false);
const activeIndex = ref(-1);
const panelStyle = ref<Record<string, string>>({});

const selectedOption = computed(() =>
  props.options.find((option) => isSameValue(option.value, props.modelValue)),
);

const activeOption = computed(() =>
  activeIndex.value >= 0 ? props.options[activeIndex.value] : null,
);

const buttonSizeClass = computed(() =>
  props.size === "sm" ? "min-h-9 px-3 py-1.5 text-sm" : "min-h-11 px-3.5 py-2.5",
);

function isSameValue(a: string | number | undefined, b: string | number) {
  return String(a ?? "") === String(b ?? "");
}

function setActiveToSelected() {
  const selectedIndex = props.options.findIndex((option) =>
    isSameValue(option.value, props.modelValue),
  );
  activeIndex.value = selectedIndex >= 0 ? selectedIndex : firstEnabledIndex();
}

function firstEnabledIndex() {
  return props.options.findIndex((option) => !option.disabled);
}

function updatePanelPosition() {
  if (!root.value) return;
  const rect = root.value.getBoundingClientRect();
  const gap = 8;
  const maxPanelHeight = 260;
  const below = window.innerHeight - rect.bottom - gap;
  const above = rect.top - gap;
  const openAbove = below < 180 && above > below;
  const height = Math.max(
    140,
    Math.min(maxPanelHeight, openAbove ? above : below),
  );

  panelStyle.value = {
    left: `${rect.left}px`,
    top: openAbove
      ? `${Math.max(gap, rect.top - gap - height)}px`
      : `${rect.bottom + gap}px`,
    width: `${rect.width}px`,
    maxHeight: `${height}px`,
  };
}

function openMenu() {
  if (props.disabled) return;
  open.value = true;
  setActiveToSelected();
  nextTick(updatePanelPosition);
}

function toggleMenu() {
  if (open.value) {
    open.value = false;
    return;
  }
  openMenu();
}

function moveActive(step: number) {
  if (!props.options.length) return;
  let next = activeIndex.value;
  for (let i = 0; i < props.options.length; i += 1) {
    next = (next + step + props.options.length) % props.options.length;
    if (!props.options[next]?.disabled) {
      activeIndex.value = next;
      return;
    }
  }
}

function choose(option: SelectOption) {
  if (option.disabled) return;
  emit("update:modelValue", option.value);
  emit("change", option.value);
  open.value = false;
}

function onKeydown(event: KeyboardEvent) {
  if (props.disabled) return;

  if (event.key === "ArrowDown") {
    event.preventDefault();
    if (!open.value) openMenu();
    else moveActive(1);
  }

  if (event.key === "ArrowUp") {
    event.preventDefault();
    if (!open.value) openMenu();
    else moveActive(-1);
  }

  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    if (!open.value) {
      openMenu();
      return;
    }
    if (activeOption.value) choose(activeOption.value);
  }

  if (event.key === "Escape") {
    open.value = false;
  }
}

function onPointerDown(event: PointerEvent) {
  if (!root.value?.contains(event.target as Node)) open.value = false;
}

function onWindowLayoutChange() {
  if (open.value) updatePanelPosition();
}

onMounted(() => {
  window.addEventListener("pointerdown", onPointerDown);
  window.addEventListener("resize", onWindowLayoutChange);
  window.addEventListener("scroll", onWindowLayoutChange, true);
});

onBeforeUnmount(() => {
  window.removeEventListener("pointerdown", onPointerDown);
  window.removeEventListener("resize", onWindowLayoutChange);
  window.removeEventListener("scroll", onWindowLayoutChange, true);
});
</script>

<template>
  <div ref="root" class="ui-select relative" @keydown="onKeydown">
    <button
      type="button"
      class="group flex w-full items-center gap-2 rounded-lg border border-slate-200 bg-white text-left font-semibold text-slate-900 shadow-[0_1px_2px_rgba(15,23,42,0.03)] outline-none transition hover:border-slate-300 hover:bg-slate-50 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 disabled:cursor-not-allowed disabled:bg-slate-50 disabled:text-slate-400"
      :class="buttonSizeClass"
      :disabled="disabled"
      :aria-label="ariaLabel"
      :aria-expanded="open"
      aria-haspopup="listbox"
      @click="toggleMenu"
    >
      <span class="min-w-0 flex-1">
        <span
          class="block truncate"
          :class="selectedOption ? 'text-slate-900' : 'text-slate-400'"
        >
          {{ selectedOption?.label || placeholder }}
        </span>
        <span
          v-if="selectedOption?.description"
          class="mt-0.5 block truncate text-xs font-medium text-slate-500"
        >
          {{ selectedOption.description }}
        </span>
      </span>
      <ChevronDown
        :size="17"
        class="shrink-0 text-slate-500 transition group-disabled:text-slate-300"
        :class="open ? 'rotate-180 text-brand-600' : ''"
      />
    </button>

    <Transition
      enter-active-class="transition duration-150 ease-out"
      enter-from-class="-translate-y-1 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-100 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="-translate-y-1 opacity-0"
    >
      <div
        v-if="open"
        class="fixed z-[70] overflow-hidden rounded-lg border border-slate-200 bg-white shadow-[0_18px_42px_rgba(15,23,42,0.14)]"
        :style="panelStyle"
      >
        <div
          v-if="options.length"
          class="overflow-y-auto p-1.5 scrollbar-thin"
          role="listbox"
        >
          <button
            v-for="(option, index) in options"
            :key="String(option.value)"
            type="button"
            class="flex min-h-10 w-full items-center gap-2 rounded-md px-2.5 py-2 text-left text-sm transition"
            :class="[
              isSameValue(option.value, modelValue)
                ? 'bg-brand-50 text-brand-700'
                : 'text-slate-700 hover:bg-slate-50',
              activeIndex === index && !option.disabled
                ? 'ring-1 ring-inset ring-brand-100'
                : '',
              option.disabled ? 'cursor-not-allowed opacity-45' : '',
            ]"
            :disabled="option.disabled"
            role="option"
            :aria-selected="isSameValue(option.value, modelValue)"
            @mouseenter="activeIndex = index"
            @click="choose(option)"
          >
            <span class="min-w-0 flex-1">
              <span class="block truncate font-semibold">{{ option.label }}</span>
              <span
                v-if="option.description"
                class="mt-0.5 block truncate text-xs text-slate-500"
              >
                {{ option.description }}
              </span>
            </span>
            <Check
              v-if="isSameValue(option.value, modelValue)"
              :size="16"
              class="shrink-0 text-brand-600"
            />
          </button>
        </div>
        <div v-else class="px-3 py-2.5 text-sm font-medium text-slate-400">
          暂无可选项
        </div>
      </div>
    </Transition>
  </div>
</template>
