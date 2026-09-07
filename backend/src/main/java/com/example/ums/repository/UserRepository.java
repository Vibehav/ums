package com.example.ums.repository;

import com.example.ums.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find non-deleted members
    @Query("SELECT u FROM User u where u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") Long id);

    // We fetch the actual Optional<User> so we can check its deletedAt status in Java
    Optional<UserStatusView> findByEmailIgnoreCase(String email);
    Optional<UserStatusView> findByAadhaar(String aadhaar);
    Optional<UserStatusView> findByPanIgnoreCase(String pan);

    //  ADMIN QUERIES (Show deleted users)
    List<User> findAllByDeletedAtIsNotNull();
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    //    Pagination
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findAllActive(Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.deletedAt IS NULL
              AND (
                   LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR u.primaryMobile LIKE CONCAT('%', :search, '%')
              )
            """)
    Page<User> searchActive(@Param("search") String search, Pageable pageable);

}
////  Validations: Used to ensure new data is unique
//    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);
//    boolean existsByAadhaarAndDeletedAtIsNull(String aadhaar);
//    boolean existsByPanIgnoreCaseAndDeletedAtIsNull(String pan);
//
////   Ignores the user's own Id
//    boolean existsByEmailIgnoreCaseAndDeletedAtIsNullAndIdNot(String email, Long id);
//    boolean existsByAadhaarAndDeletedAtIsNullAndIdNot(String aadhaar, Long id);
//    boolean existsByPanIgnoreCaseAndDeletedAtIsNullAndIdNot(String pan, Long id);
