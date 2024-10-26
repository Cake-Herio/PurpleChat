<template>
    <ContentPanel>
        <div class="group-info-item">
            <div class="group-title">群封面：</div>
            <div class="group-value">
                <AvatarBase :userId="groupInfo.groupId" :width="100" :borderRadius="5" :showDetail="true"></AvatarBase>
            </div>

            <div class="more-op">
                <el-dropdown placement="bottom-end" trigger="click">
                    <span class="el-dropdown-link">
                        <div class="iconfont icon-more"></div>
                    </span>
                    <template #dropdown>
                        <!-- 在右方显示可选菜单 -->
                        <el-dropdown-menu v-if="groupInfo.groupOwnerId == userInfoStore.getInfo().userId">
                            <el-dropdown-item @click="editGroupInfo">修改群信息</el-dropdown-item>
                            <el-dropdown-item @click="dissolutionGroup">解散该群</el-dropdown-item>
                        </el-dropdown-menu>
                        <el-dropdown-menu v-else>
                            <el-dropdown-item @click="leaveGroup">退出该群</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </div>


        <div class="group-info-item">
            <div class="group-title">群ID：</div>
            <div class="group-value">{{ groupInfo.groupId }}</div>
        </div>
        <div class="group-info-item">
            <div class="group-title">群名称：</div>
            <div class="group-value">{{ groupInfo.groupName }}</div>
        </div>
        <div class="group-info-item">
            <div class="group-title">群成员：</div>
            <div class="group-value">{{ groupInfo.memberCount }}</div>
        </div>
        <div class="group-info-item">
            <div class="group-title">加入权限：</div>
            <div class="group-value">
                {{ groupInfo.joinType == 0 ? '直接加入' : '管理员同意后加入' }}
            </div>
        </div>
        <div class="group-info-item notice">
            <div class="group-title">公告：</div>
            <div class="group-value">{{ groupInfo.groupNotice || '-' }}</div>
        </div>

        <!-- 按钮 -->
        <div class="group-info-item">
            <div class="group-title"></div>
            <div class="group-value">
                <el-button type="primary" @click="sendMessage">发送群消息</el-button>
            </div>
        </div>

    </ContentPanel>
    <GroupEditDialog ref="groupEditDialogRef" @reloadGroupInfo="getGroupInfo()"></GroupEditDialog>
</template>




<script setup>
import GroupEditDialog from '@/views/contact/GroupEditDialog.vue'
import { ref, reactive, getCurrentInstance, nextTick, watch } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
import { useContactStateStore } from "@/stores/ContactStateStore"
import { useUserInfoStore } from "@/stores/UserInfoStore"
const userInfoStore = useUserInfoStore()
const contactStateStore = useContactStateStore()

const groupInfo = ref({})
const groupId = ref()

const getGroupInfo = async () => {

    let result = await proxy.Request({
        url: proxy.Api.getGroupInfo,
        params: {
            groupId: groupId.value
        },
        errorCallback: async () => {
            //获取失败应当在服務器数据库中删除该会话 
            await proxy.Confirm({
                message: `未找到信息，是否将其从列表中删除？`,
                okfun: async () => {
                    let result = await proxy.Request({
                        url: proxy.Api.removeContact,
                        params: {
                            contactId: groupId.value
                        }
                    })
                    if (!result) {
                        return;
                    }
                    contactStateStore.setContactReload('ALL')
                    router.push('/contact/blank')
                },
                showCancelBtn: true
            })


        },
        showError: false
    })
    if (!result) {
        return
    }
    groupInfo.value = result.data
}

const groupEditDialogRef = ref()

//点击后的响应式函数存放位置
const editGroupInfo = () => {
    if (groupEditDialogRef.value && typeof groupEditDialogRef.value.show === 'function') {
        groupEditDialogRef.value.show(groupInfo.value) // 将参数传递给子组件
    } else {
        console.error('GroupEditDialog component or show method is not properly referenced')
    }
    // groupEditDialogRef.value.show(groupInfo.value) //将参数传递给子组件
}

//解散群聊
const dissolutionGroup = async () => {
    let result = await proxy.Request({
        url: proxy.Api.dissolutionGroup,
        params: {
            groupId: groupInfo.value.groupId
        }
    })
    if (!result) {
        return;
    }
    proxy.Message.success('解散成功')
    //通过watch刷新群聊信息
    contactStateStore.setContactReload("DISSOLTION_GROUP")
}

//退出群聊
const leaveGroup = async () => {
    let result = await proxy.Request({
        url: proxy.Api.leaveGroup,
        params: {
            groupId: groupInfo.value.groupId
        }
    })
    if (!result) {
        return;
    }
    proxy.Message.success('退出成功')
    contactStateStore.setContactReload('LEAVE_GROUP')
    //刪除数据库中的session
    window.ipcRenderer.send('delChatSessionData', { contactId: groupInfo.value.groupId })
    //刷新sesion
    window.ipcRenderer.send('transferStation', "RELOAD_SESSION")
}

//发送消息
const sendMessage = () => {
    router.push({
        path: '/chat',
        query: {
            contactId: groupInfo.value.groupId, //传递值到另一个页面
            timestamp: new Date().getTime()
        }
    })

}






watch(() =>
    route.query.contactId,
    (newVal, oldVal) => {
        if (newVal) {
            groupId.value = newVal
            getGroupInfo()
        }
    },
    { immediate: true, deep: true });




// getGroupInfo()



</script>

<style lang="scss" scoped>
.group-info-item {
    display: flex;
    margin: 15px 0;
    align-items: center;
    text-align: left;

    .group-title {
        width: 100px;
        text-align: right;
    }

    .group-value {
        flex: 1;
    }
}

.notice {
    align-items: flex-start;
}
</style>
