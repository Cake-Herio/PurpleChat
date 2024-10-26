package com.purplechat.enums;

public enum BeautyAccountEnum {
    NO_USE(0, "未使用"),
    USED(1, "使用");

    private final Integer status;
    private final String desc;

    BeautyAccountEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
