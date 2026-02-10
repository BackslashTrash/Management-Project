package net.backslashtrash.project1;

public enum Files {
    TITLESCREEN(0),
    REGISTER(1),
    LOGIN(2),
    EMPLOYEE(3),
    EMPLOYER(4),
    EMPLOYEELIST(5);

    public final int INDEX;
    Files(int index) {
        this.INDEX =index;
    }
}
