package com.codesoft.edu.controller;

import com.codesoft.edu.service.ToDoService;
import com.codesoft.edu.dto.ToDoDto;
import com.codesoft.edu.dto.ToDoResponse;
import com.codesoft.edu.dto.ToDoTransformer;
import com.codesoft.edu.model.ToDo;
import com.codesoft.edu.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{user_id}/todos")
@Slf4j
public class ToDoController {
    private final UserService userService;
    private final ToDoService toDoService;

    @Autowired
    public ToDoController(UserService userService, ToDoService toDoService) {
        this.userService = userService;
        this.toDoService = toDoService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ToDoResponse> getAll(@PathVariable("user_id") long user_id) {
        log.info("Get ToDos by user_id = {}", userService.readById(user_id));
        return toDoService.getByUserId(user_id).stream()
                .map(ToDoResponse::new)
                .collect(Collectors.toList());
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createToDo(@PathVariable("user_id") long user_id, @Validated @RequestBody ToDoDto toDoDto, BindingResult result) {
        if (result.hasErrors()) {
            log.error("ToDo creation failed: {}", result.getAllErrors());
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        ToDo toDo = ToDoTransformer.convertToEntity(toDoDto);
        toDo.setOwner(userService.readById(user_id));
        System.out.println(toDo);

        try {
            toDoService.create(toDo);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        ToDoResponse toDoResponse = new ToDoResponse(toDo);
        log.info("User created: {}", toDoResponse);
        return ResponseEntity.ok(toDoResponse);
    }

    @DeleteMapping("/{todo_id}")
    public ResponseEntity<?> deleteToDo(@PathVariable("user_id") long user_id,
                                        @PathVariable("todo_id") long todo_id) {
        toDoService.delete(todo_id);
        log.info("ToDo deleted: id={}", todo_id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{todo_id}")
    public ResponseEntity<?> updateToDo(@PathVariable("user_id") long user_id,
                                        @PathVariable("todo_id") long todo_id,
                                        @Validated @RequestBody ToDoDto toDoDto,
                                        BindingResult result) {
        if (result.hasErrors()) {
            log.error("User update failed: {}", result.getAllErrors());
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        ToDo existingToDo = toDoService.readById(todo_id);
        existingToDo.setTitle(toDoDto.getTitle());
        toDoService.update(existingToDo);
        ToDoResponse toDoResponse = new ToDoResponse(existingToDo);
        log.info("ToDo updated: {}", toDoResponse);
        return ResponseEntity.ok(toDoResponse);
    }
}
