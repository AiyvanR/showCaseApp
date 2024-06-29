package com.example.showcaseapp;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TaskRepository {

    List<Task> findAll();

    void save(Task task);

    Optional<Task> getTask(UUID id);

}
