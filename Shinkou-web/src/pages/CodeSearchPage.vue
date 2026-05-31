<script setup lang="ts">
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
} from "vue";
import * as monaco from "monaco-editor/esm/vs/editor/editor.api";
import "monaco-editor/esm/vs/basic-languages/java/java.contribution";
import "monaco-editor/esm/vs/basic-languages/sql/sql.contribution";
import {
  AlertCircle,
  Bot,
  Check,
  CheckCircle2,
  ChevronDown,
  ChevronRight,
  FileCode2,
  Files,
  Folder,
  GitBranch,
  PanelBottom,
  Save,
  Search,
  Sparkles,
  TerminalSquare,
  Triangle,
  X,
  XCircle,
} from "lucide-vue-next";

type Activity = "explorer" | "search" | "source" | "agent";
type SuggestionStatus = "pending" | "applied" | "rejected";
type BottomTab = "problems" | "output" | "terminal";
type IdeTheme = "dark" | "light";
type ResizeTarget = "sidebar" | "copilot" | "bottom";

interface MockFile {
  id: string;
  name: string;
  path: string;
  folder: string;
  language: string;
  content: string;
}

interface SearchResult {
  id: string;
  fileId: string;
  line: number;
  score: number;
  snippet: string;
  type: string;
}

interface AiSuggestion {
  id: string;
  fileId: string;
  title: string;
  summary: string;
  reason: string;
  line: number;
  confidence: number;
  before: string;
  after: string;
  impact: string[];
  status: SuggestionStatus;
}

interface TreeRow {
  id: string;
  name: string;
  type: "dir" | "file";
  depth: number;
  fileId?: string;
  expanded?: boolean;
}

const mockFiles: MockFile[] = [
  {
    id: "order-service",
    name: "OrderService.java",
    path: "order-service/src/main/java/com/demo/order/service/OrderService.java",
    folder: "service",
    language: "java",
    content: `package com.demo.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderService {
    private final CouponService couponService;
    private final OrderMapper orderMapper;

    public Order createOrder(CreateOrderRequest req) {
        BigDecimal payAmount = req.getPayAmount();
        Order order = new Order();
        order.setUserId(req.getUserId());
        order.setPayAmount(payAmount);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        // 优惠券抵扣逻辑
        if (StringUtils.isNotBlank(req.getCouponCode())) {
            Coupon coupon = couponService.getValidCoupon(
                req.getUserId(), req.getCouponCode(), payAmount);
            if (coupon != null) {
                BigDecimal discount = coupon.calculateDiscount(payAmount);
                order.setCouponId(coupon.getId());
                order.setDiscountAmount(discount);
                order.setPayAmount(payAmount.subtract(discount));
            } else {
                throw new BizException("优惠券无效或不满足使用条件");
            }
        }

        orderMapper.insert(order);
        return order;
    }
}`,
  },
  {
    id: "coupon-service",
    name: "CouponService.java",
    path: "order-service/src/main/java/com/demo/order/service/CouponService.java",
    folder: "service",
    language: "java",
    content: `package com.demo.order.service;

import java.math.BigDecimal;

public class CouponService {
    public Coupon getValidCoupon(Long userId, String couponCode, BigDecimal payAmount) {
        Coupon coupon = couponRepository.findByCode(couponCode);
        if (coupon == null || !coupon.getUserId().equals(userId)) {
            return null;
        }
        if (payAmount.compareTo(coupon.getMinAmount()) < 0) {
            return null;
        }
        return coupon;
    }
}`,
  },
  {
    id: "order-controller",
    name: "OrderController.java",
    path: "order-service/src/main/java/com/demo/order/controller/OrderController.java",
    folder: "controller",
    language: "java",
    content: `package com.demo.order.controller;

public class OrderController {
    @PostMapping("/orders")
    public Order create(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }
}`,
  },
  {
    id: "coupon-rule",
    name: "coupon_rule.sql",
    path: "order-service/sql/coupon_rule.sql",
    folder: "sql",
    language: "sql",
    content: `create table coupon_rule (
    id bigint primary key,
    coupon_code varchar(64) not null,
    min_amount decimal(12, 2) not null,
    discount_amount decimal(12, 2) not null
);`,
  },
];

const mockResults: SearchResult[] = [
  {
    id: "r1",
    fileId: "order-service",
    line: 17,
    score: 0.96,
    type: "方法体匹配",
    snippet:
      "Coupon coupon = couponService.getValidCoupon(req.getUserId(), req.getCouponCode(), payAmount);",
  },
  {
    id: "r2",
    fileId: "coupon-service",
    line: 7,
    score: 0.92,
    type: "业务规则",
    snippet: "if (coupon == null || !coupon.getUserId().equals(userId)) { return null; }",
  },
  {
    id: "r3",
    fileId: "order-controller",
    line: 5,
    score: 0.78,
    type: "调用入口",
    snippet: "return orderService.createOrder(request);",
  },
];

const suggestions = ref<AiSuggestion[]>([
  {
    id: "s1",
    fileId: "order-service",
    title: "避免优惠金额超过订单金额",
    summary: "限制 discount 上限，避免出现负数订单。",
    reason: "当前逻辑直接 payAmount.subtract(discount)，当优惠券配置异常时会产生负金额。",
    line: 22,
    confidence: 94,
    before: `                BigDecimal discount = coupon.calculateDiscount(payAmount);
                order.setCouponId(coupon.getId());
                order.setDiscountAmount(discount);
                order.setPayAmount(payAmount.subtract(discount));`,
    after: `                BigDecimal discount = coupon.calculateDiscount(payAmount);
                if (discount.compareTo(payAmount) > 0) {
                    discount = payAmount;
                }
                order.setCouponId(coupon.getId());
                order.setDiscountAmount(discount);
                order.setPayAmount(payAmount.subtract(discount));`,
    impact: ["防止负支付金额", "不改变正常路径", "建议补充异常配置测试"],
    status: "pending",
  },
  {
    id: "s2",
    fileId: "coupon-service",
    title: "补充优惠券编码空值保护",
    summary: "查询前拦截空 couponCode，减少无意义数据库访问。",
    reason: "服务方法本身缺少防御式校验。",
    line: 6,
    confidence: 88,
    before: `    public Coupon getValidCoupon(Long userId, String couponCode, BigDecimal payAmount) {
        Coupon coupon = couponRepository.findByCode(couponCode);`,
    after: `    public Coupon getValidCoupon(Long userId, String couponCode, BigDecimal payAmount) {
        if (StringUtils.isBlank(couponCode)) {
            return null;
        }
        Coupon coupon = couponRepository.findByCode(couponCode);`,
    impact: ["减少无效查询", "增强服务层健壮性"],
    status: "pending",
  },
  {
    id: "s3",
    fileId: "order-controller",
    title: "为创建订单入口增加校验注解",
    summary: "给请求体增加 @Valid，交由参数校验处理必填字段。",
    reason: "Controller 入口缺少标准校验触发点。",
    line: 4,
    confidence: 81,
    before: `    public Order create(@RequestBody CreateOrderRequest request) {`,
    after: `    public Order create(@Valid @RequestBody CreateOrderRequest request) {`,
    impact: ["提前拦截无效请求", "确认已引入 validation"],
    status: "pending",
  },
]);

