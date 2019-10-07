package com.allen.imsystem.common.Const;

public class GlobalConst {
    public static final String ENCODING = "UTF-8";

    /**
     *  用户在线状态
     */
    public static class UserStatus{
        public static final String OFFLINE = "0";   // 离线
        public static final String ONLINE = "1";    // 在线
        public static final String AFK = "2";       // 离开
        public static final String HIDE = "3";      // 隐身
    }

    /**
     * 正则表达式
     */
    public static class RegExp {

        public static final String EMAIL = "^[\\w]+@[a-zA-z\\d]+\\.[a-zA-z0-9]+$";
        public static final String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,12}$";
    }

    public static class MQ {
        public static final String EMAIL_MESSAGE_KEY = "EMAIL_MESSAGE_QUEUE";
    }
}
