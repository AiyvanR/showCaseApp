package com.example.showcaseapp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class TasksControllerIT{

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TaskRepository repository;

    @AfterEach
    void tearDown(){
        this.repository.findAll().clear();
    }

    @Test
    void getTasks_ReturnsValidResponseEntity() throws Exception{
        //given
        var requestBuilder = get("/api/tasks/");
        this.repository.findAll().addAll(List.of(new Task(UUID.fromString("3549e03f-b0c2-4aef-9642-7006105de908"), "1 задача", false),
                new Task(UUID.fromString("2246f0d5-d910-4dc6-a497-caf15f6a2eb8"), "2 задача", true)));

        //when
        this.mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                        [
                        {
                        "id": "3549e03f-b0c2-4aef-9642-7006105de908",
                        "details": "1 задача",
                        "completed": false
                        },
                        {
                        "id": "2246f0d5-d910-4dc6-a497-caf15f6a2eb8",
                        "details": "2 задача",
                        "completed": true
                        }
                        ]
""")
                );
    }

    @Test
    void handleCreateNewTask_PayloadIsValid_ReturnsValidResponseEntity() throws Exception{
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/tasks/addTask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "details": "Третья задача"
                        }
                    
                        """);
        //when
        this.mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        header().exists(HttpHeaders.LOCATION),
                        content().json("""
                            {
                            "details": "Третья задача",
                            "completed": false
                            }
                            """),
                        jsonPath("$.id").exists()
                );

                assertEquals(1, this.repository.findAll().size());
                final var task = this.repository.findAll().get(0);
                assertNotNull(task.id());
                assertEquals("Третья задача", this.repository.findAll().get(0).details());
                assertFalse(this.repository.findAll().get(0).completed());

    }

    @Test
    void addTask_PayloadIsInvalid_ReturnsInvalidResponseEntity() throws Exception{
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/tasks/addTask")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
                .content("""
                        {
                        "details": null
                        }
                        """);
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        header().doesNotExist(HttpHeaders.LOCATION),
                        content().json("""
                            {
                            "err": ["Task details must be set"]
                            }
                            """, true)
                );

        assertTrue(this.repository.findAll().isEmpty());
    }
}