package net.backslashtrash.project1;

import java.util.ArrayList;

public class Employer extends Account{

    private ArrayList<Employee> employeeList = new ArrayList<>();
    public Employer(String uuid,String name, String password) {
        super(uuid,name, password);
    }
}
