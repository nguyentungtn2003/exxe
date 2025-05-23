package com.example.exe2update.controller;

import com.example.exe2update.entity.Role;
import com.example.exe2update.entity.User;
import com.example.exe2update.service.RoleService;
import com.example.exe2update.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dballuser") // lưu ý dấu /
public class DBUserController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    // Hiển thị danh sách user + roles
    @GetMapping()
    public String showAllUsers(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<User> users = userService.getAllUsers();
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("users", users);
        model.addAttribute("username", username);
        model.addAttribute("roles", roles);
        return "alluser"; // tên view .html
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user,
            @RequestParam Integer roleId,
            @RequestParam(required = false) String passwordHash) {

        Role role = roleService.getRoleById(roleId);
        user.setRole(role);

        // Gán username theo role
        if ("admin".equalsIgnoreCase(role.getRoleName())) {
            user.setUsername("admin");
        } else if ("user".equalsIgnoreCase(role.getRoleName())) {
            user.setUsername("user");
        } else {
            user.setUsername(role.getRoleName().toLowerCase());
        }

        if (user.getUserId() != null) {
            User existingUser = userService.findById(user.getUserId());
            user.setCreatedAt(existingUser.getCreatedAt());

            if (passwordHash == null || passwordHash.isBlank()) {
                // Giữ mật khẩu cũ
                user.setPasswordHash(existingUser.getPasswordHash());
            } else {
                // Hash mật khẩu mới
                user.setPasswordHash(passwordEncoder.encode(passwordHash));
            }
        } else {
            user.setCreatedAt(LocalDateTime.now());
            user.setPasswordHash(passwordEncoder.encode(passwordHash)); // hash mật khẩu mới
        }

        userService.save(user);
        return "redirect:/dballuser";
    }

    // Xóa user theo userId
    @PostMapping("/delete")
    public String deleteUser(@RequestParam Integer userId) {
        userService.deleteUserById(userId);
        return "redirect:/dballuser";
    }
}
