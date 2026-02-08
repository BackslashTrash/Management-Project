package net.backslashtrash.project1;

import java.util.UUID;


public class Employee extends Account{
    private UUID uuid;

    public Employee(String name, String password) {
        super(name, password);
    }
}
