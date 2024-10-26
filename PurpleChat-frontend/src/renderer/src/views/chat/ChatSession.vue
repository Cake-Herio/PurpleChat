<template>
    <div :class="['chat-session-item', currentChatSession ? 'active' : '', data.topType == 1? 'chat-session-item-chat-top':'']" >
        <div class="contact-tag" v-if="data.contactType == 1"></div>
        <Badge :count="data.noReadCount" :top="7" :left="42"></Badge>
        <Avatar :userId="data.contactId" :width="40" :isChatSession="true"></Avatar>
        <div class="user-info">
            <div class="user-name-panel">

                <div class="user-name">{{ data.contactName }}</div>
                <div class="message-time">{{ proxy.Utils.formatSessionDate(data.lastReceiveTime) }}</div>
            </div>

            <div class="last-message">
                <div v-if="data.contactType == 1">{{ data.sendUserNickname }}</div>
                
                {{ data.lastMessage }}
            </div>
        </div>
        <div class="chat-top iconfont icon-top" v-if="data.topType == 1"></div>




    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
import AvatarBase from "@/components/AvatarBase.vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
import { useMessageCountStore } from "../../stores/MessageCountStore";
const messageCountStore = useMessageCountStore();
const props = defineProps({
    data: {
        type: Object,
        default: {}

    },

    currentChatSession: {
        type: Boolean,
        default: false
    }

})



</script>

<style lang="scss" scoped>
.chat-session-item {
    padding: 10px;
    position: relative;
    display: flex;
    border-bottom: 1px solid #ddd;
    background: #f2f2f1;

    .contact-tag {
        &:hover {
            cursor: pointer;
            background: #d8d8d7;

            .message-time {
                color: #9a9898 !important;
            }
        }


    }

    .user-info {
        flex: 1;
        margin-left: 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        .user-name-panel {
            display: flex;

            .user-name {
                width: 140px;
                color: #000000;
                font-size: 14px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .message-time {
                width: 55px;
                color: #b6b6b6;
                font-size: 12px;
                text-align: right;
            }
        }

        .last-message {
            width: 180px;
            height: 15px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            font-size: 12px;
            color: #999999;
            margin-top: 5px;
        }
    }


    // 置顶
    .chat-top {
        position: absolute;
        right: 0px;
        top: 0px;
        font-size: 12px;
        color: #aa0bf9  !important;
    }

    

    &:hover {
        background: #ede3f5;
        //过渡
        transition: all 0.3s;
        cursor: pointer;
    }
}

.active {
        cursor: pointer;
        background: #ede3f5;
    }

.chat-session-item-chat-top{
    // background:#f4eff8
}

</style>
