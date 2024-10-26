<template>
    <ContentPanel>
        <div class="search-form">
            <el-input v-model="contactId" placeholder="请输入用户Id或群聊Id" clearable size="large"
                @keydown.enter="search"></el-input>
            <div class="search-btn iconfont icon-search" @click="search"></div>
        </div>


        <div v-if="searchResult && Object.keys(searchResult).length > 0" class="search-result-panel">
            <div class="search-result">
                <span class="contact-type">{{ contactTypeName }}</span>
                <UserBaseInfo :userInfo="searchResult" :showArea="searchResult.contactType == 'USER'" :forceGet="true"
                    :isSearch="true">
                </UserBaseInfo>
            </div>

            <div class="op-btn" v-if="searchResult.contactId != userInfoStore.getInfo().userId">
                <el-button type="primary" v-if="searchResult.status == null ||
                    searchResult.status == 0 ||
                    searchResult.status == 2 ||
                    searchResult.status == 3 ||
                    searchResult.status == 4" @click="applyContact">{{ searchResult.contactType == 'USER' ? "添加到联系人" :
                        "申请加入群聊" }}</el-button>
                <el-button type="primary" v-if="searchResult.status == 1" @click="sendMessage">发消息</el-button>
                <span v-if="searchResult.status == 5 || searchResult.status == 6">对方拉黑了你</span>
            </div>
        </div>
        <div v-if="!searchResult" class="no-data">暂无数据</div>
    </ContentPanel>
    <!--当子组件searchAdd 触发 reload 事件时 ，父组件将调用resetForm事件 -->
    <searchAdd ref="searchAddRef" @reload="resetForm"></searchAdd>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
const { proxy } = getCurrentInstance()
const contactId = ref('')
const searchResult = ref([])
import { useUserInfoStore } from '@/stores/UserInfoStore'
import searchAdd from '@/views/contact/SearchAdd.vue';
import { useRouter, useRoute } from 'vue-router';
const userInfoStore = useUserInfoStore()

const contactTypeName = computed(() => {
    if (searchResult.value.contactId === userInfoStore.getInfo().userId) {
        return '自己'
    }
    else if (searchResult.value.contactType == 'USER') {
        return '用户'
    } else if (searchResult.value.contactType == 'GROUP') {
        return '群聊'
    }
})
const router = useRouter()
const sendMessage = () => {
    router.push({
        path: '/chat',
        query: {
            contactId: searchResult.value.contactId, //传递值到另一个页面
            timestamp: new Date().getTime()
        }
    })

}


const search = async () => {
    //添加id的正则校验
    if (!proxy.Verify.checkId(contactId.value)) {
        proxy.Message.error("请输入有效Id")
        return
    }


    if (!contactId.value) {
        proxy.Message.error("请输入联系人Id或群聊Id")
        return
    }
    let result = await proxy.Request(
        {
            url: proxy.Api.search,
            method: 'post',
            params: {
                contactId: contactId.value
            }
        }
    )
    if (!result) {
        return;
    }
    searchResult.value = result.data
}

const searchAddRef = ref()
const applyContact = () => {
    searchAddRef.value.show(searchResult.value) //将参数传递给子组件searchAdd
}

const resetForm = () => {
    searchResult.value = []
    contactId.value = ''
}





</script>
<style lang="scss" scoped>
.search-form {
    padding-top: 50px;
    display: flex;
    align-items: center;

    :deep(.el-input__wrapper) {
        border-radius: 4px 0px 0px 4px;
        border-right: none;
    }
}

.search-btn {
    background: #cfb0f7;
    color: #fff;
    line-height: 40px;
    width: 80px;
    text-align: center;
    border-radius: 0px 5px 5px 0px;
    cursor: pointer;

    &:hover {
        background: #d8bbff;
        transition: all 0.3s;
    }
}

.no-data {
    padding: 30px 0px;
}

.search-result-panel {
    .search-result {
        padding: 30px 20px 20px 20px;
        background: #fff;
        border-radius: 5px;
        margin-top: 10px;
        position: relative;

        .contact-type {
            position: absolute;
            left: 0px;
            top: 0px;
            background: #e5d1f6;
            padding: 2px 5px;
            color: #fff;
            border-radius: 5px 0px 0px 5px;
            font-size: 12px;
        }
    }

    .op-btn {
        border-radius: 5px;
        margin-top: 10px;
        padding: 10px;
        background: #fff;
        text-align: center;
    }
}
</style>
