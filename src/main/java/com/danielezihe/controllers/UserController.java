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

    /**
     * Adds a user to the users OrderedMap.
     * @param name Username to be saved
     * @param email User email to be saved
     * @param password User password to be saved
     * @return the {@link UserEntity} that was saved
     * @throws UnsupportedOperationException if a User tries to create an account with an Email that already exists
     */
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

    /**
     * Logs a user in by verifying the users Email and Password against the record
     * stored in the OrderedMap
     * @param email email of user to be logged in
     * @param password password of user to be logged in
     * @param <T>
     * @return {@link UserEntity} instance of the User if login is successful
     * "INVALID_USER" if user email is not found.
     * "INCORRECT_DETAILS" if password is wrong.
     */
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

    /**
     * Updates a user's data
     * @param userEmail Email of the user to be updated
     * @param data New data to be saved
     * @param property Property of the user to be updated
     * @return {@link UserEntity} instance with the updated User details
     * @throws UnsupportedOperationException if a User tries to update Email with an already existing Email
     * @throws IllegalStateException if a User inputs an unexpected value for @Param(property)
     */
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

    /**
     * Updates a user's password
     * @param userEmail Email of the user to be updated
     * @param oldPassword User's old password
     * @param newPassword  New password to be saved
     * @return {@link UserEntity} instance of the updated User
     * @throws IllegalAccessException if Old password is incorrect
     */
    public UserEntity updateUser(String userEmail, String oldPassword, String newPassword) throws IllegalAccessException {
        var response = login(userEmail, oldPassword);

        if(response instanceof UserEntity) {
            String hashedNewPassword = hashPassword(newPassword);
            ((UserEntity) response).setPassword(hashedNewPassword);

            return (UserEntity) response;
        } else
            throw new IllegalAccessException("Old Password is incorrect!");
    }

    /**
     * Removes a {@link UserEntity} user from the users OrderedMap.
     * @param userEmail email of User to be removed
     * @return the recently deleted {@link UserEntity} user
     */
    private UserEntity deleteUser(String userEmail) {
        return users.remove(userEmail);
    }

    /**
     * Hashes a password String using BCrypt.
     * @param password Password to be hashed
     * @return The hashed password as a String
     */
    private String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray());
    }

    /**
     * Verifies a password hash with a password string
     * @param password password string
     * @param hashedPassword password hash
     * @return true if password string and hash match else returns false
     */
    private static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified;
    }

    /**
     * Gets all Users from the users OrderedMap
     * @return a Stream of users OrderedMap entry set.
     */
    public Stream<OrderedMap.Entry<String, UserEntity>> getAllUsersStream() {
        return users.entrySet().stream().sorted(OrderedMap.Entry.comparingByValue());
    }

    /**
     * Singleton. Create a new Instance of {@link UserController} class if non exists
     * else return an already created instance
     * @return An instance of {@link UserController} class.
     */
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
