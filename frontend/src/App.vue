<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import router from './router'
import api from './api'

import EscortTaskView from './views/EscortTaskView.vue'
import CashVehicleView from './views/CashVehicleView.vue'
import EscortMemberView from './views/EscortMemberView.vue'
import RouteView from './views/RouteView.vue'

// ------------------------------------------------------------------
// 导航形态：顶部模块切换（押运任务 / 运钞车队 / 押运人员 / 押运线路）
//           + 左侧常驻押运任务列表；主区按当前模块渲染对应视图（<component :is>）。
// 页面操作范式：押运线路地图（SVG 线路 + 网点节点），由任务视图呈现。
// ------------------------------------------------------------------

const modules = [
  { path: '/tasks', label: '押运任务', comp: EscortTaskView },
  { path: '/vehicles', label: '运钞车队', comp: CashVehicleView },
  { path: '/members', label: '押运人员', comp: EscortMemberView },
  { path: '/routes', label: '押运线路', comp: RouteView }
]

const currentPath = ref(router.currentRoute.value.path)
const currentView = computed(() => modules.find(m => m.path === currentPath.value)?.comp || EscortTaskView)

const tasks = ref([])
const selectedId = ref(null)

async function loadTasks() {
  try {
    const list = await api.listTasks()
    tasks.value = list || []
    if (selectedId.value == null && tasks.value.length > 0) {
      selectedId.value = tasks.value[0].id
    }
  } catch (e) {
    tasks.value = []
  }
}

function onSelectTask(t) {
  selectedId.value = t.id
}

function onTopSelect(index) {
  const path = modules[Number(index)].path
  if (path !== currentPath.value) {
    router.push(path)
  }
}

watch(() => router.currentRoute.value.path, (p) => { currentPath.value = p })
router.afterEach((to) => { currentPath.value = to.path })

onMounted(async () => {
  currentPath.value = router.currentRoute.value.path
  await loadTasks()
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">
        <span class="brand-mark">押</span>
        <span class="brand-title">金融押运公司</span>
      </div>
      <el-menu mode="horizontal" :default-active="String(modules.findIndex(m => m.path === currentPath))"
               @select="onTopSelect" class="topnav">
        <el-menu-item v-for="(m, i) in modules" :key="m.path" :index="String(i)">{{ m.label }}</el-menu-item>
      </el-menu>
    </header>

    <div class="body">
      <aside class="sidebar">
        <div class="side-title">押运任务列表</div>
        <ul class="task-list">
          <li v-for="t in tasks" :key="t.id" :class="{ active: t.id === selectedId }" @click="onSelectTask(t)">
            <div class="t-row">
              <span class="t-no">{{ t.taskNo }}</span>
              <span class="tag" :class="'st-' + t.status">{{ t.status }}</span>
            </div>
            <div class="t-sub">{{ t.startSite }} → {{ t.endSite }}</div>
          </li>
          <li v-if="tasks.length === 0" class="empty">暂无押运任务</li>
        </ul>
      </aside>

      <main class="content">
        <component :is="currentView" :task-id="selectedId" @changed="loadTasks" />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; display: flex; flex-direction: column; }

.topbar {
  display: flex; align-items: center; gap: 24px;
  padding: 0 20px; height: 56px; background: #fff; border-bottom: 1px solid var(--line);
}
.brand { display: flex; align-items: center; gap: 10px; }
.brand-mark {
  width: 34px; height: 34px; border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  background: var(--primary); color: #fff; font-weight: 700; font-size: 17px;
}
.brand-title { font-size: 16px; font-weight: 600; }
.topnav { flex: 1; border-bottom: none; }

.body { flex: 1; display: flex; min-height: 0; }

.sidebar {
  width: 260px; flex-shrink: 0; background: #fff; border-right: 1px solid var(--line);
  display: flex; flex-direction: column;
}
.side-title { padding: 14px 18px 8px; font-size: 13px; color: var(--muted); font-weight: 600; }
.task-list { list-style: none; margin: 0; padding: 0 10px 12px; overflow: auto; flex: 1; }
.task-list li {
  padding: 10px 12px; border-radius: 8px; cursor: pointer; margin-bottom: 6px;
  border: 1px solid transparent;
}
.task-list li:hover { background: var(--primary-wash); }
.task-list li.active { background: var(--primary-wash); border-color: var(--primary); }
.t-row { display: flex; align-items: center; justify-content: space-between; }
.t-no { font-weight: 600; }
.t-sub { font-size: 12px; color: var(--muted); margin-top: 2px; }
.empty { color: var(--muted); font-size: 13px; padding: 12px; }

.tag { font-size: 12px; padding: 1px 8px; border-radius: 10px; color: #fff; }
.st-待派车 { background: #909399; }
.st-执行中 { background: var(--primary); }
.st-已完成 { background: #67C23A; }
.st-异常 { background: #F56C6C; }

.content { flex: 1; padding: 20px 24px 28px; overflow: auto; }
</style>
