package com.danielezihe.controllers;

import com.danielezihe.entities.ToDoEntity;
import com.danielezihe.entities.util.CreatedAtQueryTypes;
import com.danielezihe.entities.util.ToDoStatus;
import com.danielezihe.entities.util.TodoEntityProperties;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.io.IOCase;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
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
    }

    public ToDoEntity createNewTodo(String title, String description) {
        int id = todos.size() + 1;
        // long createdAt = Timestamp.from(Instant.now()).getTime();
        Calendar calendar = Calendar.getInstance(Locale.UK);
        long createdAt = calendar.getTimeInMillis();

        ToDoEntity newTodo = new ToDoEntity(id, title, description, ToDoStatus.ACTIVE, createdAt);

        todos.put(id, newTodo);

        return newTodo;
    }

    public OrderedMap<Integer, ToDoEntity> getAllActiveTodos() {
        OrderedMap<Integer, ToDoEntity> activeTodos = new LinkedMap<>();
        todosList = new ArrayList<>(todos.values());

        todosList.stream().filter(o -> o.getStatus() == ToDoStatus.ACTIVE).forEach(
                o -> {
                    activeTodos.put(o.getId(), o);
                }
        );

        return activeTodos;
    }

    public OrderedMap<Integer, ToDoEntity> getAllCompletedTodos() {
        OrderedMap<Integer, ToDoEntity> completedTodos = new LinkedMap<>();
        todosList = new ArrayList<>(todos.values());

        todosList.stream().filter(todo -> todo.getStatus() == ToDoStatus.COMPLETED).forEach(
                todo -> {
                    completedTodos.put(todo.getId(), todo);
                }
        );

        return completedTodos;
    }

    public OrderedMap<Integer, ToDoEntity> findTodo(String query, TodoEntityProperties property, boolean useStrict) {
        OrderedMap<Integer, ToDoEntity> queryTodos = new LinkedMap<>();
        Stream<ToDoEntity> todosStream;
        todosList = new ArrayList<>(todos.values());

        switch (property) {
            case ID -> {
                ToDoEntity todo = this.todos.get(Integer.parseInt(query));
                queryTodos.put(todo.getId(), todo);
                return queryTodos;
            }
            case TITLE -> {
                if(useStrict)
                    todosStream = todosList.stream().filter(todo -> IOCase.INSENSITIVE.checkEquals(todo.getTitle(), query));
                else
                    todosStream = todosList.stream().filter(todo -> todo.getTitle().toLowerCase().contains(query.toLowerCase()));
            }
            case DESCRIPTION -> {
                if(useStrict)
                    todosStream = todosList.stream().filter(todo -> IOCase.INSENSITIVE.checkEquals(todo.getDescription(), query));
                else
                    todosStream = todosList.stream().filter(todo -> todo.getDescription().toLowerCase().contains(query.toLowerCase()));
            }
            default -> throw new IllegalStateException("Unexpected value: " + property);
        }

        todosStream.forEach(
                todo -> {
                    queryTodos.put(todo.getId(), todo);
                }
        );

        return queryTodos;
    }

    public OrderedMap<Integer, ToDoEntity> findTodo(String query, CreatedAtQueryTypes queryType) {
        OrderedMap<Integer, ToDoEntity> queryTodos = new LinkedMap<>();
        Stream<ToDoEntity> todosStream;
        todosList = new ArrayList<>(todos.values());

        switch (queryType) {
            case FULL_DATE -> {
                todosStream = todosList.stream().filter(o -> IOCase.INSENSITIVE.checkEquals(query, getDateString(o.getCreatedAt(), CreatedAtQueryTypes.FULL_DATE)));
            }
            case SHORT_DATE -> {
                todosStream = todosList.stream().filter(o -> IOCase.INSENSITIVE.checkEquals(query, getDateString(o.getCreatedAt(), CreatedAtQueryTypes.SHORT_DATE)));
            }
            case TIME -> {
                todosStream = todosList.stream().filter(o -> IOCase.INSENSITIVE.checkEquals(query, getDateString(o.getCreatedAt(), CreatedAtQueryTypes.TIME)));
            }
            case DATE_TIME -> {
                todosStream = todosList.stream().filter(o -> IOCase.INSENSITIVE.checkEquals(query, getCreatedAtDateTimeShort(o.getCreatedAt())));
            }
            default -> {
                return queryTodos;
            }
        }

        todosStream.forEach(
                o -> {
                    queryTodos.put(o.getId(), o);
                }
        );

        return queryTodos;
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

    public Stream<Map.Entry<Integer, ToDoEntity>> getAllTodos() {
        return todos.entrySet().stream().sorted(Map.Entry.comparingByValue());
    }

    public ToDoEntity getTodo(int id) {
        return todos.get(id);
    }

    public String getCreatedAtDateTimeLong(long milliseconds) {
        String fullDate = getDateString(milliseconds, CreatedAtQueryTypes.FULL_DATE);
        String time = getDateString(milliseconds, CreatedAtQueryTypes.TIME);

        return fullDate + " " + time;
    }

    private String getCreatedAtDateTimeShort(long milliseconds) {
        String shortDate = getDateString(milliseconds, CreatedAtQueryTypes.SHORT_DATE);
        String time = getDateString(milliseconds, CreatedAtQueryTypes.TIME);

        return shortDate + " " + time;
    }

    private String getDateString(long milliseconds, CreatedAtQueryTypes queryType) {
        //SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        StringBuilder stringBuilder = new StringBuilder();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(milliseconds);

        LocalDateTime localDateTime = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
        switch (queryType) {
            case FULL_DATE -> {
                stringBuilder = new StringBuilder().append(localDateTime.getDayOfWeek())
                        .append(" ").append(localDateTime.getDayOfMonth())
                        .append(" ").append(localDateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.UK))
                        .append(" ").append(localDateTime.getYear());
            }
            case SHORT_DATE -> {
                String dateFormat = "dd/MM/yyyy";
                String dateStr = DateTimeFormatter.ofPattern(dateFormat).format(localDateTime.toLocalDate());
                stringBuilder = new StringBuilder().append(dateStr);
            }
            case TIME -> {
                String timeFormat = "HH:mm";
                String timeStr = DateTimeFormatter.ofPattern(timeFormat).format(localDateTime.toLocalTime());
                stringBuilder = new StringBuilder().append(timeStr);
            }
        }

        return stringBuilder.toString();
    }
}
