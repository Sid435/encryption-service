package com.encrypt.encryption_service.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@ToString
@RequiredArgsConstructor
@Getter
@Setter
@Table(name = "user_device_details")
@Builder
@AllArgsConstructor
public class UserDeviceDetailsEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private long id;

    private String deviceToken;
    private LocalDateTime lastLoginTime;
    @Lob
    private String userAccessToken;
    @Lob
    private String userRefreshToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ToString.Exclude
    private UserEntity user;


}
