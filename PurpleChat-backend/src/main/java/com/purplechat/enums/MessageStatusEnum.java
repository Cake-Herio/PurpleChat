package com.purplechat.enums;

public enum MessageStatusEnum {
    SEND_DOWNLOADED(3, "下载成功"),  //3是下载成功和上传成功
    SEND_DOWNLOADING(2, "下载中"), //2是下载中和上传成功
    SENT(1, "上传成功"),    //如果是文字
    SENDING(0, "上传中"),

    SEND_FAIL(-1, "发送失败");


    Integer type;
    String desc;

    MessageStatusEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public MessageTypeEnum getByType(Integer type) {
        for (MessageTypeEnum value : MessageTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }



    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
