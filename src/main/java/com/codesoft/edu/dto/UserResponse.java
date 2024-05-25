package com.codesoft.edu.dto;

import com.codesoft.edu.model.ToDo;
import com.codesoft.edu.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Value
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    Long id;
    String firstName;
    String lastName;
    String email;
    String role;
    @JsonIgnore
    List<ToDo> myTodos; //need to implement ToDoResponse because of circular dependency
    @JsonIgnore
    List<ToDo> otherTodos; //need to implement ToDoResponse because of circular dependency
    public UserResponse(User user) {
        id = user.getId();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        email = user.getEmail();
        role = user.getRole().getName();
        myTodos = user.getMyTodos() != null ? new ArrayList<>(user.getMyTodos()) : Collections.emptyList();
        otherTodos = user.getOtherTodos() != null ? new ArrayList<>(user.getOtherTodos()) : Collections.emptyList();
    }
}

