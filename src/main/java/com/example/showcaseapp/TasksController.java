package com.example.showcaseapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TasksController {

    private final TaskRepository taskRepository;
    private final MessageSource messageSource;

    @Autowired
    public TasksController(TaskRepository taskRepository, MessageSource source) {
        this.taskRepository = taskRepository;
        this.messageSource = source;
    }

    @GetMapping("/")
    public ResponseEntity<List<Task>> getTasks(){
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.taskRepository.findAll());
    }

    @PostMapping("/addTask")
    public ResponseEntity<?> addTask(@RequestBody NewTaskPayload payload, UriComponentsBuilder uriComponentsBuilder, Locale locale){
        if(payload.details() == null || payload.details().isBlank()) {
            final var message = this.messageSource.getMessage("tasks.create.details.error.not_set", new Object[0] , locale);
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(new ErrorsPresentation(List.of(message)));
        }else {
            Task task = new Task(payload.details());
            this.taskRepository.save(task);
            return ResponseEntity.created(uriComponentsBuilder.path("/api/tasks/{taskId}")
                            .build(Map.of("taskId", task.id()))).contentType(MediaType.APPLICATION_JSON)
                    .body(task);
        }
    }

    @GetMapping("/getTask/{id}")
    public ResponseEntity<Task> getTask(@PathVariable(name = "id")UUID id){
        return ResponseEntity.of(this.taskRepository.getTask(id));
    }
}
