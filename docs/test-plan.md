# 脑卒中疾病管理医疗系统 — 测试计划

| 版本 | 日期 | 作者 | 状态 |
|------|------|------|------|
| v1.0 | 2026-05-21 | QA Team | 草案 |

---

## 1. 测试范围

### 1.1 测试对象

```
系统: 脑卒中疾病管理医疗系统 (Stroke Platform)
架构: Spring Boot 2.7 微服务 + Vue3 前端 + MariaDB + Redis
模块数: 7 Maven 模块 / 5 业务服务 + 2 基础库
容器数: 9 (frontend / gateway / clinical / infra / integration / rehab / analytics / db / redis)
```

### 1.2 测试层级

```
          ╔═══════════════════════╗
          ║   E2E 端到端测试      ║  ← 用户视角，全链路
          ╠═══════════════════════╣
          ║   集成测试             ║  ← 服务间交互
          ╠═══════════════════════╣
          ║   组件/服务测试        ║  ← Controller + Service
          ╠═══════════════════════╣
          ║   单元测试             ║  ← 状态机 + CDS + 工具类
          ╚═══════════════════════╝
```

### 1.3 测试分类

| 类型 | 优先级 | 工具/框架 |
|------|--------|----------|
| 单元测试 | P0 | JUnit 5 + Mockito |
| API 测试 | P0 | REST Assured / curl |
| 状态机测试 | P0 | JUnit 5 |
| CDS 决策测试 | P0 | JUnit 5 |
| 认证安全测试 | P1 | Spring Security Test |
| 前端组件测试 | P1 | Vitest + Vue Test Utils |
| 集成测试 | P1 | Testcontainers / Docker Compose |
| 性能测试 | P2 | JMeter / k6 |
| 安全扫描 | P2 | OWASP ZAP |

---

## 2. 功能测试用例

### 2.1 认证授权模块 (`infrastructure`)

| TC-ID | 场景 | 前置 | 步骤 | 预期结果 |
|-------|------|------|------|---------|
| AUTH-01 | 正确密码登录 | 无 | POST /api/v3/auth/login (admin/admin123) | 200 + JWT token |
| AUTH-02 | 错误密码登录 | 无 | POST /api/v3/auth/login (admin/wrong) | 401 "账号或密码错误" |
| AUTH-03 | 不存在的用户 | 无 | POST /api/v3/auth/login (ghost/xxx) | 401 "账号或密码错误" |
| AUTH-04 | 禁用账号登录 | DB 设置 status=DISABLED | POST /api/v3/auth/login (disabled_user/xxx) | 403 "账号已被禁用" |
| AUTH-05 | 注册新用户 | 无 | POST /api/v3/auth/register | 200 "注册成功" |
| AUTH-06 | 重复注册 | 已有同名用户 | POST /api/v3/auth/register | 400 "账号已存在" |
| AUTH-07 | 无 Token 请求 | 无 | GET /api/v3/greenway | 403 |
| AUTH-08 | 过期 Token 请求 | 生成过期 JWT | GET /api/v3/greenway (Authorization: Bearer expired) | 403 |
| AUTH-09 | 有效 Token 请求 | 先登录 | GET /api/v3/greenway (正确 Bearer) | 200 / 数据 |

### 2.2 绿道状态机 (`core-clinical`)

#### 2.2.1 正常路径

| TC-ID | 场景 | 状态链 | 预期 |
|-------|------|--------|------|
| GW-01 | 完整绿道流程 | WAITING_TRIAGE → TRIAGING → CT_ORDERED → CT_IN_PROGRESS → CT_COMPLETED → AWAITING_DECISION → THROMBOLYSIS_READY → THROMBOLYSIS_IN_PROGRESS → THROMBOLYSIS_COMPLETED → COMPLETED | 每一步 200，时间戳自动记录 |
| GW-02 | 重扫路径 | CT_COMPLETED → CT_REPEAT_REQUIRED → CT_ORDERED → ... | 200，备注必填 |
| GW-03 | 治疗中止 | 任意状态 → TREATMENT_ABORTED | 200，备注必填 |
| GW-04 | 转院 | 任意状态 → TRANSFERRED | 200，备注必填 |

