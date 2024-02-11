FROM eclipse-temurin:21.0.2_13-jdk-alpine
WORKDIR /app
COPY target/activej-crebito-0.0.1.jar ./
EXPOSE $PORT
ENTRYPOINT java --enable-preview -server -XX:+UseNUMA -XX:+UseZGC -Dconfig.http.listenAddresses=$HOSTNAME:$PORT -Dconfig.workers=$WORKERS -Dconfig.hikari.maximumPoolSize=$HIKARI_POOL -Dio.netty.buffer.checkBounds=false -Dio.netty.buffer.checkAccessible=false -jar activej-crebito-0.0.1.jar
