package com.danielezihe;


import com.danielezihe.controllers.ToDoController;
import com.danielezihe.controllers.UserController;
import com.danielezihe.entities.ToDoEntity;
import com.danielezihe.entities.UserEntity;
import com.danielezihe.entities.util.CreatedAtQueryTypes;
import com.danielezihe.entities.util.ToDoStatus;
import com.danielezihe.entities.util.TodoEntityProperties;
import com.danielezihe.entities.util.UserEntityChangeableProperties;
import com.google.common.base.Preconditions;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * @author EZIHE S. DANIEL
 * CreatedAt: 09/10/2021
 */
public class Main {
    static Scanner scanner = new Scanner(System.in);
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int FOUR = 4;
    public static final int FIVE = 5;
    public static final int SIX = 6;
    public static final int SEVEN = 7;

    private static final UserController userController = UserController.getInstance();

    public static void main(String[] args) {
        println("Welcome!");
        goToMain();
    }

    static void handleRegisterUser() {
        println("Enter User Name, Email and Password all seperated with commas. Eg:Daniel,sezihe@gmail.com,This!sAStrongPassword");
        while (true) {
            String[] data = scanner.nextLine().split(",");
            String name = data[0];
            String email = data[1];
            String password = data[2];

            boolean isEmailValid = EmailValidator.getInstance().isValid(email);

            if (!isEmailValid)
                println("The email you entered is Invalid. Kindly enter a valid email while trying again");
            else {
                try {
                    // call guava methods to validate input
                    Preconditions.checkArgument(name.length() > 2, "Invalid User Name. Kindly enter a valid name");
                    Preconditions.checkArgument(password.length() >= 8, "Password is too short. Kindly enter a Password that is >= 8 characters");

                    UserEntity newUser = userController.save(name, email, password);
                    println("Registration Successful. Please login " + newUser.getName());
                    break;
                } catch (UnsupportedOperationException | NullPointerException | IllegalArgumentException ex) {
                    println(ex.getMessage());
                }
            }
        }
    }

    static void handleLoginAsAUser() {
        println("Kindly enter your email and password both seperated with a comma. Eg:sezihe@gmail.com,This!sAStrongPassword");
        while (true) {
            String[] data = scanner.nextLine().split(",");
            String email = data[0];
            String password = data[1];

            boolean isEmailValid = EmailValidator.getInstance().isValid(email);

            if (!isEmailValid)
                println("The email you entered is Invalid. Kindly enter a valid email");
            else {
                var response = userController.login(email, password);

                if (response instanceof UserEntity) {
                    println("Login Successful");
                    goToUserDashBoard((UserEntity) response);
                    break;
                } else {
                    println(response + ". Please try again");
                }
            }
        }

    }

    static void handleGetListOfAllUsers() {
        StringBuilder stringBuilder = new StringBuilder();
        Stream<Map.Entry<String, UserEntity>> allUsersStream = userController.getAllUsersStream();

        List<Map.Entry<String, UserEntity>> allUsersList = allUsersStream.toList();
        if (allUsersList.isEmpty()) {
            println("---------------------USERS----------------------");
            println("                   No Users.                    ");
            println("------------------------------------------------");
        } else {
            allUsersList.forEach(
                    user -> {
                        UserEntity userEntity = user.getValue();
                        String userStr = userEntity.getId() + "\t" + userEntity.getName() + "\t" + userEntity.getEmail();
                        appendLine(stringBuilder, userStr);
                    }
            );

            println("---------------------USERS----------------------");
            println(stringBuilder.toString());
            println("------------------------------------------------");
        }
    }

    static void handleAddNewTodo(UserEntity user) {
        println("Kindly enter Todo title and description both seperated by '::'. Eg: Go To The Gym::Aim to complete 15 push-ups at the Gym today");

        while (true) {
            String[] data = scanner.nextLine().split("::");
            String todoTitle = data[0];
            String todoDescription = data[1];

            try {
                Preconditions.checkArgument(todoTitle.length() >= 5, "Todo Title is too short. Kindly enter a Todo title >= 5 characters");
                Preconditions.checkArgument(todoDescription.length() >= 10, "Todo Description is too short. Kindly enter a Todo Description that is >= 10 characters");

                ToDoController mTodoController = user.getMyTodoController();
                mTodoController.createNewTodo(todoTitle, todoDescription);
                println("New Todo added successfully");
                break;
            } catch (NullPointerException | IllegalArgumentException ex) {
                println(ex.getMessage());
            }
        }
    }

