package com.danielezihe.entities;

import com.danielezihe.controllers.ToDoController;

/**
 * @author EZIHE S. DANIEL
 * CreatedAt: 09/10/2021
 */
public final class UserEntity {
    private final int id;
    private String name;
    private String email;
    private String password;
    private final ToDoController myTodoController;

    public UserEntity(int id, String name, String email, String password, ToDoController myTodoController) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.myTodoController = myTodoController;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
