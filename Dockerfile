FROM eclipse-temurin:21.0.2_13-jdk-alpine

RUN apk update && \
    apk add --no-cache curl

WORKDIR /app
COPY target/activej-crebito-0.0.1.jar ./
EXPOSE $PORT
ENTRYPOINT java --enable-preview -server -XX:+UseNUMA -XX:+UseZGC -Dconfig.http.listenAddresses=$HOSTNAME:$PORT -Dio.netty.buffer.checkBounds=false -Dio.netty.buffer.checkAccessible=false -jar activej-crebito-0.0.1.jar
