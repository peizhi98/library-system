package com.example.librarysystem.controller;

import com.example.librarysystem.LibrarySystemApplication;
import com.example.librarysystem.dto.RegisterBorrowerRequest;
import com.example.librarysystem.repository.BorrowRecordRepository;
import com.example.librarysystem.repository.BorrowerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LibrarySystemApplication.class)
@ActiveProfiles("test")
class BorrowerControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        borrowRecordRepository.deleteAll();
        borrowerRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private RegisterBorrowerRequest borrowerRequest(String name, String email) {
        var req = new RegisterBorrowerRequest();
        req.setName(name);
        req.setEmail(email);
        return req;
    }

    @Test
    void registerBorrower_shouldCreateAndReturnBorrower() throws Exception {
        var request = borrowerRequest("Alice Smith", "alice@example.com");

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        assertThat(borrowerRepository.count()).isOne();
    }
}
