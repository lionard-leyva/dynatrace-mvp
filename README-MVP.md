# Dynatrace MDC Tracing Simulation MVP

This project simulates a Dynatrace + MDC tracing integration locally without requiring real Dynatrace infrastructure.

## Architecture

- `security-mgt-service`: Spring Boot service with simulated OneAgent preload
- `dynatrace-fluentbit-poc`: Fluent Bit sidecar that tails and enriches logs
- shared Docker volume for `/var/log/app`
- internal Docker network for communication

## Stack

- Spring Boot 3.3.4 / Java 21
- Maven (build)
- Lombok + SLF4J (logging)
- Logback + logstash-logback-encoder (JSON output)
- Fluent Bit 2.2 (log enrichment)

## Build

```bash
docker-compose build
```

> El build de Maven corre dentro del contenedor — no hace falta tener Maven instalado localmente.

## Run

```bash
docker-compose up --build
```

## Acceptance checks

### Test 1: Containers startup

```bash
docker-compose up --build
```

Expected:

- both containers are up
- `security-mgt-service` responds on `:8081`
- Fluent Bit metrics are exposed on `:2020`

### Test 2: Endpoint without headers

```bash
curl http://localhost:8081/poc/test-dynatrace-simulation
```

Expected:

- HTTP 200
- generated `dt_trace_id`
- generated `dt_span_id`
- generated `correlation_id`
- response status `SUCCESS`

### Test 3: Endpoint with headers

```bash
curl -H "x-dynatrace-trace-id: TEST-123" \
     -H "x-correlation-id: CORR-456" \
     http://localhost:8081/poc/test-dynatrace-simulation
```

Expected:

- `"dt_trace_id": "TEST-123"`
- `"correlation_id": "CORR-456"`

### Test 4: Fluent Bit processing

```bash
docker logs dynatrace-fluentbit-poc
```

Expected:

- enriched JSON logs on stdout
- `dt.custom_prop` injected
- `service.name` equals `ib-cc-crm-hub-int-security-mgt-service`
- `aws.ecs.task_definition` equals `security-mgt-service`
- `aws.region` equals `eu-west-1`

### Test 5: Raw log file verification

```bash
docker exec security-mgt-service sh -c "cat /var/log/app/security-mgt.log" | jq
```

Expected:

- valid JSON
- required MDC fields present
- 4 log entries per request

### Test 6: Enriched Fluent Bit output verification

```bash
docker exec dynatrace-fluentbit-poc sh -lc "cat /var/log/app/processed-logs.log | jq"
```

Expected:

- valid JSON lines
- extra Fluent Bit metadata injected

## Cleanup

```bash
docker-compose down -v
```

## Troubleshooting

- If the app container fails to start, rebuild with `docker-compose build --no-cache`.
- If Fluent Bit shows no entries, call the endpoint once and re-check the logs.
- If a port is already in use, free `8081` or `2020` and start the stack again.
