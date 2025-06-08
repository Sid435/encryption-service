package com.encrypt.encryption_service.repository;

import com.encrypt.encryption_service.db.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM user u WHERE u.userId = :userId")
    UserEntity getUserByUserId(@Param("userId") String userId);

    @Query("SELECT u FROM user u WHERE u.email = :email")
    Optional<UserEntity> getUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM user u")
    List<UserEntity> getAllUsers();
}