    static void handleViewAllTodos(UserEntity user) {
        StringBuilder stringBuilder = new StringBuilder();
        ToDoController mTodoController = user.getMyTodoController();

        Stream<Map.Entry<Integer, ToDoEntity>> todoStream = mTodoController.getAllTodos();

        List<Map.Entry<Integer, ToDoEntity>> todoList = todoStream.toList();

        if (todoList.isEmpty()) {
            println("---------------------TODOS----------------------");
            println("                   No Todos.                    ");
            println("------------------------------------------------");
        } else {
            todoList.forEach(
                    todoEntity -> {
                        ToDoEntity toDo = todoEntity.getValue();
                        String status = toDo.getStatus() == ToDoStatus.ACTIVE ? "ACTIVE" : "COMPLETED";
                        appendLine(stringBuilder, "");
                        appendLine(stringBuilder, toDo.getId() + ". " + toDo.getTitle() + "\t\t\t" + status);
                        appendLine(stringBuilder, "DESCRIPTION:");
                        appendTab(stringBuilder, "- " + toDo.getDescription());
                        appendLine(stringBuilder, "CREATED AT:");
                        appendTab(stringBuilder, "- " + mTodoController.getCreatedAtDateTimeLong(toDo.getCreatedAt()));
                    }
            );

            println("---------------------TODOS----------------------");
            println(stringBuilder.toString());
            println("------------------------------------------------");
        }
    }

    static void handleViewAllActiveTodos(UserEntity user) {
        OrderedMap<Integer, ToDoEntity> activeTodos = user.getMyTodoController().getAllActiveTodos();

        if (activeTodos.isEmpty()) {
            println("---------------------Active TODOS----------------------");
            println("                   No Active Todos.                    ");
            println("-------------------------------------------------------");
        } else {
            StringBuilder stringBuilder = formatTodoString(user, activeTodos);

            println("---------------------Active TODOS----------------------");

            println(stringBuilder.toString());

            println("-------------------------------------------------------");
        }
    }

    static void handleViewAllCompletedTodos(UserEntity user) {
        OrderedMap<Integer, ToDoEntity> completedTodos = user.getMyTodoController().getAllCompletedTodos();

        if (completedTodos.isEmpty()) {
            println("---------------------Completed TODOS----------------------");
            println("                   No Completed Todos.                    ");
            println("----------------------------------------------------------");
        } else {
            StringBuilder stringBuilder = formatTodoString(user, completedTodos);

            println("---------------------Completed TODOS----------------------");

            println(stringBuilder.toString());

            println("-----------------------------------------------------------");
        }
    }

    static void handleFindTodo(UserEntity user) {
        ToDoController mTodoController = user.getMyTodoController();
        while (true) {
            printFindTodoPrompt();
            String[] input = scanner.nextLine().split("-");
            String query = input[0];

            if (query.equals("0")) {
                goToUserDashBoard(user);
                break;
            } else if(input.length == 1)
                println("Please include a tag in your Query!\n");
            else {
                String tag = input[1];
                switch (tag) {
                    case "n" -> {
                        OrderedMap<Integer, ToDoEntity> response = mTodoController.findTodo(query, TodoEntityProperties.TITLE, false);
                        printFindTodoSearchResult(response);
                    }
                    case "d" -> {
                        OrderedMap<Integer, ToDoEntity> response = mTodoController.findTodo(query, TodoEntityProperties.DESCRIPTION, false);
                        printFindTodoSearchResult(response);
                    }
                    case "nSt" -> {
                        OrderedMap<Integer, ToDoEntity> response = mTodoController.findTodo(query, TodoEntityProperties.TITLE, true);
                        printFindTodoSearchResult(response);
                    }
                    case "dSt" -> {
                        OrderedMap<Integer, ToDoEntity> response = mTodoController.findTodo(query, TodoEntityProperties.DESCRIPTION, true);
                        printFindTodoSearchResult(response);
                    }
                    case "dF" -> {
                        OrderedMap<Integer, ToDoEntity> response = mTodoController.findTodo(query, CreatedAtQueryTypes.FULL_DATE);
                        printFindTodoSearchResult(response);
                    }
                    case "dS" -> {
                        OrderedMap<Integer, ToDoEntity> response = mTodoController.findTodo(query, CreatedAtQueryTypes.SHORT_DATE);
                        printFindTodoSearchResult(response);
                    }
                    case "t" -> {
                        OrderedMap<Integer, ToDoEntity> response = mTodoController.findTodo(query, CreatedAtQueryTypes.TIME);
                        printFindTodoSearchResult(response);
                    }
                    case "dt" -> {
                        OrderedMap<Integer, ToDoEntity> response = mTodoController.findTodo(query, CreatedAtQueryTypes.DATE_TIME);
                        printFindTodoSearchResult(response);
                    }
                    default -> println("Tag not found. Please try again");
                }
            }
        }
    }

