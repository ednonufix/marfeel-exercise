package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto;

public record S2MetricResponse(
String service,
String itemId,
int subscribers,
String iban) implements MetricResponse {
}