#### 2.2.2 异常路径

| TC-ID | 场景 | 步骤 | 预期 |
|-------|------|------|------|
| GW-05 | 非法跳步 | WAITING_TRIAGE → THROMBOLYSIS_READY | 400 "非法状态转换" |
| GW-06 | 终止态继续推进 | COMPLETED → 任何 | 400 "已处于终止态" |
| GW-07 | 相同状态变更 | TRIAGING → TRIAGING | 400 "目标状态与当前状态相同" |
| GW-08 | 中止无备注 | CT_COMPLETED → TREATMENT_ABORTED (remark=null) | 400 "必须填写操作原因" |
| GW-09 | 绿道不存在 | encounterId=99999 | 404 "未找到绿道记录" |
| GW-10 | 乐观锁冲突 | 同时两个请求修改同一绿道 | 409 "已被其他操作修改" |
| GW-11 | 并发状态变更 | 同一 encounterId 并发 100 次请求 | 所有请求最终一致，无数据损坏 |

### 2.3 CDS 溶栓检查 (`core-clinical`)

| TC-ID | 场景 | 输入 | 预期结论 | 绝对/相对 |
|-------|------|------|---------|----------|
| CDS-01 | 无禁忌 | INR=1.1, PLT=220, BP=135/85, 2h内发病 | ELIGIBLE | 0/0 |
| CDS-02 | INR 过高 | INR=2.1 (>1.7) | CONTRAINDICATED | ≥1 |
| CDS-03 | 血小板过低 | PLT=65 (<100) | CONTRAINDICATED | ≥1 |
| CDS-04 | 近期大手术 | recentMajorSurgery=true | CONTRAINDICATED | ≥1 |
| CDS-05 | 颅内出血史 | intracranialHemorrhageHistory=true | CONTRAINDICATED | ≥1 |
| CDS-06 | 超时间窗 | onset 6h前 (>4.5h) | CAUTION | 0/≥1 |
| CDS-07 | 高血压 | BP=195/115 (>185/110) | CAUTION | 0/≥1 |
| CDS-08 | 低血糖 | glucose=2.1 (<2.7) | CAUTION | 0/≥1 |
| CDS-09 | 高血糖 | glucose=25 (>22.2) | CAUTION | 0/≥1 |
| CDS-10 | 多项绝对禁忌 | INR=2.5 + PLT=50 + ICH史 | CONTRAINDICATED | ≥3 |
| CDS-11 | 多项相对禁忌 | 超窗 + 高血压 + 高血糖 | CAUTION | 0/≥3 |
| CDS-12 | 参数缺失 | INR=null | CAUTION (INR_UNKNOWN) | — |
| CDS-13 | 发病时间在未来 | onset=+1h | CONTRAINDICATED (TIME_INVALID) | — |

### 2.4 NIHSS 评估 (`core-clinical`)

| TC-ID | 场景 | 步骤 | 预期 |
|-------|------|------|------|
| NIHSS-01 | 首次提交评分 | 15项全填 | 200 + versionNo=1 + totalScore 正确 |
| NIHSS-02 | 二次提交 | 同一 encounterId | versionNo 递增，旧版本不覆盖 |
| NIHSS-03 | 离线提交 | source=OFFLINE | 标记为离线，可区分 |
| NIHSS-04 | 非法分数 | 1a=99 (max=3) | 400 "分数无效" |
| NIHSS-05 | 非法条目编码 | unknown=0 | 400 "未知条目" |
| NIHSS-06 | 部分评分 | 仅填写 3 项 | 200，总分=填写项之和 |
| NIHSS-07 | 查最新评分 | GET /latest | 返回 versionNo 最大的那条 |
| NIHSS-08 | 查历史记录 | GET /history | 返回所有版本，按时间降序 |
| NIHSS-09 | 量表配置 | GET /items | 返回 15 项配置(编码/名称/范围) |

### 2.5 超时告警 + WebSocket

