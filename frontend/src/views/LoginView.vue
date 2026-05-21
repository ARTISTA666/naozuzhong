<template>
  <div style="display:flex; justify-content:center; align-items:center; height:100vh; background:#2d3a4b">
    <el-card style="width:420px; padding:20px">
      <template #header>
        <div style="text-align:center">
          <h2 style="margin:0">卒中管理系统</h2>
          <p style="color:#999; font-size:13px; margin-top:4px">脑卒中疾病管理医疗系统</p>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" size="large"
                    prefix-icon="UserFilled" @keyup.enter="login" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large"
                    prefix-icon="Lock" show-password @keyup.enter="login" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="login">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div style="text-align:center; font-size:12px; color:#999; margin-top:12px">
        默认账号: admin / doctor1 / nurse1 / tech1 密码: admin123
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api/index.js'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function login() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await authApi.login(form)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data))
    ElMessage.success(`欢迎回来，${res.data.displayName}`)
    router.push('/greenway')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
