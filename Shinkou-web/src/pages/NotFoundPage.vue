<template>
  <main class="grid min-h-screen place-items-center bg-slate-50 p-8">
    <section class="card max-w-lg p-10 text-center">
      <div class="text-7xl font-black text-brand-600">404</div>
      <h1 class="mt-4 text-2xl font-black">页面不存在</h1>
      <p class="mt-2 text-slate-500">
        当前地址没有对应页面，请返回工作区或项目列表。
      </p>
      <RouterLink class="btn btn-primary mt-6" to="/workspace-select"
        >返回工作区选择</RouterLink
      >
    </section>
  </main>
</template>
