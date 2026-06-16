package com.intellops.copilot.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {

    List<ActivityLog> findByEntityIdOrderByTimestampDesc(String entityId);

    List<ActivityLog> findByEventTypeOrderByTimestampDesc(String eventType);
}
