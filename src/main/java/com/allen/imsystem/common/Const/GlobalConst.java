package com.allen.imsystem.common.Const;

public class GlobalConst {
    public static final String ENCODING = "UTF-8";


    /**
     * 路径类
     */
    public static class Path{
        public static final String API_SERVER_IP = "120.77.42.156";
        public static final String RESOURCES_URL = "http://120.77.42.156:8088/imsystem/static/";
        public static final String AVATAR_URL = "http://120.77.42.156:8088/imsystem/static/avatar/";
        public static final String AVATAR_PATH = "/usr/resources/imsystem/static/avatar/";
        public static final String MSG_RECORD_PATH = "/usr/resources/imsystem/static/msg_record/";
    }
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
        public static final String PASSWORD = "^*$";
    }

    public static class MQ {
        public static final String EMAIL_MESSAGE_KEY = "EMAIL_MESSAGE_QUEUE";
    }

    /**
     * redis相关的常量
     */
    public static class Redis{
        public static final String KEY_USER_STATUS = "user_status";
        public static final String KEY_CHAT_TYPE = "chat_type";
        public static final String KEY_CHAT_REMOVE = "chat_remove";
    }

    public static class ChatType{
        public static final String PRIVATE_CHAT = "0";
        public static final String GROUP_CHAT = "1";
    }
}
