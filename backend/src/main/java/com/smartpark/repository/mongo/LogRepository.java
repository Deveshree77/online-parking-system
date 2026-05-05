package com.smartpark.repository.mongo;

import com.smartpark.model.mongo.AppLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends MongoRepository<AppLog, String> {
}
