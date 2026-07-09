package com.example.librarysystem.controller;

import com.example.librarysystem.LibrarySystemApplication;
import com.example.librarysystem.dto.RegisterBookRequest;
import com.example.librarysystem.dto.RegisterBorrowerRequest;
import com.example.librarysystem.model.BorrowStatus;
import com.example.librarysystem.repository.BookEditionRepository;
import com.example.librarysystem.repository.BookRepository;
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
class BorrowRecordControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookEditionRepository bookEditionRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        bookEditionRepository.deleteAll();
        borrowerRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private RegisterBookRequest bookRequest(String isbn, String title, String author) {
        var req = new RegisterBookRequest();
        req.setIsbn(isbn);
        req.setTitle(title);
        req.setAuthor(author);
        return req;
    }

    private RegisterBorrowerRequest borrowerRequest(String name, String email) {
        var req = new RegisterBorrowerRequest();
        req.setName(name);
        req.setEmail(email);
        return req;
    }

    @Test
    void borrowAndReturnBook_shouldSucceed() throws Exception {
        var bookReq = bookRequest("978-1-11-111111-1", "Borrow Test", "Author A");
        String bookJson = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long bookId = objectMapper.readTree(bookJson).get("id").asLong();

        var borrowerReq = borrowerRequest("Bob", "bob@test.com");
        String borrowerJson = mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowerReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long borrowerId = objectMapper.readTree(borrowerJson).get("id").asLong();

        mockMvc.perform(post("/api/borrowers/{borrowerId}/borrow/{bookId}", borrowerId, bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Book borrowed successfully"));

        assertThat(bookRepository.findById(bookId).orElseThrow().isAvailable()).isFalse();
        assertThat(borrowRecordRepository
                .findByBorrowerIdAndBookIdAndStatus(borrowerId, bookId, BorrowStatus.BORROWED))
                .isPresent();

        mockMvc.perform(post("/api/borrowers/{borrowerId}/return/{bookId}", borrowerId, bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Book returned successfully"));

        assertThat(borrowRecordRepository
                .findByBorrowerIdAndBookIdAndStatus(borrowerId, bookId, BorrowStatus.RETURNED))
                .isPresent();
        assertThat(bookRepository.findById(bookId).orElseThrow().isAvailable()).isTrue();
    }

    @Test
    void borrowUnavailableBook_shouldReject() throws Exception {
        var bookReq = bookRequest("978-2-22-222222-2", "Unavailable Test", "Author B");
        String bookJson = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long bookId = objectMapper.readTree(bookJson).get("id").asLong();

        var borrowerReq = borrowerRequest("Carol", "carol@test.com");
        String borrowerJson = mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowerReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long borrowerId = objectMapper.readTree(borrowerJson).get("id").asLong();

        mockMvc.perform(post("/api/borrowers/{borrowerId}/borrow/{bookId}", borrowerId, bookId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/borrowers/{borrowerId}/borrow/{bookId}", borrowerId, bookId))
                .andExpect(status().isConflict());
    }
}
