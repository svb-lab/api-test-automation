package com.apitest.services;

import com.apitest.config.RestAssuredConfig;
import com.apitest.models.Book;
import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Service
public class BookService {

    private static final String BOOKS_ENDPOINT = "/api/v1/Books";
    private static final String BOOK_BY_ID_ENDPOINT = "/api/v1/Books/{id}";

    private final RestAssuredConfig restAssuredConfig;
    private final OpenApiValidationFilter validationFilter;

    public BookService(RestAssuredConfig restAssuredConfig) {
        this.restAssuredConfig = restAssuredConfig;

        /* Validation is intentionally disabled because fakerestapi does not conform to its own swagger.json.
        The API responses violate the published OpenAPI contract, making strict validation unusable.
        OpenApiInteractionValidator is still included to demonstrate how it can be applied for API testing
        when an API correctly honors its OpenAPI specification. */
        LevelResolver levelResolver = LevelResolver.create()
                .withDefaultLevel(ValidationReport.Level.IGNORE)
                .build();
        this.validationFilter = new OpenApiValidationFilter(
                OpenApiInteractionValidator.createFor("/fakerestapi.json")
                        .withLevelResolver(levelResolver)
                        .build()
        );
    }

    @Step("Get all books (no query params)")
    public Response getAll() {
        return getAll(Collections.emptyMap());
    }

    @Step("Get all books with optional query params")
    public Response getAll(Map<String, ?> queryParams) {
        RequestSpecification request = given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter);

        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }

        return request
                .when()
                .get(BOOKS_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Step("Get book by ID: {id}")
    public Response getById(Integer id) {
        return given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter)
                .pathParam("id", id)
                .when()
                .get(BOOK_BY_ID_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Step("Create new book: {book.title}")
    public Response create(Book book) {
        return given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter)
                .body(book)
                .when()
                .post(BOOKS_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Step("Update book with ID: {id}")
    public Response update(Integer id, Book book) {
        return given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter)
                .pathParam("id", id)
                .body(book)
                .when()
                .put(BOOK_BY_ID_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Step("Delete book with ID: {id}")
    public Response delete(Integer id) {
        return given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter)
                .pathParam("id", id)
                .when()
                .delete(BOOK_BY_ID_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }
}
