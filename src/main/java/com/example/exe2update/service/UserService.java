package com.example.exe2update.service;

import com.example.exe2update.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);

    User findById(Integer id);

    Optional<User> findByEmail(String email);

    void save(User user);

    boolean checkPassword(String rawPassword, String hashedPassword);

    String encodePassword(String rawPassword); // để dùng khi reset password

    void updatePasswordByEmail(String email, String newPassword);

    List<User> getAllUsers();

    void deleteUserById(Integer id);

    void saveUser(String fullName, String email, String phone, String passwordHash);
}
