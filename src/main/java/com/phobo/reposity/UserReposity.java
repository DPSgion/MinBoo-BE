package com.phobo.reposity;


import com.phobo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReposity extends JpaRepository<User, Long> {


}
