package io.kemalthes.semesterwork3.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmResponse(String code, List<OsrmRoute> routes) {
}