| TC-ID | 场景 | 步骤 | 预期 |
|-------|------|------|------|
| ALERT-01 | DNT 未超时不推送 | DNT=30min (<45) | 无告警事件 |
| ALERT-02 | DNT 超过 45min | DNT=60min | 触发 DNT_TIMEOUT_WARNING 事件 |
| ALERT-03 | CT 超过 25min | Door-to-CT=35min | 触发 CT_TIMEOUT_WARNING 事件 |
| ALERT-04 | WebSocket 连接 | 前端连接 /api/v3/ws/alerts | 101 Switching Protocols |
| ALERT-05 | WebSocket 心跳 | 每 30s 发 ping | 返回 pong |
| ALERT-06 | WebSocket 断连重连 | 断开连接 | 5s 后自动重连 |
| ALERT-07 | 告警实时推送 | DNT 超时触发 | WebSocket 推送 JSON 告警到所有客户端 |
| ALERT-08 | 严重告警不自动关闭 | severity=critical | 弹窗需手动关闭 |

### 2.6 驾驶舱 (`analytics`)

| TC-ID | 场景 | 步骤 | 预期 |
|-------|------|------|------|
| DASH-01 | 获取概览指标 | GET /dashboard/overview | 返回 13 个指标 |
| DASH-02 | DNT 趋势 | GET /dashboard/dnt-trend?start=&end= | 返回每日 DNT 中位数/P95 |
| DASH-03 | 月溶栓率 | GET /dashboard/thrombolysis-rate?year=2026 | 返回每月溶栓率 |
| DASH-04 | 空数据 | 无任何绿道记录 | 返回空数组/0 值 |

### 2.7 随访 (`rehab-followup`)

| TC-ID | 场景 | 步骤 | 预期 |
|-------|------|------|------|
| FUP-01 | 查随访计划 | GET /followup/plans?patientId=1 | 返回随访计划列表 |
| FUP-02 | 查随访任务 | GET /followup/tasks?planId=1 | 返回任务列表 |
| FUP-03 | 患者自报 | POST /followup/reports | 200，异常指标触发预警 |
| FUP-04 | 不存在的患者 | patientId=99999 | 空列表 |

### 2.8 前端

| TC-ID | 场景 | 步骤 | 预期 |
|-------|------|------|------|
| UI-01 | 未登录跳转 | 访问 /greenway | 路由到 /login |
| UI-02 | 登录成功 | 输入 admin/admin123 | 跳转到 /greenway |
| UI-03 | 登录失败 | 输入错误密码 | 错误提示，不跳转 |
| UI-04 | 退出登录 | 点击退出 | 清除 token，跳转 /login |
| UI-05 | 绿道状态推进 | 前端操作状态变更 | 界面刷新，时间线更新 |
| UI-06 | ECharts 渲染 | 驾驶舱页面 | 图表正常加载 |
| UI-07 | 表单校验 | 必填项未填 | 校验提示，不提交 |
| UI-08 | NIHSS 趋势图 | 有多个评估记录 | 折线图显示评分趋势 |

---

## 3. 非功能测试

### 3.1 性能测试

| 场景 | 指标 | 阈值 |
|------|------|------|
| 绿道创建（写） | TPS | > 100 req/s |
| 绿道查询（读） | TPS | > 500 req/s |
| 登录接口 | 响应时间 | p99 < 500ms |
| 状态变更 | 响应时间 | p99 < 200ms |
| 同时在线用户 | 并发 | > 50 |
| WebSocket 连接数 | 并发 | > 200 |

### 3.2 安全测试

| 类别 | 测试项 | 方法 |
|------|--------|------|
| 认证绕过 | 无 Token 访问 API | curl 无 Authorization 头 |
| JWT 伪造 | 篡改 token payload | 修改 userId/role 后重签名 |
| JWT 过期 | 使用过期 token | 等待 token 过期后请求 |
| SQL 注入 | 参数注入 | encounterId=1' OR '1'='1 |
| XSS | 跨站脚本 | 备注字段输入 <script>alert(1)</script> |
| 越权访问 | 低权限用户访问高权限 API | nurse1 执行溶栓操作 |

### 3.3 兼容性测试

