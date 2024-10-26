<template>
    <ContentPanel>
        <div class="user-info">
            <UserBaseInfo :userInfo="userInfo" :showArea="true" >
            </UserBaseInfo>
            <div class="more-op" v-if="userInfo.userId != robot_UID">
                <el-dropdown placement="bottom-end" trigger="click">
                    <span class="el-dropdown-link">
                        <div class="iconfont icon-more"></div>
                    </span>
                    
                    <template #dropdown >
                        <!-- 在右方显示两个可选菜单 -->
                        <el-dropdown-menu> 
                            <el-dropdown-item @click="addContact2BlackList">拉入黑名单</el-dropdown-item>
                            <el-dropdown-item @click="delContact">删除联系人</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
            
        </div>
        <!-- 个性签名 -->
        <div class="part-item">
            <div class="part-title">个性签名</div>
            <div class="part-content">
                {{ userInfo.personalSignature || '-'}}
            </div>
        </div>

        <!-- 发消息按钮 -->
        <div class="send-message" @click="sendMessage">
            <div class="iconfont icon-chat2"></div>
            <div class="text">发消息</div>
        </div>
    </ContentPanel>
</template>

<script setup>
import { robot_UID } from "@/utils/Constants";
import { ref, reactive, getCurrentInstance, nextTick, watch } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();
import { useContactStateStore } from "@/stores/ContactStateStore";
const contactState = useContactStateStore();



//通过后端查信息
const userInfo = ref({
});


const loadUserDetail = async (contactId) => {

    let result = await proxy.Request({
        url:proxy.Api.getContactInfo,
        params:{
        contactId: contactId
        },
        showLoading: false
    })
    if(!result){
        return;
    }
    userInfo.value = result.data;
}


// 按钮响应函数
const addContact2BlackList = () => {
    //调用提示函数 里面有现成的一个弹窗 提示是否继续操作
    proxy.Confirm({
        message: '确定要将该联系人拉入黑名单吗？',
        okfun: async () => {
            let result = await proxy.Request({
                url:proxy.Api.addContact2BlackList,
                params:{
                contactId : userInfo.value.userId
                }
            })
            if(!result){
                return;
            }
            delContactData()
        }
        
    })
}
import { useDynamicStore } from '@/stores/dynamicStore';
const dynamicStore = useDynamicStore()
const delContact = () => {
    proxy.Confirm({
        message: '确定要删除联系人吗？',
        okfun: async () => {
            let result = await proxy.Request({
                url:proxy.Api.delContact,
                params:{
                contactId : userInfo.value.userId
                }
            })
            if(!result){
                return;
            }
            window.ipcRenderer.send('transferStation', 'RELOAD_SESSION')
            delContactData()
        }
    })
}   



const delContactData = () => {
    contactState.setContactReload('REMOVE_USER')
    //移除数据库的信息
    window.ipcRenderer.send('removeContact', route.query.contactId)

}

const sendMessage = () => {
    router.push({
        path: '/chat',
        query: {
            contactId: userInfo.value.userId,
            timestamp: new Date().getTime() 
        }
    })
}





watch(() => 
    route.query.contactId, //contactId是根据路由传递过来的
    (newVal, oldVal) => {
        if (newVal) {
            
            loadUserDetail(newVal);
        }
    },
    { immediate: true, deep: true });
    
</script>

<style lang="scss" scoped>
.user-info {
    display: flex;
    align-items: center; /* 垂直居中对齐 */
    justify-content: space-between; /* 水平分布 */
    .more-op {
        position: static; /* 重置位置，使其参与 Flexbox 布局 */
        .icon-more {
            color: #9e9e9e;
            &:hover {
                background: #dddddd;
            }
        }
    }
}

.part-item {
    display: flex;
    border-bottom: 1px solid #eaeaea;
    padding: 20px 0px;
    .part-title {
        width: 60px;
        color: #000;
    }
    .part-content {
        flex: 1;
        margin-left: 15px;
        color: #161616;
    }
    text-align: left;
}

.send-message {
    width: 80px;
    margin: 0px auto;
    text-align: center;
    margin-top: 20px;
    color: #cfb0f7;
    padding: 5px;
    .icon-chat2 {
        font-size: 23px;
    }
    .text {
        font-size: 12px;
        margin-top: 5px;
    }
    &:hover {
        cursor: pointer;
        background: #e9e9e9;
    }
}





</style>
