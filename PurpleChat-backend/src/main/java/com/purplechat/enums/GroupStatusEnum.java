package com.purplechat.enums;

public enum GroupStatusEnum {
    ABNORMAL(0, "解散"),
    NORMAL(1, "正常");

    private Integer status;
    private String desc;

    GroupStatusEnum(Integer status, String desc){
        this.status = status;
        this.desc =desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}




