
#!/bin/bash
# wait-for-http.sh: aguarda até que duas URLs diferentes estejam disponíveis e retornem uma resposta desejada


#!/bin/bash

url1="localhost:8081/healthcheck"
url2="localhost:8082/healthcheck"
expected_response="200"

until [ "$(curl -s -o /dev/null -w '%{http_code}' "$url1")" = "$expected_response" ] && \
      [ "$(curl -s -o /dev/null -w '%{http_code}' "$url2")" = "$expected_response" ]; do
  sleep 1
done

echo "As APIs estão prontas. Iniciando o Nginx."
exec "$@" nginx -g "daemon off;"

