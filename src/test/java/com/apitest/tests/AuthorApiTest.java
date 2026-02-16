package com.apitest.tests;

import com.apitest.base.BaseTest;
import com.apitest.helpers.AssertionHelper;
import com.apitest.helpers.AuthorTestDataBuilder;
import com.apitest.models.Author;
import com.apitest.models.ErrorResponse;
import com.apitest.services.AuthorService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authors API Tests")
class AuthorApiTest extends BaseTest {

    @Autowired
    private AuthorService authorService;

    static Stream<Arguments> nullRequiredFields() {
        return Stream.of(
                Arguments.of(null, 1),    // id null
                Arguments.of(1, null)     // idBook null
        );
    }

    static Stream<Arguments> randomEmptyFields() {
        Faker faker = new Faker();

        return Stream.of(
                Arguments.of("", faker.name().lastName()),    // firstName empty
                Arguments.of(faker.name().firstName(), "")    // lastName empty
        );
    }

    @Test
    @Tag("smoke")
    @Tag("regression")
    @DisplayName("Should return all authors successfully")
    void getAllAuthors() {
        Response response = authorService.getAllAuthors();
        List<Author> authors = List.of(response.as(Author[].class));

        assertAll("Get all authors",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains(CONTENT_TYPE_JSON), "Content type should be application/json"),
                () -> assertTrue(response.getTime() < 5000, "Response time should be under 5000ms, was: " + response.getTime() + "ms"),
                () -> assertNotNull(authors, "Authors list should not be null"),
                () -> assertFalse(authors.isEmpty(), "Authors list should not be empty"),
                () -> authors.forEach(AssertionHelper::assertAuthorHasRequiredFields)
        );
    }

    @Test
    @Tag("smoke")
    @Tag("regression")
    @DisplayName("Should return author by valid ID")
    void getAuthorById() {
        Integer authorId = 1;

        Response response = authorService.getById(authorId);
        Author author = response.as(Author.class);

        assertAll("Get author by ID",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains(CONTENT_TYPE_JSON), "Content type should be application/json"),
                () -> assertEquals(authorId, author.getId(), "Author ID should match requested ID"),
                () -> assertNotNull(author.getIdBook(), "Author book ID should not be null")
        );
    }

    @Test
    @Tag("smoke")
    @Tag("regression")
    @DisplayName("Should return authors by book ID")
    void getAuthorsByBookId() {
        Integer bookId = 1;

        Response response = authorService.getByBookId(bookId);
        List<Author> authors = List.of(response.as(Author[].class));

        assertAll("Get authors by book ID",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains(CONTENT_TYPE_JSON), "Content type should be application/json"),
                () -> assertNotNull(authors, "Authors list should not be null"),
                () -> assertFalse(authors.isEmpty(), "Authors list should not be empty"),
                () -> authors.forEach(author -> assertEquals(bookId, author.getIdBook(),
                        "Author should be associated with book ID " + bookId)),
                () -> authors.forEach(AssertionHelper::assertAuthorHasRequiredFields)
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("Should create new author successfully")
    void createAuthor() {
        Author newAuthor = new AuthorTestDataBuilder().build();

        Response response = authorService.create(newAuthor);
        Author createdAuthor = response.as(Author.class);

        AssertionHelper.assertAuthorMatchesExpected(response, newAuthor, createdAuthor);
    }

    @Test
    @Tag("regression")
    @DisplayName("Should update existing author successfully")
    void updateAuthor() {
        Author updatedAuthor = new AuthorTestDataBuilder()
                .withId(1)
                .build();

        Response response = authorService.update(updatedAuthor.getId(), updatedAuthor);
        Author returnedAuthor = response.as(Author.class);

        AssertionHelper.assertAuthorMatchesExpected(response, updatedAuthor, returnedAuthor);
    }

    @Test
    @Tag("regression")
    @DisplayName("Should delete author successfully")
    void deleteAuthor() {
        Integer authorId = 1;

        Response response = authorService.delete(authorId);

        assertAll("Delete author",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getBody().asString().isEmpty(), "Response body should be empty")
        );
    }

    @ParameterizedTest
    @Tag("regression")
    @ValueSource(ints = {0, -1, Integer.MAX_VALUE, 999999})
    @DisplayName("Should return 404 for invalid author IDs")
    void getAuthorByInvalidId(int invalidId) {
        Response response = authorService.getById(invalidId);

        AssertionHelper.assertNotFoundResponse(response);
    }

    @Test
    @Tag("regression")
    @DisplayName("Should create author with minimal required fields")
    void createAuthorWithMinimalFields() {
        Author minimalAuthor = new AuthorTestDataBuilder()
                .withId(1)
                .withFirstName(null)
                .withLastName(null)
                .build();

        Response response = authorService.create(minimalAuthor);
        Author createdAuthor = response.as(Author.class);

        AssertionHelper.assertAuthorMatchesExpected(response, minimalAuthor, createdAuthor);
    }

    @ParameterizedTest
    @Tag("regression")
    @MethodSource("nullRequiredFields")
    @DisplayName("Should return validation error when a required field is null")
    void shouldRejectAuthorWhenRequiredFieldIsNull(Integer id, Integer idBook) {
        Author author = new AuthorTestDataBuilder()
                .withId(id)
                .withIdBook(idBook)
                .build();

        Response response = authorService.create(author);
        ErrorResponse error = response.as(ErrorResponse.class);

        String expectedErrorField = id == null ? "$.id" : "$.idBook";

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

    @Test
    @Tag("regression")
    @DisplayName("Get all authors with random query parameters")
    void getAllAuthorsWithRandomQueryParams() {
        Map<String, Object> randomQueryParams = Map.of(
                "bookId", ThreadLocalRandom.current().nextInt(1, 1000),
                "category", UUID.randomUUID().toString(),
                "year", ThreadLocalRandom.current().nextInt(1900, 2030)
        );

        Response response = authorService.getAllAuthors(randomQueryParams);
        List<Author> authors = List.of(response.as(Author[].class));

        assertAll("Get all authors with random query parameters",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains(CONTENT_TYPE_JSON), "Content type should be application/json"),
                () -> assertTrue(response.getTime() < 5000, "Response time should be under 5000ms, was: " + response.getTime() + "ms"),
                () -> assertNotNull(authors, "Authors list should not be null"),
                () -> assertFalse(authors.isEmpty(), "Authors list should not be empty"),
                () -> authors.forEach(AssertionHelper::assertAuthorHasRequiredFields)
        );
    }

    @ParameterizedTest
    @Tag("regression")
    @MethodSource("randomEmptyFields")
    @DisplayName("Should create author with one optional field empty at a time")
    void createAuthorWithOneOptionalFieldEmpty(String firstName, String lastName) {
        Author author = new AuthorTestDataBuilder()
                .withFirstName(firstName)
                .withLastName(lastName)
                .build();

        Response response = authorService.create(author);
        Author createdAuthor = response.as(Author.class);

        AssertionHelper.assertAuthorMatchesExpected(response, author, createdAuthor);
    }

    @Test
    @Tag("regression")
    @DisplayName("Should handle special and Unicode characters in first and last name")
    void createAuthorWithSpecialAndUnicodeCharacters() {
        String specialUnicode = "José!@#$%  Müller 李明 📚 Тест كتاب 🌟";

        Author author = new AuthorTestDataBuilder()
                .withFirstName(specialUnicode)
                .withLastName(specialUnicode)
                .build();

        Response response = authorService.create(author);
        Author createdAuthor = response.as(Author.class);

        AssertionHelper.assertAuthorMatchesExpected(response, author, createdAuthor);
    }

    @Test
    @Tag("regression")
    @DisplayName("Should return empty list for non-existent book ID")
    void getAuthorsByNonExistentBookId() {
        Integer nonExistentBookId = 999999;

        Response response = authorService.getByBookId(nonExistentBookId);
        List<Author> authors = List.of(response.as(Author[].class));

        assertAll("Get authors by non-existent book ID",
                () -> assertEquals(200, response.getStatusCode(), "Status code should be 200"),
                () -> assertTrue(response.getContentType().contains(CONTENT_TYPE_JSON), "Content type should be application/json"),
                () -> assertTrue(authors.isEmpty(), "Should return empty list for non-existent book")
        );
    }

    @Test
    @Tag("regression")
    @DisplayName("Should return 201 for create (This test is intentionally failing to demonstrate failed test reporting)")
    void createAuthorExpectsWrongStatusCode() {
        Author author = new AuthorTestDataBuilder().build();

        Response response = authorService.create(author);

        assertAll("Create author expects wrong status code",
                () -> assertEquals(201, response.getStatusCode(), "Status code should be 201")
        );
    }

    @Test
    @Tag("regression")
    @Disabled("Search functionality not yet implemented")
    @DisplayName("Should support searching authors by name. Intentionally skipped tests")
    void searchAuthorsByName() {
        fail("This test should not run");
    }
}
