package com.company.securitymgt.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/poc")
public class DynatraceSimulationController {

    private static final String SERVICE_NAME = "ib-cc-crm-hub-int-security-mgt-service";

    @GetMapping("/test-dynatrace-simulation")
    public ResponseEntity<Map<String, String>> testDynatraceSimulation(
            @RequestHeader(value = "x-dynatrace-trace-id", required = false) String traceIdHeader,
            @RequestHeader(value = "x-dynatrace-span-id", required = false) String spanIdHeader,
            @RequestHeader(value = "x-correlation-id", required = false) String correlationIdHeader) {

        String timestamp = String.valueOf(System.currentTimeMillis());

        // Trace ID viaja igual de punta a punta — nunca cambia
        String traceId = resolveHeader(traceIdHeader, "DT-TRACE-" + timestamp);

        // El span ID del header identifica QUIEN NOS LLAMA → lo guardamos como parent
        // Si no viene, somos el origen de la cadena (no hay padre)
        String parentSpanId = resolveHeader(spanIdHeader, null);

        // Generamos NUESTRO propio span — identifica el trabajo que hace ESTE servicio
        String mySpanId = "security-" + Long.toHexString(System.nanoTime());

        String correlationId = resolveHeader(correlationIdHeader, "SECURITY-" + timestamp);

        try {
            MDC.put("dt_trace_id",    traceId);
            MDC.put("parent_span_id", parentSpanId != null ? parentSpanId : "none");
            MDC.put("my_span_id",     mySpanId);
            MDC.put("correlation_id", correlationId);
            MDC.put("service_name",   SERVICE_NAME);
            MDC.put("environment",    "poc");
            MDC.put("business_domain","customer");
            MDC.put("product",        "crm");
            MDC.put("application",    "security-mgt");
            MDC.put("dt.entity.service",    "ser-security-mgt");

            log.info("Starting security management validation");
            log.info("Validating user permissions");
            log.warn("Security check - elevated permissions detected");
            log.info("Security validation completed successfully");

            Map<String, String> response = new LinkedHashMap<>();
            response.put("dt_trace_id",    traceId);
            response.put("parent_span_id", parentSpanId != null ? parentSpanId : "none");
            response.put("my_span_id",     mySpanId);
            response.put("correlation_id", correlationId);
            response.put("service_name",   SERVICE_NAME);
            // Simula los headers que este servicio propagaría a cualquier downstream
            response.put("outgoing_x-dynatrace-trace-id", traceId);
            response.put("outgoing_x-dynatrace-span-id",  mySpanId);
            response.put("status",  "SUCCESS");
            response.put("message", "Dynatrace simulation working");
            return ResponseEntity.ok(response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveHeader(String headerValue, String fallback) {
        return Optional.ofNullable(headerValue)
                .filter(value -> !value.isBlank())
                .orElse(fallback);
    }
}
