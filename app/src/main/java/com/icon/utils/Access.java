package com.icon.utils;

/**
 * Created by Ivan on 19.10.2015.
 */
public class Access {
    public static final int USER_TYPE = 1;
    public static final int ADMIN_TYPE = 2;
    public static final int WRONG_TYPE = -1;
    public static final int EMPTY_TYPE = -2;
    public static final int NONE_TYPE = -3;

    public static final String DEF_USER_PASS = "1111";
    public static final String DEF_ADMIN_PASS = "2222";

    public static String UserPass = DEF_USER_PASS;
    public static String AdminPass = DEF_ADMIN_PASS;
    private static int accessType = NONE_TYPE;

    public static int checkAccess(String pass) {
        if (pass == null || pass.isEmpty())
            return EMPTY_TYPE;
        if (pass.equals(UserPass))
            return USER_TYPE;
        if (pass.equals(AdminPass))
            return ADMIN_TYPE;
        return WRONG_TYPE;
    }

    public static void setAccessType(int type) {
        accessType = type;
    }

    public static int getAccessType() {
        return accessType;
    }

    public static boolean isUser() {
        return accessType == USER_TYPE;
    }

    public static boolean isAdmin() {
        return accessType == ADMIN_TYPE;
    }

    public static String getAccessTypeName(int type) {
        if (type == USER_TYPE) return "Пользователь";
        else if (type == ADMIN_TYPE) return "Администратор";
        return "Неизвестный тип";
    }
}
