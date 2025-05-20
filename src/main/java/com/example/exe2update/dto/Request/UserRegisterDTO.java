package com.example.exe2update.dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDTO {
    private String fullName;
    private String email;
    private String phone;
    private String password;

    // Getters & setters
}
