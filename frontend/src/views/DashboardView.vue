<template>
  <div>
    <!-- 指标卡片 -->
    <el-row :gutter="12" style="margin-bottom:16px">
      <el-col v-for="indicator in indicators" :key="indicator.code" :span="6" style="margin-bottom:12px">
        <el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div style="display:flex; justify-content:space-between">
            <div>
              <div style="font-size:12px; color:#999">{{ indicator.displayName }}</div>
              <div style="font-size:24px; font-weight:bold; margin-top:4px">
                {{ formatValue(indicator.value, indicator.unit) }}
              </div>
              <div style="font-size:12px; color:#999; margin-top:4px">{{ indicator.description }}</div>
            </div>
            <el-tag :type="indicatorTypeColor(indicator.metricType)" size="small" effect="dark">
              {{ indicator.metricType }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图 (使用简单表格替代echarts以减少依赖) -->
    <el-row :gutter="12">
      <el-col :span="12">
        <el-card>
          <template #header><span>DNT 趋势（按天）</span></template>
          <el-table :data="dntTrend" stripe height="300" size="small">
            <el-table-column prop="date" label="日期" />
            <el-table-column prop="dntMedian" label="DNT中位数" />
            <el-table-column prop="dntP95" label="DNT P95" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>月溶栓率</span></template>
          <el-table :data="monthlyRate" stripe height="300" size="small">
            <el-table-column prop="month" label="月份" />
            <el-table-column prop="thrombolysisRate" label="溶栓率(%)">
              <template #default="{ row }">{{ row.thrombolysisRate }}%</template>
            </el-table-column>
            <el-table-column prop="dntTimeoutRate" label="DNT超时率(%)">
              <template #default="{ row }">{{ row.dntTimeoutRate }}%</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dashboardApi } from '../api/index.js'

const indicators = ref([])
const dntTrend = ref([])
const monthlyRate = ref([])

onMounted(async () => {
  try {
    const [indRes, trendRes, rateRes] = await Promise.all([
      dashboardApi.getOverview(),
      dashboardApi.getDntTrend(),
      dashboardApi.getMonthlyRate()
    ])
    indicators.value = indRes.data || []
    dntTrend.value = trendRes.data || []
    monthlyRate.value = rateRes.data || []
  } catch (e) { console.error(e) }
})

function formatValue(value, unit) {
  if (value === undefined || value === null) return '-'
  if (unit === '%') return value.toFixed(1) + '%'
  if (unit === '分钟') return value.toFixed(0) + 'min'
  if (unit === '分') return value.toFixed(1)
  return value
}

function indicatorTypeColor(type) {
  if (type === 'TIME') return 'primary'
  if (type === 'RATE') return 'success'
  return 'warning'
}
</script>
