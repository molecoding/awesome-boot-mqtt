# MQTT with Spring Integration and Spring Boot 2

## Run

Start emqx with docker.

```shell
docker run --rm --name emqx -p 18083:18083 -p 1883:1883 emqx:latest
```

Start application.

```shell
./mvnw spring-boot:run
```

## Test

Use `mqttx`.

```shell
mqttx sub -t nobs
```

Publish a message.

```shell
curl -v http://localhost:8080/nobs -d $(date +%H:%M:%S) -H "Content-Type: text/plain; charset=utf-8"
```
