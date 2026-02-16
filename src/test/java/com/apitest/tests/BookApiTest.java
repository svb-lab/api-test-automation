package com.apitest.tests;

import com.apitest.base.BaseTest;
import com.apitest.helpers.AssertionHelper;
import com.apitest.helpers.BookTestDataBuilder;
import com.apitest.models.Book;
import com.apitest.models.ErrorResponse;
import com.apitest.services.BookService;
import com.github.javafaker.Faker;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Books API Tests")
class BookApiTest extends BaseTest {

    @Autowired
    private BookService bookService;

    @Test
    @Tag("smoke")
    @Tag("regression")
    @DisplayName("Should return all books successfully")
    void getAllBooks() {
        Response response = bookService.getAll();
        List<Book> books = List.of(response.as(Book[].class));

        assertAll("Get all books",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains(CONTENT_TYPE_JSON), "Content type should be application/json"),
                () -> assertTrue(response.getTime() < 5000, "Response time should be under 5000ms, was: " + response.getTime() + "ms"),
                () -> assertNotNull(books, "Books list should not be null"),
                () -> assertFalse(books.isEmpty(), "Books list should not be empty"),
                () -> books.forEach(AssertionHelper::assertBookHasRequiredFields)
        );
    }

    @Test
    @Tag("smoke")
    @Tag("regression")
    @DisplayName("Should return book by valid ID")
    void getBookById() {
        Integer bookId = 1;

        Response response = bookService.getById(bookId);
        Book book = response.as(Book.class);

        assertAll("Get book by ID",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains(CONTENT_TYPE_JSON), "Content type should be application/json"),
                () -> assertEquals(bookId, book.getId(), "Book ID should match requested ID"),
                () -> assertNotNull(book.getTitle(), "Book title should not be null"),
                () -> assertNotNull(book.getDescription(), "Book description should not be null"),
                () -> assertNotNull(book.getPageCount(), "Book page count should not be null")
        );
    }

    @Test
    @Tag("smoke")
    @Tag("regression")
    @DisplayName("Should create new book successfully")
    void createBook() {
        Book newBook = new BookTestDataBuilder().build();

        Response response = bookService.create(newBook);
        Book createdBook = response.as(Book.class);

        AssertionHelper.assertBookMatchesExpected(response, newBook, createdBook);
    }

    @Test
    @Tag("regression")
    @DisplayName("Should update existing book successfully")
    void updateBook() {
        Book updatedBook = new BookTestDataBuilder()
                .withId(1)
                .build();

        Response response = bookService.update(updatedBook.getId(), updatedBook);
        Book returnedBook = response.as(Book.class);

        assertAll("Update existing book",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains(CONTENT_TYPE_JSON), "Content type should be application/json"),
                () -> assertEquals(updatedBook.getId(), returnedBook.getId(), "Book ID should match"),
                () -> assertEquals(updatedBook.getTitle(), returnedBook.getTitle(), "Title should match"),
                () -> assertEquals(updatedBook.getDescription(), returnedBook.getDescription(), "Description should match"),
                () -> assertEquals(updatedBook.getPageCount(), returnedBook.getPageCount(), "Page count should match"),
                () -> assertEquals(updatedBook.getExcerpt(), returnedBook.getExcerpt(), "Excerpt should match"),
                () -> assertEquals(
                        Instant.parse(updatedBook.getPublishDate()),
                        Instant.parse(returnedBook.getPublishDate()),
                        "Publish date should match"
                )
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("Should delete book successfully")
    void deleteBook() {
        Integer bookId = 1;

        Response response = bookService.delete(bookId);

        assertAll("Delete book",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getBody().asString().isEmpty(), "Response body should be empty")
        );
    }

    @ParameterizedTest
    @Tag("regression")
    @ValueSource(ints = {0, -1, Integer.MAX_VALUE, 999999})
    @DisplayName("Should return 404 for invalid book IDs")
    void getBookByInvalidId(int invalidId) {
        Response response = bookService.getById(invalidId);

        AssertionHelper.assertNotFoundResponse(response);
    }

    @Test
    @Tag("regression")
    @DisplayName("Should create book with minimal required fields")
    void createBookWithMinimalFields() {
        Book minimalBook = new BookTestDataBuilder()
                .withId(1)
                .withTitle(null)
                .withDescription(null)
                .withPageCount(10)
                .withExcerpt(null)
                .build();

        Response response = bookService.create(minimalBook);
        Book createdBook = response.as(Book.class);

        AssertionHelper.assertBookMatchesExpected(response, minimalBook, createdBook);
    }

    @ParameterizedTest
    @Tag("regression")
    @MethodSource("nullRequiredFields")
    @DisplayName("Should return validation error when a required field is null")
    void shouldRejectBookWhenRequiredFieldIsNull(
            Integer id,
            Integer pageCount,
            String publishDate
    ) {
        Book book = new BookTestDataBuilder()
                .withId(id)
                .withPageCount(pageCount)
                .withPublishDate(publishDate)
                .build();

        Response response = bookService.create(book);
        ErrorResponse error = response.as(ErrorResponse.class);

        String expectedErrorField =
                id == null ? "$.id" :
                        pageCount == null ? "$.pageCount" :
                                "$.publishDate";

        assertAll("400 Error: Bad Request",
                () -> assertEquals(400, response.getStatusCode(), "Status code should be 400"),
                () -> assertTrue(
                        response.getContentType().contains("application/problem+json"),
                        "Content type should be application/problem+json"
                ),
                () -> assertEquals(400, error.getStatus(), "Error response status should be 400"),
                () -> assertEquals(
                        "One or more validation errors occurred.",
                        error.getTitle(),
                        "Error response title should be 'One or more validation errors occurred.'"
                ),
                () -> assertNotNull(error.getType(), "Error response type should not be null"),
                () -> assertNotNull(error.getTraceId(), "Error response traceId should not be null"),
                () -> assertFalse(error.getTraceId().isEmpty(), "Error response traceId should not be empty"),
                () -> assertNotNull(error.getErrors(), "Error response errors should not be null"),
                () -> assertTrue(
                        error.getErrors().containsKey(expectedErrorField),
                        "Expected validation error for field: " + expectedErrorField
                )
        );
    }

    static Stream<Arguments> nullRequiredFields() {
        return Stream.of(
                Arguments.of(null, 100, Instant.now().toString()),   // id null
                Arguments.of(1, null, Instant.now().toString()),     // pageCount null
                Arguments.of(1, 100, null)                           // publishDate null
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("Get all books with random query parameters")
    void getAllBooksWithRandomQueryParams() {
        Map<String, Object> randomQueryParams = Map.of(
                "authorId", ThreadLocalRandom.current().nextInt(1, 1000),
                "category", UUID.randomUUID().toString(),
                "year", ThreadLocalRandom.current().nextInt(1900, 2030)
        );

        Response response = bookService.getAll(randomQueryParams);
        List<Book> books = List.of(response.as(Book[].class));

        assertAll("Get all books with random query parameters",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains(CONTENT_TYPE_JSON), "Content type should be application/json"),
                () -> assertTrue(response.getTime() < 5000, "Response time should be under 5000ms, was: " + response.getTime() + "ms"),
                () -> assertNotNull(books, "Books list should not be null"),
                () -> assertFalse(books.isEmpty(), "Books list should not be empty"),
                () -> books.forEach(AssertionHelper::assertBookHasRequiredFields)
        );
    }

    @ParameterizedTest
    @Tag("regression")
    @MethodSource("randomEmptyFields")
    @DisplayName("Should create book with one optional field empty at a time")
    void createBookWithOneOptionalFieldEmpty(String title, String description, String excerpt) {
        Book book = new BookTestDataBuilder()
                .withTitle(title)
                .withDescription(description)
                .withExcerpt(excerpt)
                .build();

        Response response = bookService.create(book);
        Book createdBook = response.as(Book.class);

        AssertionHelper.assertBookMatchesExpected(response, book, createdBook);
    }

    static Stream<Arguments> randomEmptyFields() {
        Faker faker = new Faker();

        return Stream.of(
                Arguments.of("", faker.lorem().paragraph(), faker.lorem().sentence()),          // title empty
                Arguments.of(faker.book().title(), "", faker.lorem().sentence()),               // description empty
                Arguments.of(faker.book().title(), faker.lorem().paragraph(), "")               // excerpt empty
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("Should handle special and Unicode characters in title and description")
    void createBookWithSpecialAndUnicodeCharacters() {
        String specialUnicode = "Test!@#$%^&*()_+-=[]{}|;':\",./<>?  测试书籍 📚 Тест كتاب 🌟";

        Book book = new BookTestDataBuilder()
                .withTitle(specialUnicode)
                .withDescription(specialUnicode)
                .withPageCount(100)
                .build();

        Response response = bookService.create(book);
        Book createdBook = response.as(Book.class);

        AssertionHelper.assertBookMatchesExpected(response, book, createdBook);
    }

    @Test
    @Tag("regression")
    @DisplayName("Should return 201 for create (This test is intentionally failing to demonstrate failed test reporting)")
    void createBookExpectsWrongStatusCode() {
        Book book = new BookTestDataBuilder().build();

        Response response = bookService.create(book);

        assertAll("Create book expects wrong status code",
                () -> assertEquals(201, response.getStatusCode(), "Status code should be 201")
        );
    }

    @Test
    @Tag("regression")
    @Disabled("Pagination not yet implemented")
    @DisplayName("Should support pagination for books. Intentionally skipped tests")
    void getBooksWithPagination() {
        fail("This test should not run");
    }
}
