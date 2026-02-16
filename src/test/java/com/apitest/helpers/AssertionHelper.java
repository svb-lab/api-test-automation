package com.apitest.helpers;

import com.apitest.models.Author;
import com.apitest.models.Book;
import com.apitest.models.ErrorResponse;
import io.restassured.response.Response;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Custom reusable assertions
 */
public class AssertionHelper {

    private AssertionHelper() {
    }

    public static void assertBookHasRequiredFields(Book book) {
        assertNotNull(book.getId(), "Book ID");
        assertNotNull(book.getPageCount(), "Book page count");
        assertNotNull(book.getPublishDate(), "Book publish date");
    }

    public static void assertAuthorHasRequiredFields(Author author) {
        assertNotNull(author.getId(), "Author ID");
        assertNotNull(author.getIdBook(), "Author book ID");
    }

    public static void assertAuthorMatchesExpected(Response response, Author expected, Author actual) {
        assertAll("Create new author",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains("application/json"), "Content type should be application/json"),
                () -> assertEquals(expected.getId(), actual.getId(), "ID should match"),
                () -> assertEquals(expected.getIdBook(), actual.getIdBook(), "Book ID should match"),
                () -> assertEquals(expected.getFirstName(), actual.getFirstName(), "First name should match"),
                () -> assertEquals(expected.getLastName(), actual.getLastName(), "Last name should match")
        );
    }

    public static void assertBookMatchesExpected(Response response, Book expected, Book actual) {
        assertAll("Create new book",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains("application/json"), "Content type should be application/json"),
                () -> assertEquals(expected.getId(), actual.getId(), "ID should match"),
                () -> assertEquals(expected.getTitle(), actual.getTitle(), "Title should match"),
                () -> assertEquals(expected.getDescription(), actual.getDescription(), "Description should match"),
                () -> assertEquals(expected.getPageCount(), actual.getPageCount(), "Page count should match"),
                () -> assertEquals(expected.getExcerpt(), actual.getExcerpt(), "Excerpt should match"),
                () -> assertEquals(Instant.parse(expected.getPublishDate()), Instant.parse(actual.getPublishDate()),
                        "Publish date should match")
        );
    }

    public static void assertNotFoundResponse(Response response) {
        ErrorResponse error = response.as(ErrorResponse.class);

        assertAll("404 Not Found response",
                () -> assertEquals(404, response.getStatusCode(), "Status code should be 404"),
                () -> assertTrue(response.getContentType().contains("application/problem+json"),
                        "Content type should be application/problem+json"),
                () -> assertEquals(404, error.getStatus(), "Error response status should be 404"),
                () -> assertEquals("Not Found", error.getTitle(), "Error response title should be 'Not Found'"),
                () -> assertNotNull(error.getType(), "Error response type should not be null"),
                () -> assertNotNull(error.getTraceId(), "Error response traceId should not be null"),
                () -> assertFalse(error.getTraceId().isEmpty(), "Error response traceId should not be empty")
        );
    }
}
