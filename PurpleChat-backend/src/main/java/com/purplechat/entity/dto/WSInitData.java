package com.purplechat.entity.dto;

import com.purplechat.entity.po.ChatMessage;
import com.purplechat.entity.po.ChatSessionUser;

import java.util.List;

public class WSInitData {
    private List<ChatSessionUser> chatSessionUsers;
    private List<ChatMessage> chatMessageList;
    private Integer applyCount; //申请总数

    public List<ChatSessionUser> getChatSessionUsers() {
        return chatSessionUsers;
    }

    public void setChatSessionUsers(List<ChatSessionUser> chatSessionUsers) {
        this.chatSessionUsers = chatSessionUsers;
    }

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }
}
