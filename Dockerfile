# ============================================================
# Docker 多阶段构建 — 脑卒中疾病管理医疗系统
# Stage 1: Maven 编译所有模块
# Stage 2: JRE 运行环境
# ============================================================

FROM maven:3.8-openjdk-11 AS build
WORKDIR /build

# 复制所有 POM 和源码
COPY pom.xml .
COPY common common/
COPY core-domain core-domain/
COPY core-clinical core-clinical/
COPY infrastructure infrastructure/
COPY integration integration/
COPY rehab-followup rehab-followup/
COPY analytics analytics/
COPY docs docs/

# 编译全部模块（跳过测试）
RUN mvn clean install -Dmaven.test.skip=true -B

# ============================================================
# Stage 2 — 运行环境
# ============================================================
FROM eclipse-temurin:11-jre
WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制各服务 JAR
COPY --from=build /build/core-clinical/target/core-clinical-1.0.0-SNAPSHOT.jar    /app/core-clinical.jar
COPY --from=build /build/infrastructure/target/infrastructure-1.0.0-SNAPSHOT.jar  /app/infrastructure.jar
COPY --from=build /build/integration/target/integration-1.0.0-SNAPSHOT.jar        /app/integration.jar
COPY --from=build /build/rehab-followup/target/rehab-followup-1.0.0-SNAPSHOT.jar  /app/rehab-followup.jar
COPY --from=build /build/analytics/target/analytics-1.0.0-SNAPSHOT.jar            /app/analytics.jar

ENTRYPOINT ["java", "-jar"]
CMD []
