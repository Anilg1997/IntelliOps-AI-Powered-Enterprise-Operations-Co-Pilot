package com.intellops.copilot.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    private String id;

    @Indexed
    private String eventType;

    private String source;

    @Indexed
    private String entityId;

    private String entityType;

    private Map<String, String> details;

    @Indexed
    private LocalDateTime timestamp;
}
