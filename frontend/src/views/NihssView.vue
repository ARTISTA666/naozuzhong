<template>
  <div>
    <el-row :gutter="16">
      <!-- NIHSS 提交 -->
      <el-col :span="14">
        <el-card>
          <template #header><span>NIHSS 评分提交</span></template>
          <el-form ref="formRef" :model="nihssForm" :rules="formRules" label-width="120px">
            <el-form-item label="就诊ID" prop="encounterId">
              <el-input v-model="nihssForm.encounterId" placeholder="输入就诊ID" />
            </el-form-item>
            <el-form-item label="患者ID" prop="patientId">
              <el-input v-model="nihssForm.patientId" placeholder="输入患者ID" />
            </el-form-item>
            <el-form-item label="来源">
              <el-radio-group v-model="nihssForm.source">
                <el-radio value="ONLINE">在线</el-radio>
                <el-radio value="OFFLINE">离线</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-divider>NIHSS 15项评分</el-divider>

            <el-row :gutter="12">
              <el-col v-for="item in scaleItems" :key="item.itemCode" :span="8" style="margin-bottom:8px">
                <el-form-item :label="item.itemCode" :label-width="'50px'">
                  <el-select v-model="scores[item.itemCode]" :placeholder="item.itemName" clearable style="width:100%">
                    <el-option v-for="s in item.maxScore - item.minScore + 1" :key="s-1"
                               :value="s-1" :label="`${s-1}分 - ${item.description?.split('，')[0] || ''}`" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-button type="primary" @click="submitNihss" style="margin-top:12px">提交评分</el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- 查询结果 -->
      <el-col :span="10">
        <el-card style="margin-bottom:16px">
          <template #header>
            <div style="display:flex; justify-content:space-between">
              <span>评估记录</span>
              <el-button size="small" @click="loadNihssHistory">刷新</el-button>
            </div>
          </template>
          <el-input v-model="historyQuery.encounterId" placeholder="输入就诊ID查询" style="margin-bottom:12px">
            <template #append>
              <el-button @click="loadNihssHistory">查询</el-button>
            </template>
          </el-input>

          <el-timeline v-if="nihssHistory.length > 0">
            <el-timeline-item v-for="record in nihssHistory" :key="record.assessmentId"
                              :timestamp="record.assessmentTime" placement="top"
                              :color="severityColor(record.totalScore)">
              <el-tag size="small" :type="severityTag(record.totalScore)">
                v{{ record.versionNo }} 总分: {{ record.totalScore }}
              </el-tag>
              <span style="margin-left:8px; color:#999">{{ record.severityLevel }}</span>
              <span style="margin-left:8px; color:#999">{{ record.operatorName }}</span>
              <el-tag v-if="record.source === 'OFFLINE'" size="small" type="warning" style="margin-left:8px">离线</el-tag>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无评估记录" />
        </el-card>

        <!-- 趋势图 -->
        <el-card v-if="nihssHistory.length >= 2">
          <template #header><span>评分趋势</span></template>
          <v-chart :option="trendChartOption" style="height:240px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, MarkLineComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { assessmentApi } from '../api/index.js'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, MarkLineComponent])

const nihssForm = reactive({ encounterId: '', patientId: '', source: 'ONLINE' })
const scores = reactive({})
const scaleItems = ref([])
const nihssHistory = ref([])
const historyQuery = reactive({ encounterId: '' })
const formRef = ref(null)
const formRules = {
  encounterId: [{ required: true, message: '请输入就诊ID', trigger: 'blur' }],
  patientId: [{ required: true, message: '请输入患者ID', trigger: 'blur' }]
}

const trendChartOption = computed(() => {
  const data = nihssHistory.value
  return {
    tooltip: { trigger: 'axis', formatter: p => `版本 v${p[0].dataIndex + 1}<br/>总分: ${p[0].value}<br/>${p[0].axisValue}` },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => `v${d.versionNo}`) },
    yAxis: { type: 'value', name: '总分', min: 0, max: 42 },
    series: [{
      type: 'line', data: data.map(d => d.totalScore),
      smooth: true, showSymbol: true, symbolSize: 8,
      lineStyle: { width: 2 },
      markLine: {
        silent: true,
        data: [
          { yAxis: 5, label: { formatter: '轻度 5' }, lineStyle: { color: '#e6a23c', type: 'dashed' } },
          { yAxis: 15, label: { formatter: '中度 15' }, lineStyle: { color: '#f56c6c', type: 'dashed' } },
          { yAxis: 21, label: { formatter: '重度 21' }, lineStyle: { color: '#c03636', type: 'dashed' } }
        ]
      },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(245,108,108,0.3)' },
            { offset: 0.5, color: 'rgba(230,162,60,0.2)' },
            { offset: 1, color: 'rgba(103,194,58,0.1)' }
          ]
        }
      }
    }]
  }
})

onMounted(async () => {
  try {
    const res = await assessmentApi.getScaleItems()
    scaleItems.value = res.data
    scaleItems.value.forEach(item => { scores[item.itemCode] = null })
  } catch (e) { /* ignore */ }
})

async function submitNihss() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const filledScores = {}
  let hasScore = false
  for (const [k, v] of Object.entries(scores)) {
    if (v !== null && v !== '') { filledScores[k] = v; hasScore = true }
  }
  if (!hasScore) { ElMessage.warning('请至少填写一项评分'); return }
  try {
    await assessmentApi.submitNihss({
      encounterId: parseInt(nihssForm.encounterId),
      patientId: parseInt(nihssForm.patientId),
      scores: filledScores,
      source: nihssForm.source
    })
    ElMessage.success('评分提交成功')
    historyQuery.encounterId = nihssForm.encounterId
    await loadNihssHistory()
  } catch (e) { ElMessage.error('提交失败') }
}

async function loadNihssHistory() {
  if (!historyQuery.encounterId) return
  try {
    const res = await assessmentApi.getHistory(historyQuery.encounterId)
    nihssHistory.value = res.data
  } catch (e) { /* ignore */ }
}

function severityColor(score) {
  if (score <= 4) return '#67c23a'
  if (score <= 15) return '#e6a23c'
  return '#f56c6c'
}

function severityTag(score) {
  if (score <= 4) return 'success'
  if (score <= 15) return 'warning'
  return 'danger'
}
</script>
