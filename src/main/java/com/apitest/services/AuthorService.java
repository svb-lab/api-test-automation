package com.apitest.services;

import com.apitest.config.RestAssuredConfig;
import com.apitest.models.Author;
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
public class AuthorService {

    private static final String AUTHORS_ENDPOINT = "/api/v1/Authors";
    private static final String AUTHOR_BY_ID_ENDPOINT = "/api/v1/Authors/{id}";
    private static final String AUTHORS_BY_BOOK_ID_ENDPOINT = "/api/v1/Authors/authors/books/{idBook}";

    private final RestAssuredConfig restAssuredConfig;
    private final OpenApiValidationFilter validationFilter;

    public AuthorService(RestAssuredConfig restAssuredConfig) {
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

    @Step("Get all authors (no query params)")
    public Response getAllAuthors() {
        return getAllAuthors(Collections.emptyMap());
    }

    @Step("Get all authors with optional query params")
    public Response getAllAuthors(Map<String, ?> queryParams) {
        RequestSpecification request = given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter);

        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }

        return request
                .when()
                .get(AUTHORS_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Step("Get author by ID: {id}")
    public Response getById(Integer id) {
        return given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter)
                .pathParam("id", id)
                .when()
                .get(AUTHOR_BY_ID_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Step("Get authors by book ID: {idBook}")
    public Response getByBookId(Integer idBook) {
        return given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter)
                .pathParam("idBook", idBook)
                .when()
                .get(AUTHORS_BY_BOOK_ID_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Step("Create new author: {author.firstName} {author.lastName}")
    public Response create(Author author) {
        return given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter)
                .body(author)
                .when()
                .post(AUTHORS_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Step("Update author with ID: {id}")
    public Response update(Integer id, Author author) {
        return given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter)
                .pathParam("id", id)
                .body(author)
                .when()
                .put(AUTHOR_BY_ID_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Step("Delete author with ID: {id}")
    public Response delete(Integer id) {
        return given()
                .spec(restAssuredConfig.getRequestSpecification())
                .filter(validationFilter)
                .pathParam("id", id)
                .when()
                .delete(AUTHOR_BY_ID_ENDPOINT)
                .then()
                .log().all()
                .extract()
                .response();
    }
}
