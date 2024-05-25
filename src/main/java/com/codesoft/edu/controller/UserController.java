package com.codesoft.edu.controller;

import com.codesoft.edu.dto.UserDto;
import com.codesoft.edu.dto.UserResponse;
import com.codesoft.edu.dto.UserTransformer;
import com.codesoft.edu.model.User;
import com.codesoft.edu.service.RoleService;
import com.codesoft.edu.service.ToDoService;
import com.codesoft.edu.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService, ToDoService toDoService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @PostMapping("/")
    public ResponseEntity<?> createUser(@Validated @RequestBody UserDto userDto, BindingResult result) {
        if (result.hasErrors()) {
            log.error("User creation failed: {}", result.getAllErrors());
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        User newUser = UserTransformer.convertToEntity(userDto);
        newUser.setRole(roleService.readById(2));
        userService.create(newUser);
        UserResponse userResponse = new UserResponse(newUser);
        log.info("User created: {}", userResponse);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long id) {
        User user = userService.readById(id);
        UserResponse userResponse = new UserResponse(user);
        log.info("User found: {}", userResponse);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable long id, @Validated @RequestBody UserDto userDto, BindingResult result) {
        if (result.hasErrors()) {
            log.error("User update failed: {}", result.getAllErrors());
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        User existingUser = userService.readById(id);
        if(userDto.getFirstName() != null) existingUser.setFirstName(userDto.getFirstName());
        if(userDto.getLastName() != null) existingUser.setLastName(userDto.getLastName());
        if(userDto.getEmail() != null)existingUser.setEmail(userDto.getEmail());
        if(userDto.getPassword() != null)existingUser.setPassword(userDto.getPassword());
        userService.update(existingUser);
        UserResponse userResponse = new UserResponse(existingUser);
        log.info("User updated: {}", userResponse);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        userService.delete(id);
        log.info("User deleted: id={}", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/")
    public ResponseEntity<List<UserResponse>> getAll() {
        List<User> users = userService.getAll();
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::new)
                .toList();
        userResponses.forEach(userResponse -> log.info("User found: {}", userResponse));
        return ResponseEntity.ok(userResponses);
    }
}
