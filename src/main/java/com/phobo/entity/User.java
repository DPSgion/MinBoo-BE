package com.phobo.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "users_id", updatable = false, nullable = false)
    private UUID usersId;

    @Column(name = "url_avt")
    private String url_avt;

    private String name;
    private LocalDate birth;
    private String sex;
    private String address;
    private String username;
    private String password;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime created_at;

    public UUID getUsersId() {
        return usersId;
    }
    public void setUsersId(UUID usersId) {
        this.usersId = usersId;
    }
    public String getUrl_avt() {
        return url_avt;
    }
    public void setUrl_avt(String url_avt) {
        this.url_avt = url_avt;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public LocalDate getBirth() {
        return birth;
    }
    public void setBirth(LocalDate birth) {
        this.birth = birth;
    }
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public LocalDateTime getCreated_at() {
        return created_at;
    }
    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