const editorHost = ref<HTMLDivElement | null>(null);
const activeActivity = ref<Activity>("explorer");
const bottomTab = ref<BottomTab>("problems");
const ideTheme = ref<IdeTheme>(
  (localStorage.getItem("shinkou-code-theme") as IdeTheme | null) || "dark",
);
const commandQuery = ref("");
const keyword = ref("coupon createOrder payAmount");
const explorerQuery = ref("");
const activeFileId = ref("order-service");
const activeSuggestionId = ref("s1");
const selectedResultId = ref("r1");
const savedAt = ref("unsaved");
const showBottomPanel = ref(true);
const terminalCommand = ref("npm test -- OrderService");
const terminalLines = ref([
  "Shinkou terminal ready.",
  "AI review loaded 3 suggestions from mock context.",
]);
const openTabIds = ref(["order-service", "coupon-service"]);
const sidebarWidth = ref(286);
const copilotWidth = ref(338);
const bottomPanelHeight = ref(150);
const expandedDirs = ref([
  "order-service",
  "order-service/src",
  "order-service/src/main",
  "order-service/src/main/java",
  "order-service/src/main/java/com",
  "order-service/src/main/java/com/demo",
  "order-service/src/main/java/com/demo/order",
  "order-service/src/main/java/com/demo/order/service",
  "order-service/src/main/java/com/demo/order/controller",
  "order-service/sql",
]);
const fileContents = reactive<Record<string, string>>(
  Object.fromEntries(mockFiles.map((file) => [file.id, file.content])),
);
const originalContents = reactive<Record<string, string>>(
  Object.fromEntries(mockFiles.map((file) => [file.id, file.content])),
);

let editor: monaco.editor.IStandaloneCodeEditor | null = null;
let contentListener: monaco.IDisposable | null = null;
let revealDecorations: string[] = [];
let suggestionDecorations: string[] = [];

const activityItems = [
  { id: "explorer", label: "Explorer", icon: Files },
  { id: "search", label: "Search", icon: Search },
  { id: "source", label: "Source Control", icon: GitBranch },
  { id: "agent", label: "Shinkou AI", icon: Bot },
] as const;

const activeFile = computed(
  () => mockFiles.find((file) => file.id === activeFileId.value) || mockFiles[0],
);
const hasOpenEditor = computed(() => Boolean(activeFileId.value));
const activeFileName = computed(() =>
  hasOpenEditor.value ? activeFile.value.name : "No file open",
);
const activeFileLanguage = computed(() =>
  hasOpenEditor.value ? activeFile.value.language.toUpperCase() : "PLAINTEXT",
);
const activeFilePathParts = computed(() =>
  hasOpenEditor.value ? activeFile.value.path.split("/") : ["No file open"],
);
const themeClass = computed(() =>
  ideTheme.value === "light" ? "theme-light" : "theme-dark",
);
const themeLabel = computed(() =>
  ideTheme.value === "light" ? "Light+" : "Dark+",
);
const activeSuggestion = computed(
  () =>
    suggestions.value.find((item) => item.id === activeSuggestionId.value) ||
    suggestions.value[0],
);
const openTabs = computed(() => {
  const ids = new Set([...openTabIds.value, activeFileId.value].filter(Boolean));
  return [...ids]
    .map((id) => mockFiles.find((file) => file.id === id))
    .filter(Boolean) as MockFile[];
});
const changedFiles = computed(() =>
  mockFiles.filter((item) => fileContents[item.id] !== originalContents[item.id]),
);
const treeRows = computed<TreeRow[]>(() => buildTreeRows());
const filteredResults = computed(() => {
  const query = keyword.value.trim().toLowerCase();
  if (!query) return mockResults;
  return mockResults.filter((result) => {
    const file = getFile(result.fileId);
    const haystack = `${file?.path || ""} ${result.snippet} ${result.type}`.toLowerCase();
    return query
      .split(/\s+/)
      .filter(Boolean)
      .some((part) => haystack.includes(part));
  });
});
const fileSuggestions = computed(() =>
  suggestions.value.filter((item) => item.fileId === activeFileId.value),
);
const dirty = computed(
  () =>
    Boolean(activeFileId.value) &&
    fileContents[activeFileId.value] !== originalContents[activeFileId.value],
);
const bodyGridStyle = computed(() => ({
  gridTemplateColumns: `48px ${sidebarWidth.value}px minmax(360px, 1fr) ${copilotWidth.value}px`,
}));
const bottomPanelStyle = computed(() => ({
  height: `${bottomPanelHeight.value}px`,
}));
const pendingCount = computed(
  () => suggestions.value.filter((item) => item.status === "pending").length,
);
const rejectedCount = computed(
  () => suggestions.value.filter((item) => item.status === "rejected").length,
);
const appliedCount = computed(
  () => suggestions.value.filter((item) => item.status === "applied").length,
);

function buildTreeRows() {
  interface TreeNode {
    id: string;
    name: string;
    type: "dir" | "file";
    children: Map<string, TreeNode>;
    fileId?: string;
  }

  const query = explorerQuery.value.trim().toLowerCase();
  const forceExpanded = Boolean(query);
  const root: TreeNode = {
    id: "",
    name: "",
    type: "dir",
    children: new Map(),
  };

  mockFiles
    .filter((file) => {
      if (!query) return true;
      return (
        file.name.toLowerCase().includes(query) ||
        file.path.toLowerCase().includes(query)
      );
    })
    .forEach((file) => {
      const parts = file.path.split("/");
      let cursor = root;
      parts.forEach((part, index) => {
        const isFile = index === parts.length - 1;
        const id = isFile ? file.id : parts.slice(0, index + 1).join("/");
        if (!cursor.children.has(id)) {
          cursor.children.set(id, {
            id,
            name: part,
            type: isFile ? "file" : "dir",
            children: new Map(),
            fileId: isFile ? file.id : undefined,
          });
        }
        cursor = cursor.children.get(id)!;
      });
    });

  const rows: TreeRow[] = [];
  const walk = (nodes: Iterable<TreeNode>, depth: number) => {
    [...nodes]
      .sort((a, b) => {
        if (a.type !== b.type) return a.type === "dir" ? -1 : 1;
        return a.name.localeCompare(b.name);
      })
      .forEach((node) => {
        const expanded = forceExpanded || expandedDirs.value.includes(node.id);
        rows.push({
          id: node.id,
          name: node.name,
          type: node.type,
          depth,
          fileId: node.fileId,
          expanded,
        });
        if (node.type === "dir" && expanded) {
          walk(node.children.values(), depth + 1);
        }
      });
  };

  walk(root.children.values(), 0);
  return rows;
}

onMounted(async () => {
  await nextTick();
  if (!editorHost.value) return;

  editor = monaco.editor.create(editorHost.value, {
    value: fileContents[activeFileId.value],
    language: activeFile.value.language,
    theme: ideTheme.value === "dark" ? "vs-dark" : "vs",
    automaticLayout: true,
    fontSize: 13,
    lineHeight: 22,
    minimap: { enabled: true, scale: 0.8 },
    scrollBeyondLastLine: false,
    wordWrap: "on",
    roundedSelection: false,
    tabSize: 4,
    renderLineHighlight: "all",
    overviewRulerBorder: false,
    padding: { top: 12, bottom: 12 },
  });

  contentListener = editor.onDidChangeModelContent(() => {
    if (!editor) return;
    if (!activeFileId.value) return;
    fileContents[activeFileId.value] = editor.getValue();
  });

  renderInlineSuggestions();
  revealLine(17);
});

