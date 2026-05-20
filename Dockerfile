# 第一阶段：构建 Maven 环境
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
# 配置阿里云 Maven 镜像
COPY settings.xml $MAVEN_HOME/conf/settings.xml
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# 第二阶段：运行应用
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8124
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
