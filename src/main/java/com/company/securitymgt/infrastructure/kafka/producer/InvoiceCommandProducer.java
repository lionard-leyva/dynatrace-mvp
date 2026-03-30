package com.company.securitymgt.infrastructure.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class InvoiceCommandProducer {

    private static final String TOPIC = "invoice.command";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InvoiceCommandProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCreateInvoice(String invoiceId, double amount) {

        CreateInvoiceMessage createInvoiceMessage = new CreateInvoiceMessage(UUID.randomUUID(), invoiceId, amount);
        // Usamos ProducerRecord para poder añadir headers antes del send
        ProducerRecord<String, Object> record = new ProducerRecord<>(TOPIC, createInvoiceMessage.invoiceId(),  createInvoiceMessage.amount());


        // Propagamos el contexto de tracing Dynatrace desde MDC → headers Kafka
        // El consumidor los recibirá en ConsumerRecord.headers() y los meterá en su MDC
        addTracingHeader(record, "x-dynatrace-trace-id", "dt_trace_id");
        addTracingHeader(record, "x-dynatrace-span-id",  "my_span_id");
        addTracingHeader(record, "x-correlation-id",     "correlation_id");

        log.info("Sending CreateInvoiceMessage to topic '{}' messageId={} invoiceId={} amount={}", TOPIC, createInvoiceMessage.messageId(),
                createInvoiceMessage.invoiceId(), createInvoiceMessage.amount());

        kafkaTemplate.send(record);
    }

    private void addTracingHeader(ProducerRecord<String, Object> record,
                                  String headerName,
                                  String mdcKey) {
        String value = MDC.get(mdcKey);
        if (value != null && !value.isBlank()) {
            record.headers().add(new RecordHeader(headerName, value.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
