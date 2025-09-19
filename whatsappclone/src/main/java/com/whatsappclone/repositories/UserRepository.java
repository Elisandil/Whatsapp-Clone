package com.whatsappclone.repositories;

import com.whatsappclone.entities.user.User;
import com.whatsappclone.entities.user.UserConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    @Query(name = UserConstants.FIND_USER_BY_EMAIL)
    Optional<User> findByEmail(@Param("email") String email);

    @Query(name = UserConstants.FIND_USER_BY_PUBLIC_ID)
    Optional<User> findUserByPublicId(@Param("publicId") String publicId);

    @Query(name = UserConstants.FIND_ALL_USERS_EXCEPT_SELF)
    List<User> findAllUsersExceptSelf(@Param("publicId") String senderId);

    // New
    @Query(name = UserConstants.FIND_ALL_USERS_EXCEPT_SELF_AND_BLOCKED)
    List<User> findAllUsersExceptSelfAndBlocked(@Param("publicId") String publicId);

}
