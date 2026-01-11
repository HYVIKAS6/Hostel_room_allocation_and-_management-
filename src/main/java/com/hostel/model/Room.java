package com.hostel.model;

public class Room {
    private int id;
    private String number;
    private int capacity;
    private int occupied;

    public Room() {}

    public Room(int id, String number, int capacity, int occupied) {
        this.id = id; this.number = number; this.capacity = capacity; this.occupied = occupied;
    }

    public int getId() { return id; }
    public String getNumber() { return number; }
    public int getCapacity() { return capacity; }
    public int getOccupied() { return occupied; }
}
