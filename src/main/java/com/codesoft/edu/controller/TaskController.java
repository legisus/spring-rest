package com.codesoft.edu.controller;

import com.codesoft.edu.model.Priority;
import com.codesoft.edu.service.TaskService;
import com.codesoft.edu.service.ToDoService;
import com.codesoft.edu.dto.TaskDto;
import com.codesoft.edu.dto.TaskTransformer;
import com.codesoft.edu.model.Task;
import com.codesoft.edu.model.ToDo;
import com.codesoft.edu.service.StateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping({"/api/todos/{todo_id}/tasks", "/api/users/{user_id}/todos/{todo_id}/tasks"})
@Slf4j
public class TaskController {
    private final TaskService taskService;
    private final ToDoService todoService;
    private final StateService stateService;

    @Autowired
    public TaskController(TaskService taskService, ToDoService todoService, StateService stateService) {
        this.taskService = taskService;
        this.todoService = todoService;
        this.stateService = stateService;
    }

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskDto> getAll(@PathVariable("todo_id") long todoId,
                                      @PathVariable(name = "user_id", required = false) Long userId) {
        log.info("Check if todo with id " + todoId + " exist");
        if (todoService.readById(todoId) == null) {
            throw new EntityNotFoundException("Todo with id " + todoId + " not found");
        }
        log.info("Check if todo with id " + todoId + " belong to input user");
        if (userId != null) {
            ToDo toDo = todoService.readById(todoId);
            if (toDo.getOwner().getId() != userId.longValue()) {
                throw new EntityNotFoundException("Todo with id " + todoId + " not found in todos list of user with id " + userId);
            }
        }
        log.info("Get all tasks with id " + todoId);
        List<TaskDto> result = new ArrayList<>();
        List<Task> tasks = taskService.getByTodoId(todoId);
        result = tasks.stream().map(TaskTransformer::convertToDto).toList();
        return result;
    }

    @GetMapping("/{task_id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDto getById(@PathVariable("task_id") long taskId,
                               @PathVariable("todo_id") long todoId,
                               @PathVariable(name = "user_id", required = false) Long userId) {
        log.info("Check if todo with id " + todoId + " exist");
        if (todoService.readById(todoId) == null) {
            throw new EntityNotFoundException("Todo with id " + todoId + " not found");
        }
        log.info("Check if todo with id " + todoId + " belong to input user");
        if (userId != null) {
            ToDo toDo = todoService.readById(todoId);
            if (toDo.getOwner().getId() != userId.longValue()) {
                throw new EntityNotFoundException("Todo with id " + todoId + " not found in todos list of user with id " + userId);
            }
        }
        Task task = taskService.readById(taskId);
        TaskDto taskDto = TaskTransformer.convertToDto(task);
        log.info("Check if input task belongs to todo with id " + todoId);
        if (taskDto.getTodoId() != todoId) {
            throw new EntityNotFoundException("Task with id " + taskId + " not found in todo with id " + todoId);
        }
        log.info("Get task by id " + taskId);
        return taskDto;
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@PathVariable("todo_id") long todoId,
                          @PathVariable(name = "user_id", required = false) Long userId,
                          @RequestBody TaskDto taskDto) {
        log.info("Check if todo with id " + todoId + " exist");
        if (todoService.readById(todoId) == null) {
            throw new EntityNotFoundException("Todo with id " + todoId + " not found");
        }
        log.info("Check if todo with id " + todoId + " belong to input user");
        if (userId != null) {
            ToDo toDo = todoService.readById(todoId);
            if (toDo.getOwner().getId() != userId.longValue()) {
                throw new EntityNotFoundException("Todo with id " + todoId + " not found in todos list of user with id " + userId);
            }
        }
        log.info("Create new task in todo list with id " + todoId);
        try {
            Priority validPriority = Priority.valueOf(taskDto.getPriority().toUpperCase());
        } catch (Exception e) {
            throw new EntityNotFoundException("Priority " + taskDto.getPriority() + " not exist");
        }
        Task task = TaskTransformer.convertToEntity(
                taskDto,
                todoService.readById(todoId),
                stateService.getByName("NEW")
        );
        task.setTodo(todoService.readById(todoId));
        task.setState(stateService.getByName("NEW"));
        task = taskService.create(task);
        taskDto = TaskTransformer.convertToDto(task);
        return taskDto;
    }

    @PutMapping("{task_id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDto update(@PathVariable("task_id") long taskId,
                          @PathVariable("todo_id") long todoId,
                          @PathVariable(name = "user_id", required = false) Long userId,
                          @RequestBody TaskDto taskDto) {
        log.info("Check if todo with id " + todoId + " exist");
        if (todoService.readById(todoId) == null) {
            throw new EntityNotFoundException("Todo with id " + todoId + " not found");
        }
        log.info("Check if todo with id " + todoId + " belong to input user");
        if (userId != null) {
            ToDo toDo = todoService.readById(todoId);
            if (toDo.getOwner().getId() != userId.longValue()) {
                throw new EntityNotFoundException("Todo with id " + todoId + " not found in todos list of user with id " + userId);
            }
        }
        Task task = taskService.readById(taskId);
        log.info("Check if input task belongs to todo with id " + todoId);
        if (task.getTodo().getId() != todoId) {
            throw new EntityNotFoundException("Task with id " + taskId + " not found in todo with id " + todoId);
        }
        try {
            Priority validPriority = Priority.valueOf(taskDto.getPriority().toUpperCase());
        } catch (Exception e) {
            throw new EntityNotFoundException("Priority " + taskDto.getPriority() + " not exist");
        }
        log.info("Update task with id " + taskId);
        taskDto.setId(taskId);
        task = TaskTransformer.convertToEntity(
                taskDto,
                todoService.readById(todoId),
                stateService.readById(taskDto.getStateId())
        );
        task = taskService.update(task);
        taskDto = TaskTransformer.convertToDto(task);
        return taskDto;
    }

    @DeleteMapping("{task_id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("task_id") long taskId,
                       @PathVariable("todo_id") long todoId,
                       @PathVariable(name = "user_id", required = false) Long userId) {
        log.info("Check if todo with id " + todoId + " exist");
        if (todoService.readById(todoId) == null) {
            throw new EntityNotFoundException("Todo with id " + todoId + " not found");
        }
        log.info("Check if todo with id " + todoId + " belong to input user");
        if (userId != null) {
            ToDo toDo = todoService.readById(todoId);
            if (toDo.getOwner().getId() != userId.longValue()) {
                throw new EntityNotFoundException("Todo with id " + todoId + " not found in todos list of user with id " + userId);
            }
        }
        Task task = taskService.readById(taskId);
        TaskDto taskDto = TaskTransformer.convertToDto(task);
        log.info("Check if input task belongs to todo with id " + todoId);
        if (taskDto.getTodoId() != todoId) {
            throw new EntityNotFoundException("Task with id " + taskId + " not found in todo with id " + todoId);
        }
        log.info("Delete task with id " + taskId);
        taskService.delete(taskId);
    }
}
