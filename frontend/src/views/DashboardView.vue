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

    <!-- 图表行 -->
    <el-row :gutter="12">
      <el-col :span="12">
        <el-card>
          <template #header><span>DNT 趋势（按天）</span></template>
          <v-chart :option="dntChartOption" style="height:320px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>月溶栓率</span></template>
          <v-chart :option="monthlyChartOption" style="height:320px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { dashboardApi } from '../api/index.js'

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

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

const dntChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['DNT中位数', 'DNT P95'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: dntTrend.value.map(d => d.date || ''), axisLabel: { rotate: 30 } },
  yAxis: { type: 'value', name: '分钟' },
  series: [
    { name: 'DNT中位数', type: 'line', data: dntTrend.value.map(d => d.dntMedian || 0), smooth: true,
      itemStyle: { color: '#409eff' }, areaStyle: { color: 'rgba(64,158,255,0.1)' } },
    { name: 'DNT P95', type: 'line', data: dntTrend.value.map(d => d.dntP95 || 0), smooth: true,
      itemStyle: { color: '#f56c6c' }, areaStyle: { color: 'rgba(245,108,108,0.1)' } }
  ]
}))

const monthlyChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['溶栓率', 'DNT超时率'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: monthlyRate.value.map(d => d.month || '') },
  yAxis: { type: 'value', name: '百分比', axisLabel: { formatter: '{value}%' } },
  series: [
    { name: '溶栓率', type: 'bar', data: monthlyRate.value.map(d => d.thrombolysisRate || 0),
      itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] } },
    { name: 'DNT超时率', type: 'bar', data: monthlyRate.value.map(d => d.dntTimeoutRate || 0),
      itemStyle: { color: '#e6a23c', borderRadius: [4, 4, 0, 0] } }
  ]
}))

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
