package com.example.ums.repository;

import com.example.ums.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find non-deleted members
    @Query("SELECT u FROM User u where u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") Long id);

//  Validations: Used to ensure new data is unique
    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);
    boolean existsByAadhaarAndDeletedAtIsNull(String aadhaar);
    boolean existsByPanIgnoreCaseAndDeletedAtIsNull(String pan);

//    Ignores the user's own Id
    boolean existsByEmailIgnoreCaseAndDeletedAtIsNullAndIdNot(String email, Long id);
    boolean existsByAadhaarAndDeletedAtIsNullAndIdNot(String aadhaar, Long id);
    boolean existsByPanIgnoreCaseAndDeletedAtIsNullAndIdNot(String pan, Long id);
}
