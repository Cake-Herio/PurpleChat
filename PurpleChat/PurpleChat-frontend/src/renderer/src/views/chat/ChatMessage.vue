<template>
    <div class="message-content-my" v-if="data.sendUserId === userInfoStore.getInfo().userId">
        <!-- 包含感叹号的内容面板 -->
        <div
            :class="['content-panel', { 'show-warning': data.messageType == 2 && data.status == -1, 'show-waring-media': data.messageType == 5 && data.status == -1 }, data.messageType == 5 ? 'content-panel-media' : '']">
            <!-- 对于媒体消息，额外增加加载中的组件 -->
            <div class="sending" v-if="data.status == -3 && data.messageType == 5 && data.fileType != 2">
                <el-skeleton :animated="true">
                    <template #template>
                        <el-skeleton-item class="skeleton-item" variant="image"></el-skeleton-item>
                    </template>
                </el-skeleton>
            </div>



            <!-- 非媒体消息 -->
            <img class="message-loading"  v-if="data.messageType != 5 && data.status == 0" src="../../assets/img/message-loading.gif" />

            <div v-if="data.messageType != 5" class="content" v-html="data.messageContent">

            </div>



            <!-- 媒体消息 -->
            <div class="content" v-if="data.messageType == 5">
                <!-- 网络异常 -->
                <template v-if="data.status == -1">
                    <img v-if="data.fileType == 0" src="../../assets/img/404.png">
                    <img v-if="data.fileType == 1" src="../../assets/img/404-video.png">
                </template>


                <template v-if="data.fileType == 0">

                    <ChatMessageImage :data="data" @click="showDetail"></ChatMessageImage>
                </template>
                <template v-if="data.fileType == 1">
                    <ChatMessageVideo :data="data" @click="showDetail"></ChatMessageVideo>
                </template>

            </div>

            <!-- 单独处理文件情况 -->
            <template v-if="data.messageType == 5 && data.fileType == 2">
                <div class="content">
                    <ChatMessageFile :data="data"></ChatMessageFile>
                </div>
            </template>
        </div>
        <!-- 展示头像 -->
        <div class="user-avatar">
            <AvatarBase :width="35" :userId="data.userId" :forceGet="false"></AvatarBase>
        </div>
    </div>

    <div class="message-content-other" v-else>
        <div class="user-avatar" v-if="data.contactType == 1">
            <Avatar :width="35" :userId="data.sendUserId"></Avatar>
        </div>

        <div class="user-avatar" v-if="data.contactType == 0">
            <AvatarBase :width="35" :userId="data.sendUserId"></AvatarBase>
        </div>

        <div
            :class="['content-panel', data.contactType == 1 ? 'group-content' : '', data.messageType == 5 ? 'content-panel-media' : '']">
            <!-- 如果是群聊，展示对方的昵称 -->
            <div class="nick-name" v-if="data.contactType == 1">{{ data.sendUserNickName }}</div>



            <div class="sending" v-if="data.status == -3 && data.fileType != 2">
                <el-skeleton :animated="true">
                    <template #template>
                        <el-skeleton-item class="skeleton-item" variant="image"></el-skeleton-item>
                    </template>
                </el-skeleton>
            </div>

            <!-- 文字信息 -->
            <div class="content" v-html="data.messageContent" v-if="data.messageType != 5"></div>

            <template v-if="data.status != -3 && data.fileType != 2">
                <div class="content" v-if="data.messageType == 5">
                    <template v-if="data.fileType == 0">
                        <ChatMessageImage :data="data" @click="showDetail"></ChatMessageImage>
                    </template>
                    <template v-if="data.fileType == 1">
                        <ChatMessageVideo :data="data" @click="showDetail"></ChatMessageVideo>
                    </template>
                </div>
            </template>


            <!-- 单独处理文件情况 -->
            <template v-if="data.messageType == 5 && data.fileType == 2">
                <div class="content">
                    <ChatMessageFile :data="data"></ChatMessageFile>
                </div>
            </template>
        </div>
    </div>


