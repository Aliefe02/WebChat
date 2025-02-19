package com.webchat.server.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(SqlTypes.CHAR)
    @UuidGenerator
    @Column(length = 36, columnDefinition = "char(36)", updatable = false, nullable = false, unique = true)
    private UUID id;

    @Version
    private Integer version;

    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false, unique = true)
    private String username;

    @Column(length = 60, columnDefinition = "varchar(60)", nullable = false)
    private String password;

    @Column(length = 36, columnDefinition = "varchar(36)")
    private String firstName;

    @Column(length = 36, columnDefinition = "varchar(36)")
    private String lastName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}


























