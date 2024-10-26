package com.purplechat.enums;

public enum UserContactStatusEnum {
    NO_FRIEND(0, "非好友"),
    FRIEND(1, "好友"),
    DELETE(2, "已删除"),
    DELETE_BE(3, "被删除"),

    BLACKLIST(4, "拉黑"),
    BLACKLIST_BE(5, "非第一次被拉黑"),
    BLACKLIST_BE_FIRST(6, "第一次被拉黑");

    private final Integer status;
    private final String desc;

    UserContactStatusEnum(Integer type, String desc){
        this.status = type;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static String getByStatus(Integer status){
        for (UserContactStatusEnum userContactStatusEnum : UserContactStatusEnum.values()) {
            if (userContactStatusEnum.getStatus().equals(status)) {
                return userContactStatusEnum.getDesc();
            }
        }
        return null;
    }

    public static UserContactStatusEnum getEnumByStatus(Integer status){
        for (UserContactStatusEnum userContactStatusEnum : UserContactStatusEnum.values()) {
            if (userContactStatusEnum.getStatus().equals(status)) {
                return userContactStatusEnum;
            }
        }
        return null;
    }





}
