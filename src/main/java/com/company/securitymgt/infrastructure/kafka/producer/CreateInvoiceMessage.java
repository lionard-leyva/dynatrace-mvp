package com.company.securitymgt.infrastructure.kafka.producer;

import java.util.UUID;

public record CreateInvoiceMessage (
        UUID messageId,
     String invoiceId,
     double amount
){}
