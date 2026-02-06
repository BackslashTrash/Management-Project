package net.backslashtrash.project1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;


public class Employee extends Account{
    private UUID uuid;

    public Employee(String type, String name, String password) {
        super(type, name, password);
    }
}
