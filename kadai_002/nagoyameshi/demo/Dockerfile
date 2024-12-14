FROM openjdk:17-jdk-slim
WORKDIR /app

# JVMのメモリ設定を追加
ENV JAVA_OPTS="-Xmx512m -Xms256m"

COPY target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=cloud
EXPOSE 8080

# ヘルスチェック用
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]