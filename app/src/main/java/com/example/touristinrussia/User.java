package com.example.touristinrussia;

import java.util.List;

public class User {
    private String name;
    private String email;
    private String imageUri;
    private boolean admin;
    private List<String> favorites;

    public User() {
        // Необходим для чтения из базы данных
    }


    public User(String imageUri, String name, String email, boolean admin, List<String> favorites) {
        this.imageUri = imageUri;
        this.name = name;
        this.email = email;
        this.admin = admin;
        this.favorites = favorites;
    }


    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getName() {
        return name;
    }

    public String getImageUri() {
        return imageUri;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }
}

