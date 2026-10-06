<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['changed'])
const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const form = ref({ plate: '', model: '', capacity: null, status: '可用', lastMaintain: '' })
const statusOptions = ['可用', '维修', '停用']

async function load() {
  loading.value = true
  try {
    list.value = (await api.listVehicles()) || []
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { plate: '', model: '', capacity: null, status: '可用', lastMaintain: '' }
  dialog.value = true
}

async function save() {
  saving.value = true
  try {
    await api.createVehicle({ ...form.value })
    ElMessage.success('运钞车已登记')
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
    await api.transferVehicle(row.id, status)
    ElMessage.success('状态已更新')
    await load()
    emit('changed')
  } catch (e) {
    ElMessage.error(e.message)
    await load()
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <h2>运钞车队</h2>
      <el-button type="primary" @click="openCreate">登记运钞车</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="plate" label="车牌" width="120" />
      <el-table-column prop="model" label="车型" />
      <el-table-column prop="capacity" label="容量(万元)" width="120" />
      <el-table-column prop="lastMaintain" label="上次保养" width="130" />
      <el-table-column label="状态" width="200">
        <template #default="{ row }">
          <el-select :model-value="row.status" size="small" @change="(v) => changeStatus(row, v)">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="登记运钞车" width="440px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="车牌"><el-input v-model="form.plate" placeholder="如 京A·0005" /></el-form-item>
        <el-form-item label="车型"><el-input v-model="form.model" placeholder="如 依维柯运钞车" /></el-form-item>
        <el-form-item label="容量(万元)"><el-input-number v-model="form.capacity" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="上次保养">
          <el-date-picker v-model="form.lastMaintain" type="date" value-format="YYYY-MM-DD" style="width:100%" />
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
</style>
