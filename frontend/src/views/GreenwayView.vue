<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center">
          <span>绿道流程管理</span>
          <el-button type="primary" @click="showCreateDialog = true">创建绿道</el-button>
        </div>
      </template>

      <!-- 查询 -->
      <el-form :inline="true" :model="queryForm" style="margin-bottom:16px">
        <el-form-item label="就诊ID">
          <el-input v-model="queryForm.encounterId" placeholder="输入就诊ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadGreenway">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 绿道详情 -->
      <el-card v-if="greenway" shadow="hover" style="margin-top:12px">
        <el-descriptions title="绿道详情" :column="2" border>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(greenway.status)">{{ greenway.statusDisplay || greenway.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="DNT时间">{{ greenway.dntMinutes }}分钟</el-descriptions-item>
          <el-descriptions-item label="到院时间">{{ greenway.doorTime }}</el-descriptions-item>
          <el-descriptions-item label="发病时间">{{ greenway.onsetTime }}</el-descriptions-item>
          <el-descriptions-item label="CT开单">{{ greenway.ctOrderTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="CT完成">{{ greenway.ctCompleteTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="决策时间">{{ greenway.decisionTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="溶栓时间">{{ greenway.needleTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 可用操作 -->
        <div style="margin-top:16px">
          <span style="font-weight:bold; margin-right:12px">可用操作：</span>
          <el-button v-for="action in greenway.availableActions" :key="action"
                     :type="actionBtnType(action)" size="small"
                     @click="doStateChange(action)">{{ actionLabel(action) }}</el-button>
        </div>
      </el-card>

      <!-- 历史记录 -->
      <el-card v-if="history.length > 0" shadow="hover" style="margin-top:12px">
        <template #header><span>状态变更历史</span></template>
        <el-table :data="history" stripe style="width:100%">
          <el-table-column prop="fromStatus" label="从" width="160" />
          <el-table-column prop="toStatus" label="到" width="160" />
          <el-table-column prop="operatorId" label="操作人" width="120" />
          <el-table-column prop="remark" label="备注" />
          <el-table-column prop="actionTime" label="时间" width="180" />
        </el-table>
      </el-card>
    </el-card>

    <!-- 创建绿道对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建绿道" width="500px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="患者ID" prop="patientId">
          <el-input v-model="createForm.patientId" placeholder="输入患者ID（数字）" />
        </el-form-item>
        <el-form-item label="就诊ID" prop="encounterId">
          <el-input v-model="createForm.encounterId" placeholder="输入就诊ID（数字）" />
        </el-form-item>
        <el-form-item label="到院时间" prop="doorTime">
          <el-date-picker v-model="createForm.doorTime" type="datetime" placeholder="选择到院时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="发病时间" prop="onsetTime">
          <el-date-picker v-model="createForm.onsetTime" type="datetime" placeholder="选择发病时间" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createGreenway">确定</el-button>
      </template>
    </el-dialog>

    <!-- 状态变更对话框 -->
    <el-dialog v-model="showStateDialog" title="状态变更" width="400px">
      <el-form label-width="100px">
        <el-form-item label="目标状态">
          <el-tag>{{ targetState }}</el-tag>
        </el-form-item>
        <el-form-item label="操作备注">
          <el-input v-model="stateRemark" type="textarea" :rows="3"
                    :placeholder="isAbnormalState ? '中止/转院必须填写原因' : '可选备注'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStateDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmStateChange">确认变更</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { greenwayApi } from '../api/index.js'

const queryForm = reactive({ encounterId: '' })
const greenway = ref(null)
const history = ref([])
const showCreateDialog = ref(false)
const createFormRef = ref(null)
const createForm = reactive({ patientId: '', encounterId: '', doorTime: '', onsetTime: '' })
const createRules = reactive({
  patientId: [{ required: true, message: '请输入患者ID', trigger: 'blur' },
    { pattern: /^\d+$/, message: '患者ID必须为数字', trigger: 'blur' }],
  encounterId: [{ required: true, message: '请输入就诊ID', trigger: 'blur' },
    { pattern: /^\d+$/, message: '就诊ID必须为数字', trigger: 'blur' }],
  doorTime: [{ required: true, message: '请选择到院时间', trigger: 'change' }],
  onsetTime: [{ required: true, message: '请选择发病时间', trigger: 'change' }]
})
const showStateDialog = ref(false)
const targetState = ref('')
const stateRemark = ref('')

function resetQuery() {
  queryForm.encounterId = ''
  greenway.value = null
  history.value = []
}

async function loadGreenway() {
  if (!queryForm.encounterId) { ElMessage.warning('请输入就诊ID'); return }
  try {
    const res = await greenwayApi.getDetail(queryForm.encounterId)
    greenway.value = res.data
    // 加载历史
    const histRes = await greenwayApi.getHistory(queryForm.encounterId)
    history.value = histRes.data
  } catch (e) {
    ElMessage.error('查询失败，请检查就诊ID')
  }
}

async function createGreenway() {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await greenwayApi.create({
      patientId: parseInt(createForm.patientId),
      encounterId: parseInt(createForm.encounterId),
      doorTime: createForm.doorTime,
      onsetTime: createForm.onsetTime
    })
    ElMessage.success('绿道创建成功')
    showCreateDialog.value = false
    queryForm.encounterId = createForm.encounterId
    await loadGreenway()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

function doStateChange(action) {
  targetState.value = action
  stateRemark.value = ''
  showStateDialog.value = true
}

async function confirmStateChange() {
  try {
    const res = await greenwayApi.changeState(queryForm.encounterId, {
      targetStatus: targetState.value,
      remark: stateRemark.value
    })
    ElMessage.success('状态变更成功')
    showStateDialog.value = false
    greenway.value = res.data
    // 刷新历史
    const histRes = await greenwayApi.getHistory(queryForm.encounterId)
    history.value = histRes.data
  } catch (e) {
    ElMessage.error('状态变更失败')
  }
}

function statusTagType(status) {
  if (!status) return 'info'
  if (status.includes('THROMBOLYSIS') || status === 'COMPLETED') return 'success'
  if (status.includes('ABORT') || status.includes('FAILED')) return 'danger'
  if (status.includes('WAITING') || status.includes('TRIAG')) return 'warning'
  return 'primary'
}

function actionBtnType(action) {
  if (action?.includes('ABORT') || action?.includes('TRANSFER')) return 'danger'
  if (action?.includes('THROMBOLYSIS')) return 'success'
  return 'primary'
}

function actionLabel(action) {
  const labels = {
    CT_ORDERED: '一键开CT', CT_IN_PROGRESS: 'CT扫描中',
    CT_COMPLETED: 'CT完成', CT_REPEAT_REQUIRED: '需重扫',
    AWAITING_DECISION: '等待决策', THROMBOLYSIS_READY: '准备溶栓',
    THROMBOLYSIS_IN_PROGRESS: '开始溶栓', THROMBOLYSIS_COMPLETED: '溶栓完成',
    COMPLETED: '结束绿道', TRIAGING: '分诊中',
    TREATMENT_ABORTED: '中止治疗', TRANSFERRED: '转院'
  }
  return labels[action] || action
}

function isAbnormalState() {
  return targetState.value === 'TREATMENT_ABORTED' || targetState.value === 'TRANSFERRED'
}
</script>
