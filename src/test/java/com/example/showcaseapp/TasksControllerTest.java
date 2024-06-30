package com.example.showcaseapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TasksControllerTest {

    @Mock
    TaskRepository taskRepository;
    @Mock
    MessageSource source;

    @InjectMocks
    TasksController controller;

    //module test
    @Test
    @DisplayName("GET /api/tasks/getTasks возвращает HTTP-ответ со статусом 200 OK и списком задач")
    void getTasks_ReturnsValidResponseEntity(){
        //given
        var tasks = List.of(new Task(UUID.randomUUID(), "1 задача", false),
        new Task(UUID.randomUUID(), "2 задача", true));

        doReturn(tasks).when(this.taskRepository).findAll();

        //when
        var responseEntity = this.controller.getTasks();

        //then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(tasks, responseEntity.getBody());
    }

    @Test
    void addTask_PayloadIsInvalid_ReturnsInvalidResponseEntity(){
        //given
        var details = "  ";
        var locale = Locale.US;
        var errorMessage = "Details is empty";

        doReturn(errorMessage).when(this.source).getMessage("tasks.create.details.error.not_set",new Object[0], locale);

        //when
        var responseEntity = this.controller.addTask(new NewTaskPayload(details),
                UriComponentsBuilder.fromUriString("http://localhost:8080"), locale);
        //then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(new ErrorsPresentation(List.of(errorMessage)), responseEntity.getBody());

        verifyNoInteractions(taskRepository);
    }

    @Test
    void addTask_PayloadIsValid_ReturnsValidResponseEntity(){
        //given
        var details = "Третья задача";
        //when
        var responseEntity = this.controller.addTask(new NewTaskPayload(details),
                UriComponentsBuilder.fromUriString("http://localhost:8080"), Locale.ENGLISH);
        //then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        if(responseEntity.getBody() instanceof Task task){
            assertNotNull(task.id());
            assertEquals(details, task.details());
            assertFalse(task.completed());

            assertEquals(URI.create("http://localhost:8080/api/tasks/"+task.id()),responseEntity.getHeaders().getLocation());

            verify(this.taskRepository).save(task);
        }else{
            assertInstanceOf(Task.class, responseEntity.getBody());
        }

        verifyNoMoreInteractions(this.taskRepository);
    }
}