package com.encrypt.encryption_service.repository;

import com.encrypt.encryption_service.db.Keys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeysRepository extends JpaRepository<Keys, String> {
    @Query("SELECT k FROM user_keys k WHERE k.user.userId = :userId")
    Keys findByUserId(String userId);
}
