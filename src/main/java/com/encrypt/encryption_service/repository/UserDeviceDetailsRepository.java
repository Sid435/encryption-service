package com.encrypt.encryption_service.repository;

import com.encrypt.encryption_service.db.UserDeviceDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserDeviceDetailsRepository extends JpaRepository<UserDeviceDetailsEntity, Integer> {

    @Query("SELECT u FROM UserDeviceDetailsEntity u WHERE u.user.userId = :userId AND u.deviceToken = :deviceId")
    Optional<UserDeviceDetailsEntity> findByUserIdAndDeviceId(@Param("userId") String userId, @Param("deviceId") String deviceId);

    @Query("SELECT u FROM UserDeviceDetailsEntity u WHERE u.user.userId = :userId AND u.userAccessToken = :accessToken")
    Optional<UserDeviceDetailsEntity> findByUserIdAndAccessToken(@Param("userId") String userId, @Param("accessToken") String accessToken);

    @Query("SELECT u FROM UserDeviceDetailsEntity u WHERE u.user.userId = :userId AND u.userRefreshToken = :refreshToken")
    Optional<UserDeviceDetailsEntity> findByUserIdAndRefreshToken(@Param("userId") String userId, @Param("refreshToken") String refreshToken);

    @Query("SELECT u FROM UserDeviceDetailsEntity u WHERE u.user.userId = :userId")
    UserDeviceDetailsEntity findByUserId(@Param("userId") String userId);

}
