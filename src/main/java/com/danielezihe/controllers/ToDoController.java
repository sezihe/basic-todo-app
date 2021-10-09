package com.danielezihe.controllers;

import com.danielezihe.entities.ToDoEntity;
import com.danielezihe.entities.util.ToDoStatus;
import com.danielezihe.entities.util.TodoEntityProperties;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.io.IOCase;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author EZIHE S. DANIEL
 * CreatedAt: 09/10/2021
 */
public final class ToDoController {
    private final BidiMap<Integer, ToDoEntity> todos;
    List<ToDoEntity> todosList;

    public ToDoController(BidiMap<Integer, ToDoEntity> todos) {
        this.todos = todos;
        todosList = new ArrayList<>(todos.values());
    }

    public ToDoEntity createNewTodo(String title, String description, String status) {
        int id = todos.size() + 1;
        long createdAt = Timestamp.from(Instant.now()).getTime();

        ToDoEntity newTodo = new ToDoEntity(id, title, description, ToDoStatus.ACTIVE, createdAt);

        todos.put(id, newTodo);

        return newTodo;
    }

    public OrderedMap<Integer, ToDoEntity> getAllActiveTodos() {
        OrderedMap<Integer, ToDoEntity> activeTodos = new LinkedMap<>();

        todosList.stream().filter(o -> o.getStatus().equals(ToDoStatus.ACTIVE)).forEach(
                o -> {
                    activeTodos.put(o.getId(), o);
                }
        );

        return activeTodos;
    }

    public OrderedMap<Integer, ToDoEntity> getAllCompletedTodos() {
        OrderedMap<Integer, ToDoEntity> completedTodos = new LinkedMap<>();

        todosList.stream().filter(todo -> todo.getStatus().equals(ToDoStatus.COMPLETED)).forEach(
                todo -> {
                    completedTodos.put(todo.getId(), todo);
                }
        );

        return completedTodos;
    }

    public OrderedMap<Integer, ToDoEntity> findTodo(String query, TodoEntityProperties property) {
        OrderedMap<Integer, ToDoEntity> todos = new LinkedMap<>();
        Stream<ToDoEntity> todosStream;

        switch (property) {
            case ID -> {
                ToDoEntity todo = this.todos.get(Integer.parseInt(query));
                todos.put(todo.getId(), todo);
                return todos;
            }
            case TITLE -> {
                todosStream = todosList.stream().filter(todo -> IOCase.INSENSITIVE.checkEquals(todo.getTitle(), query));
            }
            case DESCRIPTION -> {
                todosStream = todosList.stream().filter(todo -> IOCase.INSENSITIVE.checkEquals(todo.getDescription(), query));
            }
            default -> throw new IllegalStateException("Unexpected value: " + property);
        }

        todosStream.forEach(
                todo -> {
                    todos.put(todo.getId(), todo);
                }
        );

        return todos;
    }

    public void updateTodo(int todoId, String data, TodoEntityProperties property) {
        ToDoEntity todo = deleteTodo(todoId);

        switch (property) {
            case TITLE -> todo.setTitle(data);
            case DESCRIPTION -> todo.setDescription(data);
        }

        todos.put(todoId, todo);
    }

    public void updateTodo(int todoId, ToDoStatus status) {
        ToDoEntity todo = deleteTodo(todoId);

        todo.setStatus(status);

        todos.put(todoId, todo);
    }

    public ToDoEntity deleteTodo(int todoId) {
        return todos.remove(todoId);
    }

    public BidiMap<Integer, ToDoEntity> getTodos() {
        return todos;
    }
}
