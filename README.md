# 脑卒中疾病管理医疗系统 (Stroke Platform)

**卒中全流程协同与质控平台**

## 系统架构

五个核心服务（收敛设计）：

| 服务 | 端口 | 职责 |
|------|------|------|
| **core-clinical** | 8081 | 患者主索引、就诊、绿道状态机、量表评定、溶栓/取栓 |
| **infrastructure** | 8082 | 用户权限、审计、通知、CDS规则引擎 |
| **integration** | 8083 | HIS/LIS/PACS对接、FHIR网关、数据标准化 |
| **rehab-followup** | 8084 | 康复评定、随访、二级预防 |
| **analytics** | 8085 | BI报表、质控指标、科研数据导出 |

## 技术栈

- **后端**：Spring Boot 2.7 + MyBatis-Plus 3.5 + MySQL 8.0
- **网关**：Spring Cloud Gateway
- **缓存**：Redis Sentinel
- **事件**：RocketMQ（仅集成事件）
- **可观测**：SkyWalking + Prometheus + Grafana
- **API文档**：Knife4j (Swagger)

## 开发指南

```bash
# 构建
mvn clean install -DskipTests

# 启动 core-clinical
mvn -pl core-clinical spring-boot:run

# 启动 infrastructure
mvn -pl infrastructure spring-boot:run

# 启动 integration
mvn -pl integration spring-boot:run
```

## 设计原则

- **业务收敛优先**：强一致性业务聚合，减少分布式事务
- **稳定压倒新颖**：核心链路（绿道）使用简单、可验证的实现
- **离线与异常优先**：设计伊始考虑网络中断、老旧设备兼容
- **医疗合规**：等保2.0三级，国密，全操作留痕

> 详细设计见 [开发文档.txt](开发文档.txt)
