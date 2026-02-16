package com.apitest.helpers;

import com.apitest.models.Author;
import com.github.javafaker.Faker;

public class AuthorTestDataBuilder {
    private Integer id;
    private Integer idBook;
    private String firstName;
    private String lastName;

    private static final Faker faker = new Faker();

    public AuthorTestDataBuilder() {
        this.id = faker.number().numberBetween(1, Integer.MAX_VALUE);
        this.idBook = faker.number().numberBetween(1, 100);
        this.firstName = faker.name().firstName();
        this.lastName = faker.name().lastName();
    }

    public AuthorTestDataBuilder withId(Integer id) {
        this.id = id;
        return this;
    }

    public AuthorTestDataBuilder withIdBook(Integer idBook) {
        this.idBook = idBook;
        return this;
    }

    public AuthorTestDataBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public AuthorTestDataBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Author build() {
        return Author.builder()
                .id(id)
                .idBook(idBook)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }
}
