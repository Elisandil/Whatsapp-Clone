package com.whatsappclone.entities.user;

public class UserConstants {
    public static final String FIND_USER_BY_EMAIL = "Users.findUserByEmail";
    public static final String FIND_ALL_USERS_EXCEPT_SELF = "Users.findAllUsersExceptSelf";
    public static final String FIND_USER_BY_PUBLIC_ID = "Users.findUserByPublicId";

    // New
    public static final String FIND_ALL_USERS_EXCEPT_SELF_AND_BLOCKED = "Users.findAllUsersExceptSelfAndBlocked";

    private UserConstants() {}
}
