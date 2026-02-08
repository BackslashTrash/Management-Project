package net.backslashtrash.project1;



import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Register {
    private static final ObjectMapper objectMapper = JsonMapper
            .builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();


    JsonFactory factory = new JsonFactory();
    public Register() {

    }

    public static boolean register(String type, String username, String password) throws IOException {
        ArrayList<Account> accountList = new ArrayList<>();
        File file = new File("src/main/resources/net/backslashtrash/objects/",type.toLowerCase() + ".json");

        if (file.length()>0) {
            accountList = objectMapper.readValue(file, new TypeReference<>() {});
        };


        for (Account account : accountList) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(null, "Username already exist!", "Account Creation",JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }

        accountList.add(new Account(username,password));
        objectMapper.writeValue(file, accountList);
        return true;
    }
}
