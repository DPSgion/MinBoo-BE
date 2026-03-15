package com.phobo.controller;

import com.phobo.entity.User;
import com.phobo.reposity.UserReposity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserReposity userReposity;

    @GetMapping
    public List<User> getAllUsers(){
        return userReposity.findAll();
    }
}
