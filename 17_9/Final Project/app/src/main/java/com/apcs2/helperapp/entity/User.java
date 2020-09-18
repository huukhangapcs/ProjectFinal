package com.apcs2.helperapp.entity;

public class User {
    String name;
    String email;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String email() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}