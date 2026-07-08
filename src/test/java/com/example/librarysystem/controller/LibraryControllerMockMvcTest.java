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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LibrarySystemApplication.class)
@ActiveProfiles("test")
class LibraryControllerMockMvcTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookEditionRepository bookEditionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

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
    void registerBook_withoutPriorEdition_shouldCreateEditionAndBook() throws Exception {
        var request = bookRequest("978-3-16-148410-0", "Test-Driven Development", "Kent Beck");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.isbn").value("978-3-16-148410-0"))
                .andExpect(jsonPath("$.title").value("Test-Driven Development"))
                .andExpect(jsonPath("$.author").value("Kent Beck"))
                .andExpect(jsonPath("$.available").value(true));

        assertThat(bookRepository.count()).isOne();
        assertThat(bookEditionRepository.count()).isOne();
    }

    @Test
    void registerBook_withExistingEdition_shouldCreateAnotherBookCopy() throws Exception {
        var first = bookRequest("978-0-13-468599-1", "Clean Architecture", "Robert C. Martin");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        var second = bookRequest("978-0-13-468599-1", "Clean Architecture", "Robert C. Martin");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.isbn").value("978-0-13-468599-1"))
                .andExpect(jsonPath("$.title").value("Clean Architecture"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.available").value(true));

        assertThat(bookRepository.count()).isEqualTo(2);
        assertThat(bookEditionRepository.count()).isOne();
    }

    @Test
    void registerBook_withSameIsbnButDifferentTitle_shouldReject() throws Exception {
        var first = bookRequest("978-0-201-63361-0", "Domain-Driven Design", "Eric Evans");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        var second = bookRequest("978-0-201-63361-0", "Domain Driven Design", "Eric Evans");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("978-0-201-63361-0")));
    }

    @Test
    void getAllBooks_shouldReturnPaginatedResponse() throws Exception {
        var request = bookRequest("978-0-00-000000-2", "Another Book", "Another Author");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
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
