package com.purplechat.enums;

public enum AppUpdateStatusEnum {
    /**
     * 未发布
     */
    INIT(0, "未发布"),
    /**
     * 已发布
     */
    GRAYSCALE(1, "灰度发布"),
    /**
     * 已下架
     */
    ALL(2, "全网发布");

    private Integer code;
    private String desc;

    AppUpdateStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AppUpdateStatusEnum getByCode(Integer code) {
        for (AppUpdateStatusEnum value : AppUpdateStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
