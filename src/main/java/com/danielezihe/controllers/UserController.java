package com.danielezihe.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.danielezihe.entities.ToDoEntity;
import com.danielezihe.entities.UserEntity;
import com.danielezihe.entities.util.UserEntityChangeableProperties;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.commons.collections4.map.LinkedMap;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author EZIHE S. DANIEL
 * CreatedAt: 09/10/2021
 */
public class UserController {
    private final OrderedMap<String, UserEntity> users;
    private static UserController userController;

    private UserController() {
        users = new LinkedMap<>();
    }


    public UserEntity save(String name, String email, String password) {
        String hashedPassword = hashPassword(password);
        int id = users.size() + 1;

        ToDoController toDoController = new ToDoController(new TreeBidiMap<>());
        UserEntity newUser = new UserEntity(id, name, email, hashedPassword, toDoController);

        if(users.get(email) == null)
            users.put(email, newUser);
        else
            throw new UnsupportedOperationException("User with email: " + email + " already exists");

        return newUser;
    }

    public <T> T login(String email, String password) {
        UserEntity user = users.get(email);

        if (user == null)
            return (T) "INVALID USER";

        if (!verifyPassword(password, user.getPassword())) {
            return (T) "INCORRECT DETAILS";
        } else {
            return (T) user;
        }
    }

    public UserEntity updateUser(String userEmail, String data, UserEntityChangeableProperties property) {
        UserEntity user = deleteUser(userEmail);

        switch (property) {
            case NAME -> user.setName(data);
            case EMAIL -> {
                if(users.get(data) == null)
                    user.setEmail(data);
                else
                    throw new UnsupportedOperationException("User with email: " + data + " already exists");
            }
            default -> throw new IllegalStateException("Unexpected value: " + property);
        }

        users.put(user.getEmail(), user);
        return user;
    }

    public UserEntity updateUser(String userEmail, String oldPassword, String newPassword) throws IllegalAccessException {
        var response = login(userEmail, oldPassword);

        if(response instanceof UserEntity) {
            String hashedNewPassword = hashPassword(newPassword);
            ((UserEntity) response).setPassword(hashedNewPassword);

            return (UserEntity) response;
        } else
            throw new IllegalAccessException("Old Password is incorrect!");
    }

    private UserEntity deleteUser(String userEmail) {
        return users.remove(userEmail);
    }


    private String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray());
    }

    private static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified;
    }

    public Stream<OrderedMap.Entry<String, UserEntity>> getAllUsersStream() {
        return users.entrySet().stream().sorted(OrderedMap.Entry.comparingByValue());
    }

    public static UserController getInstance() {
        if(userController == null) {
            synchronized (UserController.class) {
                if(userController == null) {
                    userController = new UserController();
                }
            }
        }
        return userController;
    }
}
