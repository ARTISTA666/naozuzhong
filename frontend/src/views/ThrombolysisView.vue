<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card>
        <template #header><span>溶栓禁忌检查</span></template>
        <el-form :model="form" label-width="140px">
          <el-divider>时间窗</el-divider>
          <el-form-item label="发病时间">
            <el-date-picker v-model="form.onsetTime" type="datetime" placeholder="选择发病时间" style="width:100%" />
          </el-form-item>

          <el-divider>检验结果</el-divider>
          <el-form-item label="INR"><el-input-number v-model="form.inr" :min="0" :max="10" :step="0.1" style="width:100%" /></el-form-item>
          <el-form-item label="血小板 (×10⁹/L)"><el-input-number v-model="form.plateletCount" :min="0" :max="500" style="width:100%" /></el-form-item>
          <el-form-item label="血糖 (mmol/L)"><el-input-number v-model="form.bloodGlucose" :min="0" :max="30" :step="0.1" style="width:100%" /></el-form-item>

          <el-divider>生命体征</el-divider>
          <el-row :gutter="12">
            <el-col :span="12"><el-form-item label="收缩压"><el-input-number v-model="form.systolicBp" :min="60" :max="250" style="width:100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="舒张压"><el-input-number v-model="form.diastolicBp" :min="40" :max="150" style="width:100%" /></el-form-item></el-col>
          </el-row>

          <el-divider>病史</el-divider>
          <el-form-item label="近期大手术(3周内)">
            <el-switch v-model="form.recentMajorSurgery" />
          </el-form-item>
          <el-form-item label="颅内出血史">
            <el-switch v-model="form.intracranialHemorrhageHistory" />
          </el-form-item>

          <el-button type="primary" @click="check" :loading="loading" style="margin-top:12px">执行检查</el-button>
        </el-form>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-card v-if="result">
        <template #header>
          <span>检查结果</span>
          <el-tag :type="result.conclusion === 'ELIGIBLE' ? 'success' : result.conclusion === 'CAUTION' ? 'warning' : 'danger'"
                  style="margin-left:12px" size="large">
            {{ result.conclusion === 'ELIGIBLE' ? '建议溶栓' : result.conclusion === 'CAUTION' ? '谨慎评估' : '不建议溶栓' }}
          </el-tag>
        </template>

        <p>{{ result.message }}</p>

        <el-table :data="result.items" stripe style="width:100%">
          <el-table-column prop="ruleName" label="规则" width="140" />
          <el-table-column label="严重程度" width="100">
            <template #default="{ row }">
              <el-tag :type="row.severity === 'ABSOLUTE' ? 'danger' : 'warning'" size="small">
                {{ row.severity === 'ABSOLUTE' ? '绝对' : '相对' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="触发" width="70">
            <template #default="{ row }">
              <el-icon v-if="row.triggered" color="#f56c6c"><CloseBold /></el-icon>
              <el-icon v-else color="#67c23a"><Check /></el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { decisionApi } from '../api/index.js'

const form = reactive({
  onsetTime: null, inr: null, plateletCount: null, bloodGlucose: null,
  systolicBp: null, diastolicBp: null,
  recentMajorSurgery: false, intracranialHemorrhageHistory: false
})
const result = ref(null)
const loading = ref(false)

async function check() {
  loading.value = true
  try {
    const res = await decisionApi.checkThrombolysis({
      onsetTime: form.onsetTime,
      inr: form.inr, plateletCount: form.plateletCount,
      bloodGlucose: form.bloodGlucose,
      systolicBp: form.systolicBp, diastolicBp: form.diastolicBp,
      recentMajorSurgery: form.recentMajorSurgery,
      intracranialHemorrhageHistory: form.intracranialHemorrhageHistory
    })
    result.value = res.data
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}
</script>