onBeforeUnmount(() => {
  contentListener?.dispose();
  editor?.dispose();
});

function getFile(fileId: string) {
  return mockFiles.find((file) => file.id === fileId);
}

function clearEditorMarks() {
  if (!editor) return;
  suggestionDecorations = editor.deltaDecorations(suggestionDecorations, []);
  revealDecorations = editor.deltaDecorations(revealDecorations, []);
}

function toggleTreeRow(row: TreeRow) {
  if (row.type === "file" && row.fileId) {
    switchFile(row.fileId);
    return;
  }
  const exists = expandedDirs.value.includes(row.id);
  expandedDirs.value = exists
    ? expandedDirs.value.filter((id) => id !== row.id)
    : [...expandedDirs.value, row.id];
}

function collectDirectoryIds() {
  const ids = new Set<string>();
  mockFiles.forEach((file) => {
    const parts = file.path.split("/");
    parts.slice(0, -1).forEach((_, index) => {
      ids.add(parts.slice(0, index + 1).join("/"));
    });
  });
  return [...ids];
}

function expandAllTree() {
  expandedDirs.value = collectDirectoryIds();
}

function collapseAllTree() {
  expandedDirs.value = [];
}

function switchFile(fileId: string, line = 1) {
  activeFileId.value = fileId;
  if (!openTabIds.value.includes(fileId)) {
    openTabIds.value.push(fileId);
  }
  const file = getFile(fileId);
  if (!file || !editor) return;

  const model = editor.getModel();
  if (model) monaco.editor.setModelLanguage(model, file.language);
  editor.updateOptions({ readOnly: false });
  editor.setValue(fileContents[fileId]);
  nextTick(() => {
    renderInlineSuggestions();
    revealLine(line);
  });
}

function closeTab(fileId: string, event?: Event) {
  event?.stopPropagation();
  const index = openTabIds.value.indexOf(fileId);
  openTabIds.value = openTabIds.value.filter((id) => id !== fileId);

  if (fileId === activeFileId.value) {
    const nextId = openTabIds.value[Math.max(0, index - 1)] || openTabIds.value[0];
    if (nextId) {
      switchFile(nextId);
      return;
    }
    activeFileId.value = "";
    clearEditorMarks();
    if (editor) {
      const model = editor.getModel();
      if (model) monaco.editor.setModelLanguage(model, "plaintext");
      editor.setValue("");
      editor.updateOptions({ readOnly: true });
    }
  }
}

function runSearch() {
  activeActivity.value = "search";
  const first = filteredResults.value[0];
  if (!first) return;
  openResult(first);
}

function executeCommand() {
  const value = commandQuery.value.trim();
  if (!value) return;

  if (value.startsWith(">")) {
    const command = value.slice(1).trim().toLowerCase();
    if (command.includes("theme")) toggleTheme();
    else if (command.includes("format")) formatCode();
    else if (command.includes("terminal")) {
      showBottomPanel.value = true;
      bottomTab.value = "terminal";
    } else if (command.includes("source")) activeActivity.value = "source";
    else if (command.includes("agent")) activeActivity.value = "agent";
    terminalLines.value.push(`[command] ${value}`);
    commandQuery.value = "";
    return;
  }

  const fileMatch = mockFiles.find((file) =>
    file.name.toLowerCase().includes(value.toLowerCase()),
  );
  if (fileMatch) {
    switchFile(fileMatch.id);
  } else {
    keyword.value = value;
    runSearch();
  }
  commandQuery.value = "";
}

function focusEditor() {
  editor?.focus();
}

function toggleTheme() {
  ideTheme.value = ideTheme.value === "dark" ? "light" : "dark";
  localStorage.setItem("shinkou-code-theme", ideTheme.value);
  monaco.editor.setTheme(ideTheme.value === "dark" ? "vs-dark" : "vs");
  nextTick(renderInlineSuggestions);
}

function clamp(value: number, min: number, max: number) {
  return Math.min(Math.max(value, min), max);
}

function startResize(target: ResizeTarget, event: MouseEvent) {
  event.preventDefault();
  const startX = event.clientX;
  const startY = event.clientY;
  const startSidebar = sidebarWidth.value;
  const startCopilot = copilotWidth.value;
  const startBottom = bottomPanelHeight.value;

  const move = (moveEvent: MouseEvent) => {
    if (target === "sidebar") {
      sidebarWidth.value = clamp(startSidebar + moveEvent.clientX - startX, 210, 420);
    } else if (target === "copilot") {
      copilotWidth.value = clamp(startCopilot - (moveEvent.clientX - startX), 280, 520);
    } else {
      bottomPanelHeight.value = clamp(startBottom - (moveEvent.clientY - startY), 110, 340);
    }
    editor?.layout();
  };
  const stop = () => {
    document.body.classList.remove("vsc-is-resizing");
    window.removeEventListener("mousemove", move);
    window.removeEventListener("mouseup", stop);
  };

  document.body.classList.add("vsc-is-resizing");
  window.addEventListener("mousemove", move);
  window.addEventListener("mouseup", stop);
}

function openResult(result: SearchResult) {
  selectedResultId.value = result.id;
  switchFile(result.fileId, result.line);
}

function revealLine(line: number) {
  if (!editor) return;
  editor.revealLineInCenter(line);
  editor.setPosition({ lineNumber: line, column: 1 });
  revealDecorations = editor.deltaDecorations(revealDecorations, [
    {
      range: new monaco.Range(line, 1, line, 1),
      options: {
        isWholeLine: true,
        className: "vsc-line-reveal",
        glyphMarginClassName: "vsc-glyph-reveal",
      },
    },
  ]);
}

function renderInlineSuggestions() {
  if (!editor) return;
  if (!activeFileId.value) {
    clearEditorMarks();
    return;
  }

  const inlineSuggestions = suggestions.value.filter(
    (suggestion) => suggestion.fileId === activeFileId.value,
  );

  suggestionDecorations = editor.deltaDecorations(
    suggestionDecorations,
    inlineSuggestions.map((suggestion) => ({
      range: new monaco.Range(suggestion.line, 1, suggestion.line, 1),
      options: {
        isWholeLine: true,
        linesDecorationsClassName: `vsc-ai-line vsc-ai-line--${suggestion.status}`,
        glyphMarginClassName: `vsc-ai-glyph vsc-ai-glyph--${suggestion.status}`,
      },
    })),
  );
}

async function formatCode() {
  await editor?.getAction("editor.action.formatDocument")?.run();
  const model = editor?.getModel();
  if (!editor || !model) return;
  const formatted = model
    .getValue()
    .replace(/[ \t]+$/gm, "")
    .replace(/\n{3,}/g, "\n\n");
  if (formatted === model.getValue()) return;
  editor.pushUndoStop();
  editor.executeEdits("format-whitespace", [
    {
      range: model.getFullModelRange(),
      text: formatted,
    },
  ]);
  editor.pushUndoStop();
}

function saveMock() {
  if (!activeFileId.value) return;
  originalContents[activeFileId.value] =
    editor?.getValue() || fileContents[activeFileId.value];
  savedAt.value = new Date().toLocaleTimeString("zh-CN", {
    hour: "2-digit",
    minute: "2-digit",
  });
  terminalLines.value.push(`[source] Saved ${activeFile.value.name}`);
}

