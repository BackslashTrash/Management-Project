package net.backslashtrash.project1;

public enum Files {
    TITLESCREEN(0),
    EMPLOYEE(1),
    EMPLOYER(2),
    LOGIN(3),
    REGISTER(4);


    public final int INDEX;

    Files(int fileIndex) {
        INDEX = fileIndex;
    }
}
