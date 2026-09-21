package com.abhishek.ecommerce.common;
import java.time.Instant;
public record DomainEvent(EventType eventType,String eventId,String aggregateId,String userId,String payload,Instant occurredAt) {}