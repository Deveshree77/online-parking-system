package com.smartpark.model.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppLog {

    @Id
    private String id;

    private String level;       // INFO, WARN, ERROR
    private String message;
    private String source;      // controller/service name
    private Long userId;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;

    @lombok.Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