| 浏览器 | 版本 | 状态 |
|--------|------|------|
| Chrome | 最新 3 个版本 | ⬜ |
| Firefox | 最新 3 个版本 | ⬜ |
| Edge | 最新 3 个版本 | ⬜ |
| Safari (iOS PAD) | 最新 2 个版本 | ⬜ |

### 3.4 稳定性测试

| 测试 | 方法 | 持续时间 |
|------|------|---------|
| 7×24 长时间运行 | 持续调用绿道 API + 状态变更 | 24h |
| 容器重启 | `docker compose restart clinical` | 5 次 |
| 数据库断连 | `docker stop db` → `docker start db` | 3 次 |
| 全量服务重启 | `docker compose down && docker compose up -d` | 3 次 |

---

## 4. 测试环境

### 4.1 环境配置

```
┌──────────┬──────────────────────────────┐
│ 环境      │ 配置                         │
├──────────┼──────────────────────────────┤
│ DEV       │ 开发者本地 Docker Compose      │
│ CI        │ GitHub Actions + Docker Compose│
│ STAGING   │ 单机 Docker Compose（同生产）  │
│ PROD      │ K8s 集群（待部署）            │
└──────────┴──────────────────────────────┘
```

### 4.2 测试数据

**预设账号**:
```
admin / admin123  — 系统管理员（所有权限）
doctor1 / admin123 — 神经内科医生（绿道/溶栓/评估）
nurse1 / admin123  — 急诊护士（分诊/基础操作）
```

**预设绿道**:
```
encounterId=100: THROMBOLYSIS_COMPLETED（已完成溶栓）
encounterId=200: CT_COMPLETED（待决策）
```

---

## 5. 测试执行计划

### 5.1 阶段划分

| 阶段 | 内容 | 工时 | 交付物 |
|------|------|------|--------|
| **Phase 1** | 单元测试补充 + 现有 82 测试回归 | 2d | 测试报告 |
| **Phase 2** | API 自动化测试（REST Assured） | 3d | 自动化脚本 |
| **Phase 3** | 前端组件测试（Vitest） | 2d | 前端测试 |
| **Phase 4** | 集成测试（Docker Compose + curl） | 2d | 集成测试报告 |
| **Phase 5** | 性能 + 安全测试 | 3d | 性能/安全报告 |
| **Phase 6** | 全量回归 + 验收 | 2d | 测试总结报告 |

### 5.2 自动化测试框架建议

```
单元测试:
  ├─ JUnit 5 + Mockito + AssertJ
  ├─ JaCoCo 覆盖率 > 80%
  └─ Surefire 报告

API 测试:
  ├─ REST Assured + JUnit 5
  ├─ 或 Karate DSL（Gherkin 风格）
  └─ 覆盖全部 23+ 端点

前端测试:
  ├─ Vitest + Vue Test Utils
  ├─ Playwright (E2E)
  └─ 覆盖核心流程

契约测试:
  ├─ Spring Cloud Contract
  └─ 服务间 API 兼容性
```

---

## 6. 缺陷管理

### 6.1 严重等级定义

| 等级 | 定义 | SLA |
|------|------|-----|
| 🔴 P0-Critical | 核心流程中断（无法创建绿道/登录） | 2h |
| 🟡 P1-Major | 功能异常（状态机跳步/CDS 误判） | 8h |
| 🔵 P2-Minor | 非核心功能问题（UI 显示/文案） | 2d |
| ⚪ P3-Trivial | 体验优化/样式/建议 | 迭代规划 |

### 6.2 已知高风险区域

```
1. 状态机边界条件（并发状态变更）
2. CDS 时间窗计算（跨时区/夏令时）
3. WebSocket 长连接保活
4. JWT 密钥轮换
5. 离线数据合并冲突
```

---

## 7. 测试通过标准

```
□ 所有 P0 用例通过率 100%
□ 所有 P1 用例通过率 ≥ 95%
□ 核心 API 响应时间 p99 < 500ms
□ 无高危安全漏洞
□ JaCoCo 覆盖率 ≥ 80%（新增代码）
□ 前端 3 大浏览器兼容
□ DNT 告警 WebSocket 推送延迟 < 3s
```
