package net.backslashtrash.project1;

import java.util.ArrayList;

public class Employer extends Account{

    private ArrayList<Employee> employeeList = new ArrayList<>();
    public Employer(String type, String name, String password) {
        super(type, name, password);
    }
}
