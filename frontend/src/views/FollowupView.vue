<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card>
        <template #header><span>随访计划</span></template>
        <el-input v-model="patientId" placeholder="患者ID" style="margin-bottom:12px">
          <template #append><el-button @click="loadPlans">查询</el-button></template>
        </el-input>
        <el-table :data="plans" stripe style="width:100%">
          <el-table-column prop="planName" label="计划名称" />
          <el-table-column prop="planType" label="类型" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button size="small" @click="loadTasks(row.id)">任务</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-card>
        <template #header><span>随访任务</span></template>
        <el-table :data="tasks" stripe style="width:100%">
          <el-table-column prop="taskName" label="任务" />
          <el-table-column prop="plannedDate" label="计划日期" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="tasks.length === 0" description="选择计划查看任务" />
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref } from 'vue'
import { followupApi } from '../api/index.js'

const patientId = ref('')
const plans = ref([])
const tasks = ref([])

async function loadPlans() {
  if (!patientId.value) return
  try {
    const res = await followupApi.getPlans(patientId.value)
    plans.value = res.data || []
  } catch (e) { console.error(e) }
}

async function loadTasks(planId) {
  try {
    const res = await followupApi.getTasks(planId)
    tasks.value = res.data || []
  } catch (e) { console.error(e) }
}
</script>
