package com.example.touristinrussia;

public class Place {
    private String id;
    private String name;
    private String city;
    private String description;
    private String imageUri;

    public Place(){}

    public Place(String id, String name, String city, String description, String imageUri) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.description = description;
        this.imageUri = imageUri;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getCity() {
        return city;
    }
}
