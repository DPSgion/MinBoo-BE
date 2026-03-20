package com.phobo.user.controller;

import com.phobo.common.response.PageResponse;
import com.phobo.user.dto.UserResponse;
import com.phobo.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"", "/"})
    public PageResponse<UserResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.findAll(search, page, size);
    }

    @GetMapping("/{id}")
    public Optional<UserResponse> findById(@PathVariable UUID id){
        return userService.findByID(id);
    }
}
