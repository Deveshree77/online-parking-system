package com.smartpark.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Logging service that writes to console.
 * In production, this would write to MongoDB.
 */
@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    public void info(String source, String message, Long userId, Map<String, Object> metadata) {
        logger.info("[{}] {} | userId={} | {}", source, message, userId, metadata);
    }

    public void warn(String source, String message, Long userId, Map<String, Object> metadata) {
        logger.warn("[{}] {} | userId={} | {}", source, message, userId, metadata);
    }

    public void error(String source, String message, Long userId, Map<String, Object> metadata) {
        logger.error("[{}] {} | userId={} | {}", source, message, userId, metadata);
    }
}
