# ============================================================
# Docker 多阶段构建 — 脑卒中疾病管理医疗系统
# Stage 1: Maven 编译所有模块
# Stage 2: JRE 运行环境
# ============================================================

FROM maven:3.8-openjdk-11 AS build
WORKDIR /build

# 1. 先复制 POM 文件，利用 Docker 缓存加速依赖下载
COPY pom.xml .
COPY common/pom.xml common/
COPY core-domain/pom.xml core-domain/
COPY core-clinical/pom.xml core-clinical/
COPY infrastructure/pom.xml infrastructure/
COPY integration/pom.xml integration/
COPY rehab-followup/pom.xml rehab-followup/
COPY analytics/pom.xml analytics/

# 2. 下载依赖（单独层，源码不变时不重复下载）
RUN mvn dependency:go-offline -B || true

# 3. 复制全部源码并编译（修改时间戳确保缓存失效）
RUN echo "build-$(date +%s)" > /dev/null
COPY . .
RUN mvn clean install -DskipTests -B

# ============================================================
# Stage 2 — 运行环境
# ============================================================
FROM eclipse-temurin:11-jre
WORKDIR /app

# 时区
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制各服务 JAR
COPY --from=build /build/core-clinical/target/core-clinical-1.0.0-SNAPSHOT.jar    /app/core-clinical.jar
COPY --from=build /build/infrastructure/target/infrastructure-1.0.0-SNAPSHOT.jar  /app/infrastructure.jar
COPY --from=build /build/integration/target/integration-1.0.0-SNAPSHOT.jar        /app/integration.jar
COPY --from=build /build/rehab-followup/target/rehab-followup-1.0.0-SNAPSHOT.jar  /app/rehab-followup.jar
COPY --from=build /build/analytics/target/analytics-1.0.0-SNAPSHOT.jar            /app/analytics.jar

# 默认不启动（由 docker-compose 指定具体服务）
ENTRYPOINT ["java", "-jar"]
CMD []