function resetFile() {
  if (!activeFileId.value) return;
  const original = originalContents[activeFileId.value];
  fileContents[activeFileId.value] = original;
  editor?.setValue(original);
  terminalLines.value.push(`[source] Discarded changes in ${activeFile.value.name}`);
}

function saveAllChanges() {
  const count = changedFiles.value.length;
  changedFiles.value.forEach((file) => {
    originalContents[file.id] = fileContents[file.id];
  });
  savedAt.value = new Date().toLocaleTimeString("zh-CN", {
    hour: "2-digit",
    minute: "2-digit",
  });
  terminalLines.value.push(`[source] Saved ${count} changed files`);
}

function discardAllChanges() {
  changedFiles.value.forEach((file) => {
    fileContents[file.id] = originalContents[file.id];
  });
  editor?.setValue(activeFileId.value ? fileContents[activeFileId.value] : "");
  terminalLines.value.push("[source] Discarded all mock changes");
}

function focusSuggestion(suggestion: AiSuggestion) {
  activeSuggestionId.value = suggestion.id;
  activeActivity.value = "agent";
  if (suggestion.fileId !== activeFileId.value) {
    switchFile(suggestion.fileId, suggestion.line);
    return;
  }
  revealLine(suggestion.line);
}

function applySuggestionToText(text: string, suggestion: AiSuggestion) {
  const matchIndex = text.indexOf(suggestion.before);
  if (matchIndex < 0) {
    const lines = text.split("\n");
    lines.splice(Math.max(suggestion.line - 1, 0), 0, suggestion.after);
    return lines.join("\n");
  }
  return `${text.slice(0, matchIndex)}${suggestion.after}${text.slice(
    matchIndex + suggestion.before.length,
  )}`;
}

function replaceSuggestionInActiveEditor(suggestion: AiSuggestion) {
  const model = editor?.getModel();
  if (!editor || !model) return false;

  const current = model.getValue();
  const matchIndex = current.indexOf(suggestion.before);
  editor.pushUndoStop();
  if (matchIndex >= 0) {
    const start = model.getPositionAt(matchIndex);
    const end = model.getPositionAt(matchIndex + suggestion.before.length);
    editor.executeEdits("ai-suggestion", [
      {
        range: new monaco.Range(
          start.lineNumber,
          start.column,
          end.lineNumber,
          end.column,
        ),
        text: suggestion.after,
      },
    ]);
  } else {
    editor.executeEdits("ai-suggestion", [
      {
        range: new monaco.Range(suggestion.line, 1, suggestion.line, 1),
        text: `${suggestion.after}\n`,
      },
    ]);
  }
  editor.pushUndoStop();
  return true;
}

function applySuggestion(
  suggestion: AiSuggestion,
  options: { keepFocus?: boolean } = {},
) {
  if (suggestion.status !== "pending") return;

  const targetFile = getFile(suggestion.fileId);
  if (!targetFile) return;

  const applyToActiveEditor = suggestion.fileId === activeFileId.value && editor;
  if (applyToActiveEditor) {
    replaceSuggestionInActiveEditor(suggestion);
    fileContents[suggestion.fileId] = editor!.getValue();
    suggestion.status = "applied";
    terminalLines.value.push(`[agent] Accepted inline suggestion: ${suggestion.title}`);
    renderInlineSuggestions();
    revealLine(suggestion.line);
    if (options.keepFocus) editor?.focus();
    return;
  }

  fileContents[suggestion.fileId] = applySuggestionToText(
    fileContents[suggestion.fileId],
    suggestion,
  );
  suggestion.status = "applied";
  terminalLines.value.push(`[agent] Accepted suggestion in ${targetFile.name}: ${suggestion.title}`);

  if (!options.keepFocus) {
    switchFile(suggestion.fileId, suggestion.line);
    return;
  }

  nextTick(() => {
    suggestion.status = "applied";
    renderInlineSuggestions();
  });
}

function applyAllPending() {
  const pending = suggestions.value.filter((item) => item.status === "pending");
  if (!pending.length) return;
  const currentFileSuggestion =
    pending.find((item) => item.fileId === activeFileId.value) || pending[0];
  applySuggestion(currentFileSuggestion);
  terminalLines.value.push(
    `[agent] Accepted suggestion: ${currentFileSuggestion.title}`,
  );
}

function applyAllForCurrentFile() {
  if (!activeFileId.value) return;
  const pending = suggestions.value.filter(
    (item) => item.fileId === activeFileId.value && item.status === "pending",
  );
  if (!pending.length) return;
  let nextText = editor?.getValue() || fileContents[activeFileId.value];
  pending.forEach((item) => {
    nextText = applySuggestionToText(nextText, item);
    item.status = "applied";
  });
  fileContents[activeFileId.value] = nextText;
  editor?.setValue(nextText);
  terminalLines.value.push(
    `[agent] Accepted ${pending.length} suggestions in ${activeFile.value.name}`,
  );
  renderInlineSuggestions();
  revealLine(pending[0].line);
}

function rejectSuggestion(
  suggestion: AiSuggestion,
  options: { keepFocus?: boolean } = {},
) {
  if (suggestion.status !== "pending") return;
  suggestion.status = "rejected";
  terminalLines.value.push(`[agent] Rejected suggestion: ${suggestion.title}`);
  if (activeSuggestionId.value === suggestion.id) {
    const next = suggestions.value.find((item) => item.status === "pending");
    if (next) activeSuggestionId.value = next.id;
  }
  renderInlineSuggestions();
  if (options.keepFocus) editor?.focus();
}

function rejectAllForCurrentFile() {
  const current = suggestions.value.filter(
    (item) => item.fileId === activeFileId.value && item.status === "pending",
  );
  current.forEach((item) => {
    item.status = "rejected";
  });
  terminalLines.value.push(
    `[agent] Rejected ${current.length} pending suggestions in ${activeFile.value.name}`,
  );
  renderInlineSuggestions();
}

function runTerminalCommand() {
  const command = terminalCommand.value.trim();
  if (!command) return;
  terminalLines.value.push(`$ ${command}`);
  if (command.includes("test")) {
    terminalLines.value.push("PASS OrderServiceTest.shouldClampCouponDiscount");
    terminalLines.value.push("PASS CouponServiceTest.shouldIgnoreBlankCouponCode");
  } else if (command.includes("lint")) {
    terminalLines.value.push("No lint issues found in mock workspace.");
  } else {
    terminalLines.value.push(`Mock command completed: ${command}`);
  }
  bottomTab.value = "terminal";
  terminalCommand.value = "";
}

function statusLabel(status: SuggestionStatus) {
  if (status === "applied") return "Accepted";
  if (status === "rejected") return "Rejected";
  return "Pending";
}
</script>

