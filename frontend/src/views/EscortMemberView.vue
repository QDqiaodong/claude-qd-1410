<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['changed'])
const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const form = ref({ name: '', certNo: '', level: '', status: '在岗', joinDate: '' })
const statusOptions = ['在岗', '休班', '停用']

async function load() {
  loading.value = true
  try {
    list.value = (await api.listMembers()) || []
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { name: '', certNo: '', level: '', status: '在岗', joinDate: '' }
  dialog.value = true
}

async function save() {
  saving.value = true
  try {
    await api.createMember({ ...form.value })
    ElMessage.success('押运员已录入')
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
    await api.transferMember(row.id, status)
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
      <h2>押运人员</h2>
      <el-button type="primary" @click="openCreate">录入押运员</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="姓名" width="110" />
      <el-table-column prop="certNo" label="持枪证号" width="130" />
      <el-table-column prop="level" label="资质等级" width="110" />
      <el-table-column prop="joinDate" label="入队日期" width="130" />
      <el-table-column label="状态" width="200">
        <template #default="{ row }">
          <el-select :model-value="row.status" size="small" @change="(v) => changeStatus(row, v)">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="录入押运员" width="440px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="持枪证号"><el-input v-model="form.certNo" placeholder="如 GA11005" /></el-form-item>
        <el-form-item label="资质等级"><el-input v-model="form.level" placeholder="如 一级" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="入队日期">
          <el-date-picker v-model="form.joinDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
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
