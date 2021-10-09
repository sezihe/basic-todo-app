package com.danielezihe.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.danielezihe.entities.ToDoEntity;
import com.danielezihe.entities.UserEntity;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.commons.collections4.map.LinkedMap;

/**
 * @author EZIHE S. DANIEL
 * CreatedAt: 09/10/2021
 */
public class UserController {
    private OrderedMap<String, UserEntity> users;
    private static UserController userController;

    private UserController() {
        users = new LinkedMap<>();
    }

    public UserEntity save(String name, String email, String password) {
        String hashedPassword = hashPassword(password);
        int id = users.size() + 1;

        ToDoController toDoController = new ToDoController(new TreeBidiMap<>());
        UserEntity newUser = new UserEntity(id, name, email, hashedPassword, toDoController);

        users.put(email, newUser);

        return newUser;
    }

    public <T> T login(String email, String password) {
        UserEntity user = users.get(email);

        if (user == null)
            return (T) "INVALID USER";

        if (!verifyPassword(password, user.password())) {
            return (T) "INCORRECT DETAILS";
        } else {
            return (T) user;
        }
    }


    private String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray());
    }

    private static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified;
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
