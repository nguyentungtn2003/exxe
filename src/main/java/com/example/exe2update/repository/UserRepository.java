package com.example.exe2update.repository;

import com.example.exe2update.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
        SELECT u
          FROM User u
         WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email))
    """)
    Optional<User> findByEmailNormalized(@Param("email") String email);

     @Query("SELECT u FROM User u JOIN u.role r WHERE r.roleName = 'ADMIN'")
    List<User> findAllAdmins();

     long countByRole_RoleName(String roleName);

}