<template>
  <section class="vsc-shell" :class="themeClass">
    <div class="vsc-titlebar">
      <label class="vsc-command-center">
        <Search :size="14" />
        <input
          v-model="commandQuery"
          placeholder="Search files or run >command"
          @keyup.enter="executeCommand"
        />
      </label>
      <div class="vsc-title-actions">
        <button type="button" @click="toggleTheme">
          {{ themeLabel }}
        </button>
        <button type="button" @click="saveMock">
          <Save :size="14" />
          Save
        </button>
      </div>
    </div>

    <div class="vsc-body" :style="bodyGridStyle">
      <aside class="vsc-activitybar">
        <button
          v-for="item in activityItems"
          :key="item.id"
          :title="item.label"
          type="button"
          :class="{ active: activeActivity === item.id }"
          @click="activeActivity = item.id"
        >
          <component :is="item.icon" :size="23" />
          <span v-if="item.id === 'agent' && pendingCount" class="vsc-badge">
            {{ pendingCount }}
          </span>
        </button>
      </aside>

      <aside class="vsc-sidebar">
        <div class="vsc-sidebar-title">
          <span v-if="activeActivity === 'explorer'">EXPLORER</span>
          <span v-else-if="activeActivity === 'search'">SEARCH</span>
          <span v-else-if="activeActivity === 'source'">SOURCE CONTROL</span>
          <span v-else>SHINKOU AI</span>
          <button
            v-if="activeActivity === 'explorer'"
            type="button"
            title="Collapse all"
            @click="collapseAllTree"
          >
            <ChevronDown :size="14" />
          </button>
          <button v-else type="button" @click="activeActivity = 'explorer'">
            <ChevronDown :size="14" />
          </button>
        </div>

        <div v-if="activeActivity === 'explorer'" class="vsc-panel-scroll">
          <div class="vsc-section-header">
            <button type="button" @click="expandAllTree">
              <ChevronDown :size="13" />
              ORDER-SERVICE
            </button>
            <button type="button" title="Collapse all" @click="collapseAllTree">
              <X :size="13" />
            </button>
          </div>
          <label class="vsc-input-wrap">
            <Search :size="14" />
            <input v-model="explorerQuery" placeholder="Filter files" />
          </label>
          <button
            v-for="row in treeRows"
            :key="row.id"
            class="vsc-file-row vsc-tree-row"
            :class="{ active: row.fileId === activeFileId, 'is-dir': row.type === 'dir' }"
            :style="{ paddingLeft: `${7 + row.depth * 14}px` }"
            type="button"
            @click="toggleTreeRow(row)"
          >
            <component
              :is="row.type === 'dir' ? (row.expanded ? ChevronDown : ChevronRight) : FileCode2"
              :size="15"
            />
            <Folder v-if="row.type === 'dir'" :size="15" />
            <span>{{ row.name }}</span>
            <i
              v-if="
                row.fileId &&
                fileContents[row.fileId] !== originalContents[row.fileId]
              "
            />
          </button>
          <div v-if="!treeRows.length" class="vsc-empty">No matching files</div>
        </div>

        <div v-else-if="activeActivity === 'search'" class="vsc-panel-scroll">
          <label class="vsc-input-wrap">
            <Search :size="14" />
            <input
              v-model="keyword"
              placeholder="Search"
              @keyup.enter="runSearch"
            />
          </label>
          <button class="vsc-primary-btn" type="button" @click="runSearch">
            Search
          </button>
          <div class="vsc-section-label">
            RESULTS {{ filteredResults.length }}
          </div>
          <button
            v-for="result in filteredResults"
            :key="result.id"
            class="vsc-search-row"
            :class="{ active: result.id === selectedResultId }"
            type="button"
            @click="openResult(result)"
          >
            <strong>{{ getFile(result.fileId)?.name }}</strong>
            <span>Line {{ result.line }} · {{ result.type }}</span>
            <code>{{ result.snippet }}</code>
          </button>
        </div>

        <div v-else-if="activeActivity === 'source'" class="vsc-panel-scroll">
          <div class="vsc-section-label">CHANGES</div>
          <button
            v-for="file in changedFiles"
            :key="file.id"
            class="vsc-file-row"
            type="button"
            @click="switchFile(file.id)"
          >
            <GitBranch :size="15" />
            <span>{{ file.name }}</span>
          </button>
          <div v-if="changedFiles.length" class="vsc-source-actions">
            <button type="button" @click="saveAllChanges">Save All</button>
            <button type="button" @click="discardAllChanges">Discard All</button>
          </div>
          <div
            v-if="!changedFiles.length"
            class="vsc-empty"
          >
            No source changes
          </div>
        </div>

        <div v-else class="vsc-panel-scroll">
          <div class="vsc-agent-summary">
            <Sparkles :size="18" />
            <strong>{{ pendingCount }}</strong>
            <span>pending suggestions</span>
          </div>
          <button
            v-for="suggestion in suggestions"
            :key="suggestion.id"
            class="vsc-agent-row"
            :class="[
              { active: activeSuggestion?.id === suggestion.id },
              `is-${suggestion.status}`,
            ]"
            type="button"
            @click="focusSuggestion(suggestion)"
          >
            <span>{{ statusLabel(suggestion.status) }}</span>
            <strong>{{ suggestion.title }}</strong>
            <small>{{ getFile(suggestion.fileId)?.name }}:{{ suggestion.line }}</small>
          </button>
        </div>
        <div
          class="vsc-resizer vsc-resizer--sidebar"
          @mousedown="startResize('sidebar', $event)"
        />
      </aside>

      <main class="vsc-workbench">
        <div class="vsc-tabs">
          <button
            v-for="tab in openTabs"
            :key="tab.id"
            type="button"
            class="vsc-tab"
            :class="{ active: tab.id === activeFileId }"
            @click="switchFile(tab.id)"
          >
            <FileCode2 :size="14" />
            <span>{{ tab.name }}</span>
            <i v-if="fileContents[tab.id] !== originalContents[tab.id]" />
            <X :size="13" @click="closeTab(tab.id, $event)" />
          </button>
        </div>

        <div class="vsc-breadcrumbs">
          <span v-for="part in activeFilePathParts" :key="part">
            {{ part }}
          </span>
        </div>

        <div class="vsc-editor-wrap">
          <div class="vsc-editor-pane">
            <div ref="editorHost" class="h-full w-full" />
            <div v-if="!hasOpenEditor" class="vsc-empty-editor">
              <FileCode2 :size="34" />
              <span>No editor is open</span>
              <button type="button" @click="switchFile(mockFiles[0].id)">
                Open OrderService.java
              </button>
            </div>
          </div>
        </div>

        <section
          v-if="showBottomPanel"
          class="vsc-bottom-panel"
          :style="bottomPanelStyle"
        >
          <div
            class="vsc-resizer vsc-resizer--bottom"
            @mousedown="startResize('bottom', $event)"
          />
          <div class="vsc-panel-tabs">
            <button
              type="button"
              :class="{ active: bottomTab === 'problems' }"
              @click="bottomTab = 'problems'"
            >
              PROBLEMS
            </button>
            <button
              type="button"
              :class="{ active: bottomTab === 'output' }"
              @click="bottomTab = 'output'"
            >
              OUTPUT
            </button>
            <button
              type="button"
              :class="{ active: bottomTab === 'terminal' }"
              @click="bottomTab = 'terminal'"
            >
              TERMINAL
            </button>
            <button type="button" class="ml-auto" @click="showBottomPanel = false">
              <X :size="14" />
            </button>
          </div>
          <div class="vsc-bottom-content">
            <template v-if="bottomTab === 'problems'">
              <div class="vsc-problem">
                <AlertCircle :size="15" />
                <span>{{ pendingCount }} AI suggestions require review</span>
              </div>
              <button
                v-for="suggestion in suggestions.filter((item) => item.status === 'pending')"
                :key="suggestion.id"
                class="vsc-problem"
                type="button"
                @click="focusSuggestion(suggestion)"
              >
                <AlertCircle :size="15" />
                <span>{{ suggestion.title }} · {{ getFile(suggestion.fileId)?.name }}:{{ suggestion.line }}</span>
              </button>
              <div class="vsc-problem">
                <CheckCircle2 :size="15" />
                <span>{{ appliedCount }} suggestions accepted</span>
              </div>
            </template>
            <template v-else-if="bottomTab === 'output'">
              <div class="vsc-output-line">
                [agent] Inline suggestions rendered for {{ activeFileName }}
              </div>
              <div class="vsc-output-line">
                [mock] Last save: {{ savedAt }}
              </div>
            </template>
            <template v-else>
              <div class="vsc-terminal">
                <div class="vsc-terminal-lines">
                  <div v-for="line in terminalLines" :key="line">{{ line }}</div>
                </div>
                <label class="vsc-terminal-input">
                  <Triangle :size="11" />
                  <input
                    v-model="terminalCommand"
                    placeholder="Run mock command"
                    @keyup.enter="runTerminalCommand"
                  />
                </label>
              </div>
            </template>
          </div>
        </section>
      </main>

      <aside class="vsc-copilot">
        <div
          class="vsc-resizer vsc-resizer--copilot"
          @mousedown="startResize('copilot', $event)"
        />
        <div class="vsc-copilot-title">
          <span><Bot :size="16" /> SHINKOU AI</span>
          <strong>{{ pendingCount }}</strong>
        </div>
        <div class="vsc-copilot-toolbar">
          <button type="button" :disabled="!pendingCount" @click="applyAllPending">
            <Check :size="14" />
            Accept next
          </button>
          <button
            type="button"
            :disabled="!fileSuggestions.some((item) => item.status === 'pending')"
            @click="applyAllForCurrentFile"
          >
            <CheckCircle2 :size="14" />
            Accept file
          </button>
          <button
            type="button"
            :disabled="!fileSuggestions.some((item) => item.status === 'pending')"
            @click="rejectAllForCurrentFile"
          >
            <XCircle :size="14" />
            Reject file
          </button>
        </div>
        <div class="vsc-copilot-body">
          <article
            v-for="suggestion in suggestions"
            :key="suggestion.id"
            class="vsc-suggestion-card"
            :class="[
              { active: activeSuggestion?.id === suggestion.id },
              `is-${suggestion.status}`,
            ]"
          >
            <button type="button" @click="focusSuggestion(suggestion)">
              <div>
                <span>{{ statusLabel(suggestion.status) }}</span>
                <strong>{{ suggestion.title }}</strong>
              </div>
              <small>{{ suggestion.confidence }}%</small>
            </button>
            <p>{{ suggestion.reason }}</p>
            <div class="vsc-card-tags">
              <span v-for="item in suggestion.impact" :key="item">{{ item }}</span>
            </div>
            <div class="vsc-card-actions">
              <button
                type="button"
                :disabled="suggestion.status !== 'pending'"
                @click="rejectSuggestion(suggestion)"
              >
                <XCircle :size="14" />
                Reject
              </button>
              <button
                type="button"
                class="primary"
                :disabled="suggestion.status !== 'pending'"
                @click="applySuggestion(suggestion)"
              >
                <Check :size="14" />
                Accept
              </button>
            </div>
          </article>
        </div>
      </aside>
    </div>

    <div class="vsc-statusbar">
      <span><GitBranch :size="13" /> main</span>
      <span><CheckCircle2 :size="13" /> {{ appliedCount }} accepted</span>
      <span><XCircle :size="13" /> {{ rejectedCount }} rejected</span>
      <span class="ml-auto">{{ activeFileLanguage }}</span>
      <span>Spaces: 4</span>
      <button type="button" @click="showBottomPanel = !showBottomPanel">
        <PanelBottom :size="13" />
        Panel
      </button>
      <span><TerminalSquare :size="13" /> Mock</span>
    </div>
  </section>
