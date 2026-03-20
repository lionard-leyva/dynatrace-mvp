package com.company.securitymgt;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.securitymgt.controller.DynatraceSimulationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DynatraceSimulationController.class)
class DynatraceSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateTracingValuesWhenHeadersAreMissing() throws Exception {
        mockMvc.perform(get("/poc/test-dynatrace-simulation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dt_trace_id", startsWith("DT-TRACE-")))
                .andExpect(jsonPath("$.dt_span_id", startsWith("DT-SPAN-")))
                .andExpect(jsonPath("$.correlation_id", startsWith("SECURITY-")))
                .andExpect(jsonPath("$.service_name").value("ib-cc-crm-hub-int-security-mgt-service"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Dynatrace simulation working"));
    }

    @Test
    void shouldPreserveIncomingHeaders() throws Exception {
        mockMvc.perform(get("/poc/test-dynatrace-simulation")
                        .header("x-dynatrace-trace-id", "TEST-123")
                        .header("x-dynatrace-span-id", "SPAN-456")
                        .header("x-correlation-id", "CORR-789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dt_trace_id").value("TEST-123"))
                .andExpect(jsonPath("$.dt_span_id").value("SPAN-456"))
                .andExpect(jsonPath("$.correlation_id").value("CORR-789"));
    }
}
