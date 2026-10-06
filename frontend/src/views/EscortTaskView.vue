<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const props = defineProps({ taskId: { default: null } })
const emit = defineEmits(['changed'])

const tasks = ref([])
const routes = ref([])
const members = ref([])
const vehicles = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const form = ref({ taskNo: '', routeId: null, startSite: '', endSite: '', amount: null, memberId: null, vehicleId: null, planDate: '' })

async function load() {
  loading.value = true
  try {
    const [t, r, m, v] = await Promise.all([
      api.listTasks(), api.listRoutes(), api.listMembers(), api.listVehicles()
    ])
    tasks.value = t || []
    routes.value = r || []
    members.value = m || []
    vehicles.value = v || []
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

const selectedTask = computed(() => tasks.value.find(t => t.id === props.taskId) || null)
const routeMap = computed(() => {
  const m = {}
  routes.value.forEach(r => { m[r.id] = r })
  return m
})

// 押运线路地图（SVG 线路 + 网点节点），按线路风险等级着色
const map = computed(() => {
  const t = selectedTask.value
  const risk = t ? (routeMap.value[t.routeId]?.riskLevel || '低') : '低'
  const color = risk === '高' ? '#F56C6C' : risk === '中' ? '#E6A23C' : '#67C23A'
  return { t, color, risk }
})

const statusOptions = ['待派车', '执行中', '已完成', '异常']

async function openCreate() {
  form.value = { taskNo: '', routeId: null, startSite: '', endSite: '', amount: null, memberId: null, vehicleId: null, planDate: '' }
  dialog.value = true
}

async function save() {
  saving.value = true
  try {
    await api.createTask({ ...form.value })
    ElMessage.success('押运任务已创建')
    dialog.value = false
    await load()
    emit('changed')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function changeStatus(row, status) {
  try {
    await api.transferTask(row.id, status)
    ElMessage.success('状态已更新')
    await load()
    emit('changed')
  } catch (e) {
    ElMessage.error(e.message)
    await load()
  }
}

watch(() => props.taskId, () => {})
onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <h2>押运任务</h2>
      <el-button type="primary" @click="openCreate">新建押运任务</el-button>
    </div>

    <div class="map-card">
      <div class="map-title">
        押运线路地图
        <span class="map-meta" v-if="map.t">
          {{ map.t.taskNo }} · 风险等级：<b :style="{ color: map.color }">{{ map.risk }}</b>
        </span>
      </div>
      <svg viewBox="0 0 480 170" class="map-svg">
        <line x1="60" y1="85" x2="420" y2="85" :stroke="map.color" stroke-width="4" stroke-dasharray="9 7" />
        <circle cx="60" cy="85" r="11" :fill="map.color" />
        <circle cx="420" cy="85" r="11" :fill="map.color" />
        <text x="60" y="122" text-anchor="middle" class="node-label">{{ map.t ? map.t.startSite : '起点网点' }}</text>
        <text x="420" y="122" text-anchor="middle" class="node-label">{{ map.t ? map.t.endSite : '终点网点' }}</text>
        <text x="240" y="62" text-anchor="middle" class="route-label">{{ map.t ? map.t.taskNo : '请在左侧选择任务查看线路' }}</text>
      </svg>
    </div>

    <el-table :data="tasks" v-loading="loading" border stripe>
      <el-table-column prop="taskNo" label="任务编号" width="110" />
      <el-table-column prop="startSite" label="起点网点" />
      <el-table-column prop="endSite" label="终点网点" />
      <el-table-column prop="amount" label="押运金额(元)" width="130" />
      <el-table-column label="状态" width="200">
        <template #default="{ row }">
          <el-select :model-value="row.status" size="small" @change="(v) => changeStatus(row, v)">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="planDate" label="计划日期" width="130" />
    </el-table>

    <el-dialog v-model="dialog" title="新建押运任务" width="460px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="任务编号"><el-input v-model="form.taskNo" placeholder="如 T0007" /></el-form-item>
        <el-form-item label="押运线路">
          <el-select v-model="form.routeId" placeholder="选择线路" style="width:100%">
            <el-option v-for="r in routes" :key="r.id" :label="r.name + '（' + r.status + '）'" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="起点网点"><el-input v-model="form.startSite" /></el-form-item>
        <el-form-item label="终点网点"><el-input v-model="form.endSite" /></el-form-item>
        <el-form-item label="押运金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="押运员">
          <el-select v-model="form.memberId" placeholder="可选（在岗）" style="width:100%" clearable>
            <el-option v-for="m in members.filter(x => x.status === '在岗')" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="运钞车">
          <el-select v-model="form.vehicleId" placeholder="可选（可用）" style="width:100%" clearable>
            <el-option v-for="v in vehicles.filter(x => x.status === '可用')" :key="v.id" :label="v.plate" :value="v.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-head h2 { margin: 0; font-size: 18px; }
.map-card { background: #fff; border: 1px solid var(--line); border-radius: 10px; padding: 14px 18px; margin-bottom: 18px; }
.map-title { font-weight: 600; margin-bottom: 8px; }
.map-meta { font-size: 13px; color: var(--muted); margin-left: 10px; font-weight: 400; }
.map-svg { width: 100%; height: 170px; }
.node-label { font-size: 13px; fill: var(--ink); }
.route-label { font-size: 14px; fill: var(--primary); font-weight: 600; }
</style>
