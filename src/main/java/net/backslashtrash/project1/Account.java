package net.backslashtrash.project1;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) //This will exclude null values
public class Account {
    private String username;
    private String password;
    private String uuid;

    public Account(){}

    public Account(String uuid,String name, String password){
        this.uuid= uuid;
        this.username=name;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
}
