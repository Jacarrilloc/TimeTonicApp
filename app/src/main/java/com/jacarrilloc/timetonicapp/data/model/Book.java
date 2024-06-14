package com.jacarrilloc.timetonicapp.data.model;

public class Book {
    private String name;
    private String imageUrl;

    public Book(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

