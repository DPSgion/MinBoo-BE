package com.phobo.user.controller;

import com.phobo.common.response.PageResponse;
import com.phobo.post.service.PostService;
import com.phobo.user.dto.UserRequest;
import com.phobo.user.dto.UserResponse;
import com.phobo.user.dto.UserUpdatePasswordRequest;
import com.phobo.user.dto.UserUpdateRequest;
import com.phobo.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;

    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
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

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserResponse updateUser(
            @Valid @RequestBody UserUpdateRequest userUpdateRequest,
            @PathVariable UUID id){

        return userService.update(userUpdateRequest, id);
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePasswordUser(@Valid @RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest,
                                   @PathVariable UUID id){

        userService.updatePassword(userUpdatePasswordRequest, id);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id){
        userService.deleteUser(id);
    }


    //Lấy bài viết của một user cụ thể
    @GetMapping("/{user_id}/posts") //
    public ResponseEntity<Map<String, Object>> getUserPosts(
            @RequestHeader("user-id") UUID viewerId,
            @PathVariable("user_id") UUID profileOwnerId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        Map<String, Object> data = postService.getUserPosts(viewerId, profileOwnerId, page, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
