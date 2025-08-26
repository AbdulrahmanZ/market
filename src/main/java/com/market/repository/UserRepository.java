package com.market.repository;

import com.market.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    /**
     * Search users by phone and/or username with pagination
     *
     * @param phone    Phone number to search for (can be null)
     * @param username Username to search for (can be null)
     * @param pageable Pagination parameters
     * @return Page of users matching the criteria
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:phone IS NULL OR u.phone = :phone) AND " +
            "(:username IS NULL OR u.username LIKE %:username%)")
    Page<User> searchByPhoneAndUsername(@Param("phone") String phone,
                                        @Param("username") String username,
                                        Pageable pageable);

    @Modifying
    @Query("update User u SET u.deleted = true WHERE u.id = :id")
    void softDeleteById(Long id);
}