</template>

<style>
.vsc-shell {
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #2b2b2b;
  border-radius: 0;
  background: #1e1e1e;
  color: #cccccc;
}

.vsc-titlebar {
  display: grid;
  height: 35px;
  flex: 0 0 auto;
  grid-template-columns: minmax(260px, 620px) auto;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #2b2b2b;
  background: #181818;
  padding: 0 10px;
  font-size: 12px;
}

.vsc-title-actions button,
.vsc-statusbar button {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border-radius: 4px;
  padding: 3px 7px;
  color: #cccccc;
}

.vsc-title-actions button:hover {
  background: #2a2d2e;
}

.vsc-command-center {
  display: flex;
  min-width: 0;
  height: 24px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #3c3c3c;
  border-radius: 5px;
  background: #242424;
  color: #a6a6a6;
}

.vsc-command-center input {
  min-width: 0;
  width: 100%;
  border: 0;
  background: transparent;
  color: #cccccc;
  font-size: 12px;
  text-align: center;
  outline: 0;
}

.vsc-title-actions {
  display: flex;
  justify-content: flex-end;
  gap: 2px;
}

.vsc-body {
  display: grid;
  min-height: 0;
  flex: 1;
  grid-template-columns: 48px 286px minmax(0, 1fr) 338px;
  overflow: hidden;
}

.vsc-activitybar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  border-right: 1px solid #2b2b2b;
  background: #181818;
  padding-top: 8px;
}

.vsc-activitybar button {
  position: relative;
  display: grid;
  width: 48px;
  height: 44px;
  place-items: center;
  border-left: 2px solid transparent;
  color: #858585;
}

.vsc-activitybar button.active {
  border-left-color: #ffffff;
  color: #ffffff;
}

.vsc-badge {
  position: absolute;
  right: 7px;
  top: 5px;
  display: grid;
  min-width: 16px;
  height: 16px;
  place-items: center;
  border-radius: 999px;
  background: #007acc;
  color: #fff;
  font-size: 10px;
  font-weight: 800;
}

.vsc-sidebar,
.vsc-copilot {
  position: relative;
  min-width: 0;
  overflow: hidden;
  border-right: 1px solid #2b2b2b;
  background: #252526;
}

.vsc-copilot {
  border-left: 1px solid #2b2b2b;
  border-right: 0;
}

.vsc-sidebar-title,
.vsc-copilot-title {
  display: flex;
  height: 35px;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #2b2b2b;
  padding: 0 12px;
  color: #bbbbbb;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.vsc-copilot-title span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.vsc-copilot-title strong {
  border-radius: 999px;
  background: #0e639c;
  padding: 1px 7px;
  color: #fff;
}

.vsc-copilot-toolbar {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 6px;
  border-bottom: 1px solid #2b2b2b;
  padding: 8px;
}

.vsc-copilot-toolbar button {
  display: inline-flex;
  height: 28px;
  align-items: center;
  justify-content: center;
  gap: 5px;
  border: 1px solid #3c3c3c;
  border-radius: 3px;
  color: #cccccc;
  font-size: 12px;
  font-weight: 800;
}

.vsc-copilot-toolbar button:hover:not(:disabled) {
  background: #2a2d2e;
}

.vsc-copilot-toolbar button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.vsc-panel-scroll,
.vsc-copilot-body {
  height: calc(100% - 35px);
  overflow: auto;
  padding: 8px;
  scrollbar-width: none;
}

.vsc-panel-scroll::-webkit-scrollbar,
.vsc-copilot-body::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.vsc-section-label {
  padding: 7px 4px;
  color: #bbbbbb;
  font-size: 11px;
  font-weight: 900;
}

