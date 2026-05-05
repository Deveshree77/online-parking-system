package com.smartpark.repository.mongo;

import com.smartpark.model.mongo.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    Optional<UserSession> findBySessionTokenAndIsActiveTrue(String sessionToken);
    Optional<UserSession> findByUserIdAndIsActiveTrue(Long userId);
}
