package com.example.exe2update.service;

import com.example.exe2update.entity.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role getRoleById(Integer id);
}