    static void handleUpdateDetails(UserEntity user) {
        String userEmail = user.getEmail();
        try {
            println("-YOUR DETAILS");
            println(user.toString());

            boolean shouldRun = true;
            while(shouldRun) {
                printUpdateDetailsPrompt();
                int input = scanner.nextInt();
                switch (input) {
                    case ONE -> {
                        println("Enter a new Email Address:");
                        scanner.nextLine();
                        String newEmail = scanner.nextLine();
                        if (newEmail.isBlank())
                            println("Email cannot be empty!");
                        else if(!EmailValidator.getInstance().isValid(newEmail))
                            println("Invalid Email");
                        else if (newEmail.equals(userEmail))
                            println("Please enter a NEW email!");
                        else {
                            UserEntity updatedUser = userController.updateUser(userEmail, newEmail, UserEntityChangeableProperties.EMAIL);
                            println("Email updated!\n\n-NEW DETAILS");
                            userEmail = updatedUser.getEmail();
                            println(user.toString());
                        }
                    }
                    case TWO -> {
                        println("Enter a new Name");
                        scanner.nextLine();
                        String newName = scanner.nextLine();
                        if(newName.isBlank() || newName.length() < 3)
                            println("New name cannot be empty or less than 3!");
                        else if (newName.equals(user.getName()))
                            println("Please enter a NEW name!");
                        else {
                            UserEntity updatedUser = userController.updateUser(userEmail, newName, UserEntityChangeableProperties.NAME);
                            println("Name updated!\n\n-NEW DETAILS");
                            println(user.toString());
                        }
                    }
                    case THREE -> {
                        println("Kindly enter your old password and a new password both seperated by '::'. Eg This!sAStrongPassword::Th!s!sASTRONGERPassword");
                        scanner.nextLine();
                        String[] data = scanner.nextLine().split("::");
                        if(data.length <= 1)
                            println("Please enter your old password and a new password");
                        else {
                            String oldPassword = data[0];
                            String newPassword = data[1];
                            if(oldPassword.equals(newPassword))
                                println("Please enter a NEW Password");
                            else {
                                userController.updateUser(userEmail, oldPassword, newPassword);
                                println("Password has been changed successfully");
                            }
                        }
                    }
                    case FOUR -> {
                        goToUserDashBoard(user);
                        shouldRun = false;
                    }
                    default -> println("Please enter a valid option");
                }
            }
        } catch (UnsupportedOperationException | IllegalAccessException | IllegalStateException e) {
            println(e.getMessage());
        }
    }

    static void handleToggleTodoStatus(UserEntity user) {
        while (true) {
            ToDoController mTodoController = user.getMyTodoController();
            println("Enter number beside a Todo to toggle the Status. Separate multiple numbers with a comma. Eg: 1 Eg: 1,2\n" +
                    "Enter 0 to go back.");
            String[] data = scanner.nextLine().split(",");
            if (data[0].equals("0")) {
                break;
            } else {
                for (String todoNumStr : data) {
                    int id = Integer.parseInt(todoNumStr);
                    ToDoEntity todo = mTodoController.getTodo(id);
                    ToDoStatus status = todo.getStatus() == ToDoStatus.ACTIVE ? ToDoStatus.COMPLETED : ToDoStatus.ACTIVE;
                    mTodoController.updateTodo(id, status);
                    println("Todo with id '" + todoNumStr + "' has been updated. Status set to " + (status == ToDoStatus.ACTIVE ? "ACTIVE" : "COMPLETED"));
                }
                handleViewAllTodos(user);
            }
        }
    }

    static void handleLogout() {
        goToMain();
    }

    static void goToMain() {
        while (true) {
            printFirstPrompt();
            int input = scanner.nextInt();
            scanner.nextLine();
            switch (input) {
                case ONE -> handleRegisterUser();
                case TWO -> handleLoginAsAUser();
                case THREE -> handleGetListOfAllUsers();
                case FOUR -> System.exit(0);
                default -> println("Please enter a valid option");
            }
        }
    }

