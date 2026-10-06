<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['changed'])
const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const form = ref({ name: '', startPoint: '', endPoint: '', distance: null, riskLevel: '低', status: '启用' })
const statusOptions = ['启用', '停用']
const riskOptions = ['低', '中', '高']

async function load() {
  loading.value = true
  try {
    list.value = (await api.listRoutes()) || []
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { name: '', startPoint: '', endPoint: '', distance: null, riskLevel: '低', status: '启用' }
  dialog.value = true
}

async function save() {
  saving.value = true
  try {
    await api.createRoute({ ...form.value })
    ElMessage.success('押运线路已添加')
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
    await api.transferRoute(row.id, status)
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
      <h2>押运线路</h2>
      <el-button type="primary" @click="openCreate">添加线路</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="线路名称" width="140" />
      <el-table-column prop="startPoint" label="起点" />
      <el-table-column prop="endPoint" label="终点" />
      <el-table-column prop="distance" label="里程(km)" width="100" />
      <el-table-column prop="riskLevel" label="风险等级" width="100" />
      <el-table-column label="状态" width="200">
        <template #default="{ row }">
          <el-select :model-value="row.status" size="small" @change="(v) => changeStatus(row, v)">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="添加押运线路" width="440px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="线路名称"><el-input v-model="form.name" placeholder="如 城东支行环线" /></el-form-item>
        <el-form-item label="起点"><el-input v-model="form.startPoint" /></el-form-item>
        <el-form-item label="终点"><el-input v-model="form.endPoint" /></el-form-item>
        <el-form-item label="里程(km)"><el-input-number v-model="form.distance" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="form.riskLevel" style="width:100%">
            <el-option v-for="r in riskOptions" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
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
</style>