.vsc-section-header {
  display: flex;
  height: 28px;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 0 2px 0 4px;
  color: #bbbbbb;
  font-size: 11px;
  font-weight: 900;
}

.vsc-section-header button {
  display: inline-flex;
  min-width: 0;
  align-items: center;
  gap: 5px;
  border-radius: 3px;
  padding: 3px 4px;
  color: inherit;
}

.vsc-section-header button:hover {
  background: #37373d;
}

.vsc-input-wrap {
  display: flex;
  height: 28px;
  align-items: center;
  gap: 7px;
  border: 1px solid #3c3c3c;
  border-radius: 2px;
  background: #1e1e1e;
  padding: 0 8px;
  color: #858585;
}

.vsc-input-wrap input {
  min-width: 0;
  flex: 1;
  border: 0;
  background: transparent;
  color: #cccccc;
  font-size: 12px;
  outline: 0;
}

.vsc-primary-btn {
  width: 100%;
  height: 28px;
  margin-top: 8px;
  border-radius: 2px;
  background: #0e639c;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.vsc-file-row,
.vsc-search-row,
.vsc-agent-row {
  display: flex;
  width: 100%;
  min-width: 0;
  align-items: center;
  gap: 7px;
  border-radius: 2px;
  padding: 5px 7px;
  color: #cccccc;
  font-size: 12px;
  text-align: left;
}

.vsc-file-row:hover,
.vsc-search-row:hover,
.vsc-agent-row:hover,
.vsc-file-row.active,
.vsc-search-row.active,
.vsc-agent-row.active {
  background: #37373d;
}

.vsc-file-row span {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vsc-tree-row svg:first-child {
  flex: 0 0 auto;
  color: #858585;
}

.vsc-tree-row.is-dir {
  font-weight: 700;
}

.vsc-file-row i,
.vsc-tab i {
  width: 7px;
  height: 7px;
  flex: 0 0 auto;
  border-radius: 999px;
  background: #d7ba7d;
}

.vsc-search-row,
.vsc-agent-row {
  display: block;
  margin-top: 6px;
}

.vsc-search-row strong,
.vsc-agent-row strong {
  display: block;
  overflow: hidden;
  color: #e8e8e8;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vsc-search-row span,
.vsc-agent-row span,
.vsc-agent-row small {
  display: block;
  margin-top: 3px;
  color: #969696;
  font-size: 11px;
}

.vsc-search-row code {
  display: block;
  overflow: hidden;
  margin-top: 5px;
  color: #ce9178;
  font-family:
    "JetBrains Mono", ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas,
    monospace;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vsc-empty {
  padding: 12px 6px;
  color: #858585;
  font-size: 12px;
}

.vsc-source-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  margin-top: 8px;
  padding: 0 4px;
}

.vsc-source-actions button {
  height: 27px;
  border: 1px solid #3c3c3c;
  border-radius: 3px;
  color: #cccccc;
  font-size: 12px;
  font-weight: 800;
}

.vsc-source-actions button:hover {
  background: #2a2d2e;
}

.vsc-agent-summary {
  display: grid;
  grid-template-columns: auto auto 1fr;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  border: 1px solid #3c3c3c;
  background: #1e1e1e;
  padding: 10px;
  color: #cccccc;
  font-size: 12px;
}

.vsc-agent-summary strong {
  color: #4fc1ff;
  font-size: 20px;
}

.vsc-agent-row.is-applied span,
.vsc-suggestion-card.is-applied span {
  color: #89d185;
}

.vsc-agent-row.is-rejected span,
.vsc-suggestion-card.is-rejected span {
  color: #f48771;
}

.vsc-workbench {
  display: flex;
  min-width: 0;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
  background: #1e1e1e;
}

.vsc-tabs {
  display: flex;
  height: 35px;
  flex: 0 0 auto;
  align-items: stretch;
  border-bottom: 1px solid #2b2b2b;
  background: #181818;
}

.vsc-tab {
  display: flex;
  min-width: 140px;
  max-width: 220px;
  align-items: center;
  gap: 7px;
  border-right: 1px solid #2b2b2b;
  background: #2d2d2d;
  padding: 0 10px;
  color: #969696;
  font-size: 12px;
}

.vsc-tab.active {
  background: #1e1e1e;
  color: #ffffff;
}

.vsc-tab span {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vsc-breadcrumbs {
  display: flex;
  height: 26px;
  flex: 0 0 auto;
  align-items: center;
  gap: 6px;
  overflow: hidden;
  border-bottom: 1px solid #252526;
  background: #1e1e1e;
  padding: 0 12px;
  color: #969696;
  font-size: 12px;
}

.vsc-breadcrumbs span:not(:last-child)::after {
  content: ">";
  margin-left: 6px;
  color: #6b6b6b;
}

.vsc-editor-wrap {
  display: grid;
  min-height: 0;
  flex: 1;
  overflow: hidden;
}

.vsc-editor-pane {
  position: relative;
  min-width: 0;
  min-height: 0;
}

.vsc-empty-editor {
  position: absolute;
  inset: 0;
  display: grid;
  place-content: center;
  gap: 12px;
  background: #1e1e1e;
  color: #858585;
  text-align: center;
  font-size: 13px;
}

.vsc-empty-editor button {
  border: 1px solid #3c3c3c;
  border-radius: 3px;
  padding: 6px 10px;
  color: #cccccc;
}

.vsc-empty-editor button:hover {
  background: #2a2d2e;
}

.vsc-bottom-panel {
  position: relative;
  height: 150px;
  flex: 0 0 auto;
  border-top: 1px solid #2b2b2b;
  background: #1e1e1e;
}

.vsc-resizer {
  position: absolute;
  z-index: 5;
  background: transparent;
}

.vsc-resizer:hover {
  background: #007acc;
}

.vsc-resizer--sidebar {
  right: -2px;
  top: 0;
  width: 4px;
  height: 100%;
  cursor: col-resize;
}

.vsc-resizer--copilot {
  left: -2px;
  top: 0;
  width: 4px;
  height: 100%;
  cursor: col-resize;
}

.vsc-resizer--bottom {
  left: 0;
  right: 0;
  top: -2px;
  height: 4px;
  cursor: row-resize;
}

.vsc-is-resizing,
.vsc-is-resizing * {
  cursor: col-resize !important;
  user-select: none !important;
}

.vsc-panel-tabs {
  display: flex;
  height: 32px;
  align-items: center;
  border-bottom: 1px solid #2b2b2b;
  padding: 0 8px;
}

.vsc-panel-tabs button {
  display: inline-flex;
  height: 100%;
  align-items: center;
  border-bottom: 1px solid transparent;
  padding: 0 9px;
  color: #969696;
  font-size: 11px;
  font-weight: 800;
}

.vsc-panel-tabs button.active {
  border-bottom-color: #ffffff;
  color: #ffffff;
}

.vsc-bottom-content {
  height: calc(100% - 32px);
  overflow: hidden;
  padding: 8px 12px;
  color: #cccccc;
  font-size: 12px;
}

.vsc-problem,
.vsc-output-line {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 3px 0;
  color: #cccccc;
  text-align: left;
}

.vsc-terminal {
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  gap: 6px;
  font-family:
    "JetBrains Mono", ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas,
    monospace;
}

.vsc-terminal-lines {
  min-height: 0;
  flex: 1;
  overflow: hidden;
  color: #cccccc;
}

.vsc-terminal-input {
  display: flex;
  height: 26px;
  align-items: center;
  gap: 6px;
  color: #89d185;
}

.vsc-terminal-input input {
  min-width: 0;
  flex: 1;
  border: 0;
  background: transparent;
  color: #cccccc;
  outline: 0;
}

.vsc-copilot-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: calc(100% - 80px);
  padding: 8px;
}

.vsc-suggestion-card {
  border: 1px solid #3c3c3c;
  border-radius: 4px;
  background: #1e1e1e;
  padding: 10px;
}

.vsc-suggestion-card.active {
  border-color: #007acc;
}

.vsc-suggestion-card > button {
  display: flex;
  width: 100%;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  text-align: left;
}

.vsc-suggestion-card strong {
  display: block;
  margin-top: 4px;
  color: #ffffff;
  font-size: 13px;
}

.vsc-suggestion-card small,
.vsc-suggestion-card span {
  color: #4fc1ff;
  font-size: 11px;
  font-weight: 800;
}

.vsc-suggestion-card p {
  margin-top: 8px;
  color: #bdbdbd;
  font-size: 12px;
  line-height: 1.5;
}

.vsc-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 8px;
}

.vsc-card-tags span {
  border-radius: 3px;
  background: #2d2d2d;
  padding: 2px 6px;
  color: #cccccc;
  font-size: 11px;
}

.vsc-card-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  margin-top: 10px;
}

.vsc-card-actions button {
  display: inline-flex;
  height: 28px;
  align-items: center;
  justify-content: center;
  gap: 5px;
  border: 1px solid #3c3c3c;
  border-radius: 3px;
  color: #cccccc;
  font-size: 12px;
  font-weight: 800;
}

.vsc-card-actions button.primary {
  border-color: #0e639c;
  background: #0e639c;
  color: #fff;
}

.vsc-card-actions button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.vsc-statusbar {
  display: flex;
  height: 22px;
  flex: 0 0 auto;
  align-items: center;
  gap: 14px;
  background: #007acc;
  padding: 0 8px;
  color: #ffffff;
  font-size: 11px;
}

.vsc-statusbar span,
.vsc-statusbar button {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #ffffff;
}

.vsc-line-reveal {
  background: rgba(55, 148, 255, 0.14);
}

.vsc-glyph-reveal {
  border-left: 3px solid #3794ff;
}

.vsc-ai-line--pending {
  background: rgba(206, 145, 120, 0.1);
}

.vsc-ai-line--applied {
  background: rgba(137, 209, 133, 0.08);
}

.vsc-ai-line--rejected {
  background: rgba(244, 135, 113, 0.08);
}

.vsc-ai-glyph {
  border-left: 3px solid #d7ba7d;
}

.vsc-ai-glyph--applied {
  border-left-color: #89d185;
}

.vsc-ai-glyph--rejected {
  border-left-color: #f48771;
}

.vsc-shell.theme-light {
  border-color: #d0d7de;
  background: #ffffff;
  color: #24292f;
}

.vsc-shell.theme-light .vsc-titlebar,
.vsc-shell.theme-light .vsc-activitybar,
.vsc-shell.theme-light .vsc-tabs {
  border-color: #d0d7de;
  background: #f3f4f6;
}

.vsc-shell.theme-light .vsc-title-actions button,
.vsc-shell.theme-light .vsc-statusbar button {
  color: #24292f;
}

.vsc-shell.theme-light .vsc-title-actions button:hover {
  background: #e5e7eb;
}

.vsc-shell.theme-light .vsc-command-center,
.vsc-shell.theme-light .vsc-input-wrap,
.vsc-shell.theme-light .vsc-agent-summary,
.vsc-shell.theme-light .vsc-suggestion-card {
  border-color: #d0d7de;
  background: #ffffff;
  color: #57606a;
}

.vsc-shell.theme-light .vsc-command-center input,
.vsc-shell.theme-light .vsc-input-wrap input,
.vsc-shell.theme-light .vsc-terminal-input input {
  color: #24292f;
}

.vsc-shell.theme-light .vsc-sidebar,
.vsc-shell.theme-light .vsc-copilot {
  border-color: #d0d7de;
  background: #f6f8fa;
}

.vsc-shell.theme-light .vsc-sidebar-title,
.vsc-shell.theme-light .vsc-copilot-title,
.vsc-shell.theme-light .vsc-copilot-toolbar,
.vsc-shell.theme-light .vsc-breadcrumbs,
.vsc-shell.theme-light .vsc-bottom-panel,
.vsc-shell.theme-light .vsc-panel-tabs {
  border-color: #d0d7de;
  background: #f6f8fa;
  color: #57606a;
}

.vsc-shell.theme-light .vsc-workbench,
.vsc-shell.theme-light .vsc-editor-wrap,
.vsc-shell.theme-light .vsc-bottom-content {
  background: #ffffff;
  color: #24292f;
}

.vsc-shell.theme-light .vsc-problem,
.vsc-shell.theme-light .vsc-output-line {
  color: #24292f;
}

.vsc-shell.theme-light .vsc-tab {
  border-color: #d0d7de;
  background: #eaeef2;
  color: #57606a;
}

.vsc-shell.theme-light .vsc-tab.active {
  background: #ffffff;
  color: #24292f;
}

.vsc-shell.theme-light .vsc-file-row,
.vsc-shell.theme-light .vsc-search-row,
.vsc-shell.theme-light .vsc-agent-row {
  color: #24292f;
}

.vsc-shell.theme-light .vsc-file-row:hover,
.vsc-shell.theme-light .vsc-search-row:hover,
.vsc-shell.theme-light .vsc-agent-row:hover,
.vsc-shell.theme-light .vsc-file-row.active,
.vsc-shell.theme-light .vsc-search-row.active,
.vsc-shell.theme-light .vsc-agent-row.active {
  background: #eaeef2;
}

.vsc-shell.theme-light .vsc-search-row strong,
.vsc-shell.theme-light .vsc-agent-row strong,
.vsc-shell.theme-light .vsc-suggestion-card strong {
  color: #24292f;
}

.vsc-shell.theme-light .vsc-search-row span,
.vsc-shell.theme-light .vsc-agent-row span,
.vsc-shell.theme-light .vsc-agent-row small,
.vsc-shell.theme-light .vsc-suggestion-card p,
.vsc-shell.theme-light .vsc-section-label,
.vsc-shell.theme-light .vsc-section-header,
.vsc-shell.theme-light .vsc-empty {
  color: #57606a;
}

.vsc-shell.theme-light .vsc-section-header button:hover,
.vsc-shell.theme-light .vsc-empty-editor button:hover {
  background: #eaeef2;
}

.vsc-shell.theme-light .vsc-card-tags span {
  background: #eaeef2;
  color: #57606a;
}

.vsc-shell.theme-light .vsc-copilot-toolbar button,
.vsc-shell.theme-light .vsc-source-actions button,
.vsc-shell.theme-light .vsc-card-actions button {
  border-color: #d0d7de;
  color: #24292f;
}

.vsc-shell.theme-light .vsc-terminal-lines {
  color: #24292f;
}

.vsc-shell.theme-light .vsc-empty-editor {
  background: #ffffff;
  color: #57606a;
}

.vsc-shell.theme-light .vsc-empty-editor button {
  border-color: #d0d7de;
  color: #24292f;
}

</style>
