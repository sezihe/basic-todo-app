package com.danielezihe.entities;

import com.danielezihe.controllers.ToDoController;

/**
 * @author EZIHE S. DANIEL
 * CreatedAt: 09/10/2021
 */
public final class UserEntity implements Comparable<UserEntity> {
    private final int id;
    private String name;
    private String email;
    private transient String password;
    private final transient ToDoController myTodoController;

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

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public ToDoController getMyTodoController() {
        return myTodoController;
    }

    @Override
    public int compareTo(UserEntity o) {
        return Integer.compare(this.getId(), o.getId());
    }

    @Override
    public String toString() {
        return "User {" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
