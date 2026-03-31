package com.phobo.user.controller;

import com.phobo.common.response.PageResponse;
import com.phobo.user.dto.UserRequest;
import com.phobo.user.dto.UserResponse;
import com.phobo.user.dto.UserUpdatePasswordRequest;
import com.phobo.user.dto.UserUpdateRequest;
import com.phobo.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public UserResponse findById(@PathVariable UUID id){
        return userService.findByID(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserRequest userRequest){
        return userService.create(userRequest);
    }

    @PatchMapping("/users/me")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserResponse updateUser(
            @Valid @RequestBody UserUpdateRequest userUpdateRequest){

        return userService.update(userUpdateRequest);
    }

    @PutMapping("/users/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePasswordUser(
            @Valid @RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest
    ){
        userService.updatePassword(userUpdatePasswordRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id){
        userService.deleteUser(id);
    }
}
