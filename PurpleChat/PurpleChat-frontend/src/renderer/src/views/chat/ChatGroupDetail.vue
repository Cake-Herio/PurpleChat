<template>
    <div class="group-panel">
        <el-drawer v-model="showDrawer" modal-class="mask-style" :size="300" ref="drawerRef">
            <div class="group-panel-body">
                <div class="member-list">
                    <div class="member-item" v-for="item in memberList" :key="item.userId">
                        <Avatar :userId="item.userId" :width="40" @closeDrawer="closeDrawerHandler"></Avatar>
                        <div class="nick-name"> {{ item.nickName }}</div>
                        <!-- <div class="owner-tag" v-if="item.userId == groupInfo.groupOwnerId">群主</div> -->
                    </div>
                    <template v-if="userInfoStore.getInfo().userId == groupInfo.groupOwnerId">
                        <div class="member-item" @click="addUser">
                            <div class="iconfont icon-add icon-op"></div>
                            <div class="nick-name">添加</div>
                        </div>
                        <div class="member-item" @click="removeUser">
                            <div class="iconfont icon-min icon-op"></div>
                            <div class="nick-name">移除</div>
                        </div>
                    </template>
                </div>

                <div class="line"></div>
                <div class="part-content">
                    <AvatarBase :userId="groupInfo.groupId" :width="60" :borderRadius="5" :showDetail="true">
                    </AvatarBase>
                </div>
                <div class="part-title">群号</div>
                <div class="part-content">{{ groupInfo.groupId }}</div>

                <div class="part-title">群聊昵称</div>
                <div class="part-content">{{ groupInfo.groupName }}</div>

                <div class="part-title">群公告</div>
                <div class="part-content">{{ groupInfo.groupNotice || "-" }}</div>

                <div class="line"></div>
                <a href="javascript:void(0)" class="leave-btn" @click="dissolutionGroup"
                    v-if="userInfoStore.getInfo().userId == groupInfo.groupOwnerId">解散群聊</a>

                <a href="javascript:void(0)" class="leave-btn" @click="leaveGroup" v-else>退出群聊</a>

            </div>





        </el-drawer>

        <UserSelect ref="userSelectRef" @callback="addOrRemoveUserCallback"></UserSelect>
    </div>
</template>






<script setup>
import UserSelect from "./UserSelect.vue";
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
import Avatar from "../../components/Avatar.vue";
const route = useRoute()
const router = useRouter()

const groupInfo = ref({})
const showDrawer = ref(false) // 是否显示抽屉
const memberList = ref([])

import { useUserInfoStore } from "@/stores/UserInfoStore"
const userInfoStore = useUserInfoStore()
const drawerRef = ref(null)

const show = async (groupId) => {
    let result = await proxy.Request({
        url: proxy.Api.getGroupInfo4Chat,
        params: {
            groupId
        },
        showError: false,

        errorCallback: (response) => {
            proxy.Confirm({
                message: response.info,
                showCancelBtn: false,
            })
        }
    })
    if (!result) {
        return;
    }
    showDrawer.value = true
    memberList.value = result.data.userContactList
    groupInfo.value = result.data.groupInfo

}  //接收一个groupId参

const closeDrawerHandler = () => {
    showDrawer.value = false;
}

defineExpose({ show }) //暴露给父组件的方法

import { robot_UID } from "../../utils/Constants";
const userSelectRef = ref(null)
const addUser = async () => {
    let result = await proxy.Request({
        url: proxy.Api.loadContact,
        params: {
            contactType: 'USER'
        }
    })

    if (!result) {
        return;
    }

    const contactIdList = memberList.value.map(item => item['userId']) //已经存在群里面的人
    contactIdList.push(robot_UID)
    let contactList = result.data
    //去掉已经存在群里面的人和机器人
    contactList = contactList.filter(item => !contactIdList.includes(item.contactId))

    userSelectRef.value.show({
        contactList,
        groupId: groupInfo.value.groupId,
        type: 1
    })
}
const removeUser = () => {
    let contactList = memberList.value.map(item => item)
    contactList.forEach((item) => {
        item.contactId = item.userId
    })
    contactList = contactList.filter(item => item.userId != groupInfo.value.groupOwnerId)

    userSelectRef.value.show({
        contactList,
        groupId: groupInfo.value.groupId,
        type: 0
    })
}


const addOrRemoveUserCallback = () => {
    showDrawer.value = false
}

const leaveGroup = () => {
    proxy.Confirm({
        message: `确定要退出群聊【${groupInfo.value.groupName}】吗？`,
        okfun: async () => {
            let result = await proxy.Request({
                url: proxy.Api.leaveGroup,
                params: {
                    groupId: groupInfo.value.groupId
                }
            })
            if (!result) {
                return;
            }
            proxy.Message.success("退出群聊成功")
            showDrawer.value = false; // 关闭抽屉

            //发送删除会话的操作直接调用父组件函数即可
            emit("delChatSessionCallBack", groupInfo.value.groupId)
            window.ipcRenderer.send('transferStation', 'RELOAD_CONTACT')
            window.ipcRenderer.send('transferStation', 'RELOAD_SESSION')
        }
    })
}

const delContact = async (groupId) => {
    let result = await proxy.Request({
        url: proxy.Api.removeContact,
        params: {
            contactId: groupId
        }
    })
    if (!result) {
        return;
    }
    window.ipcRenderer.send('transferStation', 'RELOAD_CONTACT')
}


const dissolutionGroup = () => {
    proxy.Confirm({
        message: `确定要解散群聊【${groupInfo.value.groupName}】吗？`,
        okfun: async () => {
            let result = await proxy.Request({
                url: proxy.Api.dissolutionGroup,
                params: {
                    groupId: groupInfo.value.groupId
                }
            })
            if (!result) {
                return;
            }
            showDrawer.value = false; // 关闭抽屉
            window.ipcRenderer.send('transferStation', 'RELOAD_SESSION')
            delContact(groupInfo.value.groupId)
        }
    })
}




const emit = defineEmits(["delChatSessionCallBack"])

</script>

<style lang="scss" scoped>
.group-panel {
    color: #000000;

    :deep(.mask-style) {
        top: 1px;
        right: 1px;
        height: calc(100vh - 2px);
    }

    :deep(.el-drawer) {
        -webkit-app-region: no-drag;
    }

    :deep(.el-drawer__header) {
        margin-bottom: 10px;
    }

    :deep(.el-drawer__body) {
        padding: 10px;
    }

    .group-panel-body {
        .member-list {
            display: flex;
            flex-wrap: wrap;

            .member-item {
                width: 20%;
                display: flex;
                flex-direction: column;
                align-items: center;
                margin-bottom: 5px;
                padding: 5px;
                position: relative;


                .nick-name {
                    margin-top: 3px;
                    width: 100%;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    font-size: 13px;
                    text-align: center;
                }

                .icon-op {
                    cursor: pointer;
                    width: 40px;
                    height: 40px;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    border: 1px solid #dbdbdb;
                    color: #6e6e6e;
                }
            }
        }



        .line {
            margin-bottom: 10px;
            border-top: 1px solid #ddd;
            height: 1px;
        }

        .part-title {
            margin-top: 10px;
        }

        .part-content {
            color: #757575;
            margin-bottom: 10px;
        }

        .leave-btn {
            color: #f45454;
            text-decoration: none;
            text-align: center;
            display: block;
            margin-top: 10px
        }
    }
}
</style>
