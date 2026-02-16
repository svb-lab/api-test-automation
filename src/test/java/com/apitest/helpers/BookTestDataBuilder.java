package com.apitest.helpers;

import com.apitest.models.Book;
import com.github.javafaker.Faker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class BookTestDataBuilder {
    private Integer id;
    private String title;
    private String description;
    private Integer pageCount;
    private String excerpt;
    private String publishDate;

    private static final Faker faker = new Faker();

    public BookTestDataBuilder() {
        // Default values
        this.id = faker.number().numberBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
        this.title = faker.book().title();
        this.description = faker.lorem().paragraph();
        this.pageCount = faker.number().numberBetween(50, 1000);
        this.excerpt = faker.lorem().sentence();
        this.publishDate = Instant.now().truncatedTo(ChronoUnit.MILLIS).toString();
    }

    public BookTestDataBuilder withId(Integer id) {
        this.id = id;
        return this;
    }

    public BookTestDataBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public BookTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public BookTestDataBuilder withPageCount(Integer pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    public BookTestDataBuilder withExcerpt(String excerpt) {
        this.excerpt = excerpt;
        return this;
    }

    public BookTestDataBuilder withPublishDate(String publishDate) {
        this.publishDate = publishDate;
        return this;
    }

    public Book build() {
        return Book.builder()
                .id(id)
                .title(title)
                .description(description)
                .pageCount(pageCount)
                .excerpt(excerpt)
                .publishDate(publishDate)
                .build();
    }
}