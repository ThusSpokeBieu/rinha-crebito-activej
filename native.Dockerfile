FROM ghcr.io/graalvm/native-image-community:21-muslib
WORKDIR /app
COPY target/crebito-activej ./
EXPOSE $PORT
ENTRYPOINT ./crebito-activej --enable-preview -server --gc=G1 -Dconfig.http.listenAddresses=$HOSTNAME:$PORT -Dio.netty.buffer.checkBounds=false -Dio.netty.buffer.checkAccessible=false

