package com.purplechat.enums;

import com.purplechat.utils.StringTools;

public enum UserContactApplyStatusEnum {
    INIT(0, "待处理"),
    PASS(1, "已通过"),
    REJECT(2, "已拒绝"),
    BLACKLIST(3, "已拉黑");

    private Integer status;
    private String desc;

    UserContactApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static UserContactApplyStatusEnum getByStatus(Integer status) {
        for (UserContactApplyStatusEnum value : UserContactApplyStatusEnum.values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

    public static UserContactApplyStatusEnum getByStatus(String status){
        try{
            if (StringTools.isEmpty(status)) {
                return null;
            }
            return UserContactApplyStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e){
            return null;
        }
    }
}
