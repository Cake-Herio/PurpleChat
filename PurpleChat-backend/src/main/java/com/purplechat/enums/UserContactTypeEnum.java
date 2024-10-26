package com.purplechat.enums;

import com.purplechat.utils.StringTools;

public enum UserContactTypeEnum {

    USER(0, "U", "好友"),
    GROUP(1, "G", "群聊");


    private final Integer type;
    private final String prefix;
    private final String desc;//描述

    UserContactTypeEnum(Integer type, String prefix, String desc) {
        this.type = type;
        this.prefix = prefix;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDesc() {
        return desc;
    }

    public static UserContactTypeEnum getByName (String name){
        try {
            if(StringTools.isEmpty(name)){
                return null;
            }
            return UserContactTypeEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

    }

    public static UserContactTypeEnum getByPrefix(String prefix){
        try {
            if(StringTools.isEmpty(prefix)){
                return null;
            }

            prefix = prefix.substring(0, 1);
            for(UserContactTypeEnum typeEnum : UserContactTypeEnum.values()){
                if(typeEnum.getPrefix().equals(prefix)){
                    return typeEnum;
                }
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }
}
