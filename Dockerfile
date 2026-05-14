# 第一阶段：构建 Maven 环境
# 构建镜像
FROM maven:3.9.6-eclipse-temurin-21 AS builder
# 设置工作目录
WORKDIR /build
# 拷贝 pom.xml 文件到工作目录
COPY pom.xml .
# 下载并缓存所有的 Maven 依赖，依赖不变时可以加速后续构建
RUN mvn dependency:go-offline -q
# 拷贝源代码到工作目录
COPY src ./src
# 编译源代码，打包jar文件
RUN mvn clean package -DskipTests -q

# 第二阶段：运行应用
# 构建镜像，使用轻量级JRE 21镜像，减小体积
FROM eclipse-temurin:21-jre
# 设置工作目录
WORKDIR /app
# 拷贝构建好的 jar 包到工作目录，重命名为app.jar
COPY --from=builder /build/target/*.jar app.jar
# 暴露端口
EXPOSE 8124
# 指定prod配置，启动SpringBoot应用
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
