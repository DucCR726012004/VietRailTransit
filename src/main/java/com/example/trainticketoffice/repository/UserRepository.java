package com.example.trainticketoffice.repository;

import com.example.trainticketoffice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Integer> {
    User findByEmailAndPassword(String email, String password);
    User findByFullName(String fullName);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.fullName LIKE %:keyword% OR u.phone LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    List<User> findByRole(com.example.trainticketoffice.model.User.Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (u.email LIKE %:keyword% OR u.fullName LIKE %:keyword% OR u.phone LIKE %:keyword%)")
    List<User> searchByKeywordAndRole(@Param("keyword") String keyword, @Param("role") com.example.trainticketoffice.model.User.Role role);
}
