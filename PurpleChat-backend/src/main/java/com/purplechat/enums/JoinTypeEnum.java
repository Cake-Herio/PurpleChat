package com.purplechat.enums;

import com.purplechat.utils.StringTools;

public enum JoinTypeEnum {
    //直接加入 和 需要验证
    DIRECT(0, "直接加入"),
    VERIFY(1, "需要验证");

    private Integer type;
    private String desc;

    JoinTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static JoinTypeEnum getByType(Integer type) {
        for (JoinTypeEnum value : JoinTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

    public static JoinTypeEnum getByType (String type) {
        try {
            if (StringTools.isEmpty(type)) {
                return null;
            }
            return JoinTypeEnum.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
