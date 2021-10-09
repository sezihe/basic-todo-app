package com.danielezihe.entities;

import com.danielezihe.controllers.ToDoController;

/**
 * @author EZIHE S. DANIEL
 * CreatedAt: 09/10/2021
 */
public record UserEntity(int id, String name, String email, String password,
                         ToDoController myTodoController) {
}
