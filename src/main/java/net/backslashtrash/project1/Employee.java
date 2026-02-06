package net.backslashtrash.project1;

import java.util.UUID;

public class Employee extends Account{
    private UUID uuid;

    public Employee(String type, String name, long password) {
        super(type, name, password);
    }
}
