package com.danielezihe.entities;

import com.danielezihe.entities.util.ToDoStatus;

/**
 * @author EZIHE S. DANIEL
 * CreatedAt: 09/10/2021
 */
public class ToDoEntity implements Comparable<ToDoEntity> {
    private final int id;
    private String title;
    private String description;
    private ToDoStatus status;
    private final long createdAt;

    public ToDoEntity(int id, String title, String description, ToDoStatus status, long createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(ToDoStatus status) {
        this.status = status;
    }

    public ToDoStatus getStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public int compareTo(ToDoEntity o) {
        if(this.getStatus() == ToDoStatus.ACTIVE && o.getStatus() == ToDoStatus.COMPLETED)
            return -1;
        else
            return 1;
    }
}
