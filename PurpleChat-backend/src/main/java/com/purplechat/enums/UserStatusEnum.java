package com.purplechat.enums;

public enum UserStatusEnum {

    ENABLE(1, "启用"),
    DISABLE(0, "禁止");

    private final Integer type;
    private final String desc;

    UserStatusEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
