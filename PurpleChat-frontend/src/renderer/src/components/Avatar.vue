<template>
    <el-popover :width="280" placement="right-start" :show-arrow="false" trigger="click" transition="none"
        :hide-after="0" @show="getUserInfo" ref="powerPopover">
        <template #reference>
            <AvatarBase :userId="userId" :width="width" :borderRadius="borderRadius" :showDetail="false"
                :forceGet="forceGet"></AvatarBase>
        </template>
        <template #default>
            <div class="popover-user-panel">
                <UserBaseInfo :userInfo="userInfo" :showArea="true"></UserBaseInfo>

                <div class="op-btn" v-if="userId != userInfoStore.getInfo().userId&& isChatSession == false">
                    <el-button link size="small" @click="addFriend" v-if="userInfo.status != 1">添加好友</el-button>

                    <el-button link size="small" @click="goToChat" v-else>发消息</el-button>
                </div>
            </div>
        </template>

    </el-popover>

    <!-- 添加好友 -->
    <SearchAdd ref="searchAddRef"></SearchAdd>



</template>

<script setup>
import { robot_UID } from "@/utils/Constants";
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
const { proxy } = getCurrentInstance()
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
const powerPopover = ref()
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()
import SearchAdd from '@/views/contact/SearchAdd.vue'
import { useAvatarInfoStore } from "@/stores/AvatarUpdateStore";
const avatarInfoStore = useAvatarInfoStore();
const props = defineProps({
    userId: {
        type: String,
        default: ''
    },
    width: {
        type: Number,
        default: 40

    },
    borderRadius: {
        type: Number,
        default: 3
    },
    groupId: {
        type: String,
        default: ''
    },
    forceGet: {
        type: Boolean,
        default: true
    },
    isChatSession:{
        type: Boolean,
        default: false
    }
});


const userInfo = ref({});

const getUserInfo = async () => {
    console.log('getUserInfo')
    //强制从服务器获取图片
    window.ipcRenderer.send('forceGetAvatar', {
        console: 'forceGetAvatar',
        fileId: props.userId,
    })

    if (userInfoStore.getInfo().userId == props.userId) {
        userInfo.value = userInfoStore.getInfo()
    }
    let result = await proxy.Request(
        {
            url: proxy.Api.search,
            method: 'post',
            params: {
                contactId: props.userId
            },
            showLoading: false
        }
    )
    if(!result){
        return
    }
    if(!result.data){

        proxy.Message.error("未找到相关信息")
        return
    }
    userInfo.value = Object.assign({}, result.data)
    userInfo.value.userId = result.data.contactId
    if(props.userId[0] === 'G'){
        userInfo.value.groupName = result.data.nickName
    }
}

const emit = defineEmits(['closeDrawer'])




const goToChat = () => {
    // 跳转到聊天页面 研究添加时间戳的作品是为了使得每次点击头像都能刷新聊天页面
    //关掉popover
    powerPopover.value.hide()
    emit('closeDrawer')
    router.push({
        path: '/chat',
        query: {
            contactId: userInfo.value.userId,
            timestamp: new Date().getTime()
        }
    })
}

const searchAddRef = ref(null)
const addFriend = () => {
    //关掉popover
    powerPopover.value.hide()
    searchAddRef.value.show({
        contactId: props.userId,
        contactType: 'USER',
    })
}





</script>

<style lang="scss" scoped>
.op-btn {
    text-align: center;
    border-top: 1px solid #ebeef5;
    padding-top: 10px;

}
</style>
