# ============================================================
# Docker 多阶段构建 — 脑卒中疾病管理医疗系统
# Stage 1: Maven 编译所有模块（优化分层缓存）
# Stage 2: JRE 运行环境
# ============================================================

FROM maven:3.8-openjdk-11 AS build
WORKDIR /build

# --- 第1层：阿里云 Maven 镜像配置 ---
COPY .mvn/settings.xml /root/.m2/settings.xml

# --- 第2层：仅 POM 文件（不变则缓存命中） ---
COPY pom.xml ./
RUN mkdir -p common/src/main/java core-domain/src/main/java \
    && mkdir -p core-clinical infrastructure integration rehab-followup analytics \
    && mvn dependency:resolve -q -B 2>/dev/null || true

# --- 第3层：domain + common 源码编译（最常被依赖） ---
COPY common common/
COPY core-domain core-domain/
RUN mvn compile -pl common,core-domain -q -B 2>/dev/null || true

# --- 第4层：全部源码 + 最终编译 ---
COPY . ./
RUN mvn clean package -Dmaven.test.skip=true -B

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
