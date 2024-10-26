package com.purplechat.entity.constants;

public class Constants {
    public static final String REDIS_KEY_CHECK_CODE = "easy_chat:check_code:";
    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "easy_chat:ws:user:heartbeat:";
    public static final String REDIS_KEY_WS_TOKEN = "easy_chat:ws:token:";
    public static final String REDIS_KEY_WS_TOKEN_USERID = "easy_chat:ws:token_user_id:";
    public static final String REDIS_KEY_CHECK_EMAIL_CODE = "easy_chat:check_email_code:";


    public static final String REDIS_KEY_SYS_SETTING = "easy_chat:sys_setting";




    public static final Integer REDIS_TIME_1MIN = 60;
    public static final Integer REDIS_TIME_1DAY = REDIS_TIME_1MIN * 60 * 24;
    public static final Integer LENGTH_11 = 11;
    public static final Integer LENGTH_20 = 20;
    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 60 * 60 * 24 * 7;
    public static final Integer REDIS_KEY_TOKEN_EXPIRE = REDIS_TIME_1DAY;
    public static final Long MillisSECOND_3DAY = 1000L * 60 * 60 * 24 * 3;



    public static final String FILE_FOLDER_FILE = "/file/";
    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    public static final String APP_UPDATE_FOLDER = "/app/";


    public static final String IMAGE_SUFFIX = ".png";
    public static final String VIDEO_SUFFIX = ".mp4";

    public static final String COVER_IMAGE_SUFFIX = "_cover.png";
    public static final String APP_EXE_SUFFIX = ".exe";
    public static final String APP_NAME = "purplechatSetup.";


    //默认申请信息
    public static final String APPLY_INFO_TEMPLATE = "我是%s";

    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$^&*_.]{8,18}$";


    public static final String ROBOT_UID = "U52013140711";

    //用户联系人列表
    public static final String REDIS_KEY_USER_CONTACT = "easy_chat:ws:user:contact:";
    public static final String[] IMAGE_SUFFIX_LIST = new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", "JPEG"};
    public static final String[] VIDEO_SUFFIX_LIST = new String[]{".mp4", ".avi", ".rmvb", ".rm", ".flv", ".3gp", ".mkv", ".mov", ".wmv", ".asf", ".asx", ".dat", ".mpg", ".mpeg", ".mpe", ".ts", ".vob",".MOV"};
    public static final Long FILE_SIZE_MB = 10 * 1024 * 1024L;
}