    static void goToUserDashBoard(UserEntity user) {
        boolean isRunning = true;
        while (isRunning) {
            printUserDashBoardPrompt(user.getName());
            int input = scanner.nextInt();
            scanner.nextLine();
            switch (input) {
                case ONE -> handleAddNewTodo(user);
                case TWO -> {
                    handleViewAllTodos(user);
                    handleToggleTodoStatus(user);
                }
                case THREE -> handleViewAllActiveTodos(user);
                case FOUR -> handleViewAllCompletedTodos(user);
                case FIVE -> handleFindTodo(user);
                case SIX -> handleUpdateDetails(user);
                case SEVEN -> {
                    isRunning = false;
                    handleLogout();
                }
                default -> println("Please enter a valid option");
            }
        }
    }

    // UTILITIES
    static <T> void println(T value) {
        System.out.println(value);
    }

    static void printFirstPrompt() {
        println("""
                -MAIN MENU-
                Press 1 to Register a new User
                Press 2 to Login as a User
                Press 3 to see all Users
                Press 4 to Quit.""");
    }

    static void printUserDashBoardPrompt(String userName) {
        println("\n-TODO DASHBOARD-\n" +
                "Hello, " + userName + "!\n" +
                "Press 1 to add a new Todo\n" +
                "Press 2 to view all Todos\n" +
                "Press 3 to view all Active Todos\n" +
                "Press 4 to view all Completed Todos\n" +
                "Press 5 to search for a Todo\n" +
                "Press 6 to edit/update your details\n" +
                "Press 7 to logout.");
    }

    static void printFindTodoPrompt() {
        println("Kindly input the search query. Use tags to search in specific categories. Eg: 10/10/2021-dS Eg: Go to-n");
        println("""
                
                Tags:
                -n: Title/Name (Eg: Go to the Gym)
                -d: Description/Details (Eg: at the Gym)
                -nSt: Title/Name using Strict query. (Finds a specific Todo with title using a user-provided 'FULL' title)
                -dSt: Description/Details using Strict query. (Finds a specific Todo with Description using a user-provided 'FULL' description)
                -dF: Full Date (Eg: Sunday 10 October 2021)
                -dS: Short Date (Eg: 10/10/2021)
                -t: Time in 24hr format (Eg: 10:30)
                -dt: Date and time (Eg: 10/10/2021 10:30)
                Press 0 to go back.""");
    }

    static void printUpdateDetailsPrompt() {
        println("""
                
                Press 1 to change your email
                Press 2 to change your name
                Press 3 to change your password
                Press 4 to go back
                """);
    }

    static void printFindTodoSearchResult(OrderedMap<Integer, ToDoEntity> result) {
        StringBuilder stringBuilder = new StringBuilder();
        println("-QUERY RESULT");
        if (result.isEmpty()) {
            println("---------------------TODOS----------------------");
            println("                   No Todos.                    ");
            println("------------------------------------------------");
        } else {
            for (Map.Entry<Integer, ToDoEntity> entry : result.entrySet()) {
                ToDoEntity toDoEntity = entry.getValue();
                String status = toDoEntity.getStatus() == ToDoStatus.ACTIVE ? "ACTIVE" : "COMPLETED";
                appendLine(stringBuilder, "\n" + toDoEntity.getId() + ". " + toDoEntity.getTitle() + "\t\t\t" + status);
                appendLine(stringBuilder, "DESCRIPTION:");
                appendTab(stringBuilder, "- " + toDoEntity.getDescription());
            }

            println("---------------------TODOS----------------------");
            println(stringBuilder.toString());
            println("------------------------------------------------");
        }
    }

    static StringBuilder formatTodoString(UserEntity user, OrderedMap<Integer, ToDoEntity> todos) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, ToDoEntity> todoEntry : todos.entrySet()) {
            appendLine(stringBuilder, "");
            ToDoEntity activeTodo = todoEntry.getValue();
            appendLine(stringBuilder, activeTodo.getTitle());
            appendLine(stringBuilder, "DESCRIPTION:");
            appendTab(stringBuilder, "- " + activeTodo.getDescription());
            appendLine(stringBuilder, "CREATED AT:");
            appendTab(stringBuilder, "- " + user.getMyTodoController().getCreatedAtDateTimeLong(activeTodo.getCreatedAt()));
        }

        return stringBuilder;
    }

    static StringBuilder appendLine(StringBuilder builder, String line) {
        return builder.append(line).append("\n");
    }

    static StringBuilder appendTab(StringBuilder builder, String line) {
        return builder.append("\t").append(line).append("\n");
    }
}
