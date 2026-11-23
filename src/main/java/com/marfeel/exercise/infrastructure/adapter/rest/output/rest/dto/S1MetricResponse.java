package com.marfeel.exercise.infrastructure.adapter.rest.output.rest.dto;

public record S1MetricResponse(
String service,
String code,
String subcode,
int subscribers,
String iban
) implements MetricResponse {
}
