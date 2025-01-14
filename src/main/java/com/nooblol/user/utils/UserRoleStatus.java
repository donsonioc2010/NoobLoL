package com.nooblol.user.utils;

/**
 * Value 분류
 *
 * <p>GUEST: 게스트, AUTH_USER: 인증 사용자, UNAUTH_USER: 미인증 사용자, SUSPENSION_USER: 계정정지 사용자, ADMIN: 관리자
 * Role
 */
public enum UserRoleStatus {
    GUEST(0),
    AUTH_USER(1),
    UNAUTH_USER(2),
    SUSPENSION_USER(3),
    ADMIN(9);

    int roleValue;

    UserRoleStatus(int roleValue) {
        this.roleValue = roleValue;
    }

    public int getRoleValue() {
        return roleValue;
    }

    public static boolean isUserRoleAdmin(int roleValue) {
        return ADMIN.getRoleValue() == roleValue;
    }

    public static boolean isNotUserAdmin(int roleValue) {
        return ADMIN.getRoleValue() != roleValue;
    }

    public static boolean isUserRoleAuth(int roleValue) {
        return roleValue == AUTH_USER.getRoleValue();
    }

    public static boolean isUserUnAuthUser(int roleValue) {
        return roleValue == UNAUTH_USER.getRoleValue();
    }

    public static boolean isUserSuspension(int roleValue) {
        return roleValue == SUSPENSION_USER.getRoleValue();
    }
}
