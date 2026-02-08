package net.backslashtrash.project1;

import java.util.ArrayList;

public class Employer extends Account{

    private ArrayList<Employee> employeeList = new ArrayList<>();
    public Employer(String name, String password) {
        super(name, password);
    }
}
