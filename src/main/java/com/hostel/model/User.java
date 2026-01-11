package com.hostel.model;

public abstract class User {
    protected int id;
    protected String name;
    protected String email;

    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
