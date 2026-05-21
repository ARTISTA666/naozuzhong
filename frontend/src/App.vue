<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background: #304156">
      <div class="logo">
        <span class="logo-text">卒中管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/greenway">
          <el-icon><Ambulance /></el-icon>
          <span>绿道管理</span>
        </el-menu-item>
        <el-menu-item index="/nihss">
          <el-icon><Document /></el-icon>
          <span>NIHSS评估</span>
        </el-menu-item>
        <el-menu-item index="/thrombolysis">
          <el-icon><Warning /></el-icon>
          <span>溶栓禁忌检查</span>
        </el-menu-item>
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据驾驶舱</span>
        </el-menu-item>
        <el-menu-item index="/followup">
          <el-icon><ChatLineSquare /></el-icon>
          <span>随访管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="border-bottom:1px solid #e6e6e6; display:flex; align-items:center; justify-content:space-between">
        <h2 style="margin:0; font-size:18px">{{ route.meta.title }}</h2>
        <div style="display:flex; align-items:center; gap:12px">
          <el-tag type="success" effect="dark" size="small">系统运行中</el-tag>
          <el-dropdown v-if="user" @command="handleCommand">
            <span style="cursor:pointer; display:flex; align-items:center; gap:4px">
              <el-icon><UserFilled /></el-icon>
              {{ user.displayName }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main style="background:#f0f2f5">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()

const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('user')) } catch { return null }
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }
}
</script>

<style>
body { margin: 0; }
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
}
</style>
