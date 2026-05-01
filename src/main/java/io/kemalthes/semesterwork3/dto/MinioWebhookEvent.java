package io.kemalthes.semesterwork3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MinioWebhookEvent(
        @JsonProperty("EventName") String eventName,
        @JsonProperty("Key") String key,
        @JsonProperty("Records") List<MinioRecord> records
) {
    public record MinioRecord(@JsonProperty("s3") S3 s3) {}

    public record S3(@JsonProperty("object") MinioObject object) {}

    public record MinioObject(
            @JsonProperty("key") String key,
            @JsonProperty("size") Long size
    ) {}
}