package com.phobo.user.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Entity(name = "users")
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // No Update **

    @Column(name = "url_avt")
    private String avatar; // Can update

    @Column(name = "birth")
    private LocalDate birth; // Can update

    @Column(name = "sex")
    private int sex; // Can update

    @Column(name = "address")
    private String address; // Can update

    @Column(name = "name")
    private String name; // Can update

    @Column(name = "email")
    private String email;  // No Update **

    @Column(name = "phone")
    private String phone; // Can update

    @Column(name = "username")
    private String username; // No Update **

    @Column(name = "password")
    private String password; // Can update

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "role")
    private int role;

}