</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useUserInfoStore } from "@/stores/UserInfoStore"
import ChatMessageImage from "./ChatMessageImage.vue"
import ChatMessageVideo from "./ChatMessageVideo.vue"
import ChatMessageFile from "./ChatMessageFile.vue"
import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore();
const { proxy } = getCurrentInstance()
const userInfoStore = useUserInfoStore()
const route = useRoute()
const router = useRouter()

const props = defineProps({
    data: {
        type: Object,
        default: {}
    },
    currentChatSession: {
        type: Object,
        default: {}
    }
})

const emit = defineEmits(['showMediaDetail', 'loaded'])

const loadedCallBack = () => {
    emit('loaded', props.data)
}

const showDetail = () => {
    console.log("点击")
    if (props.data.sendUserId === userInfoStore.getInfo().userId) {
        if (props.data.status == 1) {
            emit('showMediaDetail', props.data.messageId)
        }
    } else {
        if (props.data.status == 3) {
            emit('showMediaDetail', props.data.messageId)
        }
        if (props.data.status == -2) {
            props.data.status = 2
            dynamicStore.setData("reloadOnline" + props.data.messageId, new Date().getTime());
        }
    }


}



nextTick(() => {
    loadedCallBack()
})
</script>

<style lang="scss" scoped>
.sending {
    width: 170px;
    height: 170px;
    overflow: hidden;
    float: right;
    margin-right: 5px;
    border-radius: 5px;

    .skeleton-item {
        width: 170px;
        height: 170px;
    }
}

.content {
    display: inline-block;
    padding: 8px;
    color: #474747;
    border-radius: 5px;
    text-align: left;
    font-size: 14px;

    :deep(.emoji) {
        font-size: 20px;
    }
}

.content-panel {
    flex: 1;
    position: relative;
    justify-content: flex-end;

    &.show-warning::before {
        content: "!";
        
        margin-right: 5px;
        padding: 1.0% 2.5%;
        padding-bottom: 4px;
        background-color: rgb(243, 76, 76);
        height: 15px;
        margin-top:5px;
        //背景大小

        border-radius: 50%;
        color: white;
        font-size: 14px;
        font-weight: bold;
        //高度居中  
        //进行相对位移

    }

    &.show-waring-media::before {
        content: "!";
        display: inline-block;
        margin-right: 5px;
        padding: 0% 2.5%;
        background-color: rgb(243, 76, 76);
        border-radius: 50%;
        color: white;
        font-size: 14px;
        font-weight: bold;
        //高度居中  
        //进行相对位移
        position: relative;
        top: -45%;

    }



    &::after {
        content: "";
        position: absolute;
        display: block;
        width: 10px;
        height: 10px;
        background: #dac1fa;
        transform: rotate(45deg);
        border-radius: 2px;
        top: 13px;
    }
}

.content-panel-media {
    .content {
        border-radius: 5px;
        background: none !important;
        overflow: hidden;
        padding: 0px;
    }

    &::after {
        display: none;
    }
}

.message-content-my {
    display: flex;

    .content-panel {
        margin-right: 10px;
        text-align: right;
        padding-left: 32%;

        .content {
            background: #dac1fa;
            margin-left: 7px
        }

        &::after {
            right: -4px;
        }
    }
}

.message-content-other {
    display: flex;
    padding-right: 32%;

    .user-avatar {
        margin-right: 10px;
        width: 35px;
        height: 35px;
    }

    .content-panel {
        flex: 1;
        position: relative;
        text-align: left;

        .nick-name {
            font-size: 12px;
            color: #b2b2b2;
        }

        .content {
            background: #fff;
        }

        .sending {
            float: left;
        }

        &::after {
            left: -4px;
            background: #fff;
        }
    }

    .content-panel-media {
        justify-content: flex-start;
    }
}

.group-content {
    margin-top: -6px;

    .content {
        margin-top: 6px;
    }

    &::after {
        left: -4px;
        top: 35px;
        background: #fff;
    }
}

.message-loading{
    width: 30px;
}




.not-found {
    height: 100px;
    border-radius: 5px;
}
</style>
