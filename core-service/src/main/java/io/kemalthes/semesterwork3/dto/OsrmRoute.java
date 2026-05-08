package io.kemalthes.semesterwork3.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmRoute(Double distance, Double duration, String geometry) {
}
