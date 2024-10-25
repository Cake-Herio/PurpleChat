<template>
    <layout>
        <template #left-content>
            <!-- 顶部可拖动区域 -->
            <div class="drag-panel drag"></div>
            <div class="top-search">
                <el-input clearable placeholder="搜索联系人" v-model="searchKey" size="small" @keyup="search">
                    <template #suffix>
                        <span class="iconfont icon-search"></span>
                    </template>
                </el-input>
            </div>

            <!-- 左半部分 显示聊天列表 -->
            <div class="contact-list" v-show="!searchKey">
                <template v-for="item in partList" :key="item.partName">
                    <div class="part-title">{{ item.partName }}</div>
                    <div class="part-list">
                        <!-- 固定写死的 具有特殊功能-->
                        <div v-for="child in item.children" :key="child.name"
                            :class="['part-item', child.path === route.path ? 'active' : '']"
                            @click="goTo(child, item)">
                            <div :class="['iconfont', child.icon]" :style="{ backgroundColor: child.iconBgColor }">
                            </div>
                            <div class="text">{{ child.name }}</div>
                            <Badge :count=messageCountStore.getCount(child.countkey) :top="3" :left="45"></Badge>
                        </div>


                        <!-- 数据库查找到的列表 -->
                        <div v-for="contact in item.contactData" :key="contact.groupId"
                            :class="['part-item', contact[item.contactId] === route.query.contactId ? 'active' : '']"
                            @click="contactDetail(contact, item)">
                            <Avatar :userId="contact[item.contactId]" :width="35" v-if="item.partName == '联系人'">
                            </Avatar>
                            <Avatar :userId="contact[item.contactId]" :width="35" :showDetail="true" v-else>
                            </Avatar>
                            <!-- 这里用item.contactName的原因是判断当前是group还是uesrId， 也可以通过判断 不过这样简洁 -->
                            <div class="text">{{ contact[item.contactName] }}</div>
                        </div>

                        <template v-if="item.contactData && item.partName != '我的群聊' && item.contactData.length == 0">
                            <div class="no-data">{{ item.emptyMsg }}</div>
                        </template>


                    </div>
                </template>

                <svg t="1726887391214" class="icon-reload" @click="reloadContact" viewBox="0 0 1024 1024" version="1.1"
                    xmlns="http://www.w3.org/2000/svg" p-id="5923" width="200" height="200">
                    <path
                        d="M960 630.4c-12.8-3.2-25.6 3.2-32 12.8-76.8 204.8-320 307.2-544 227.2-224-80-342.4-307.2-265.6-512 76.8-204.8 320-307.2 544-227.2 92.8 32 172.8 92.8 224 172.8l-92.8 0c-12.8 0-25.6 9.6-25.6 22.4 0 12.8 9.6 22.4 25.6 22.4l153.6 0c12.8 0 25.6-9.6 25.6-22.4l0-140.8c0-12.8-9.6-22.4-25.6-22.4-12.8 0-25.6 9.6-25.6 22.4l0 89.6c-57.6-86.4-140.8-150.4-246.4-188.8-249.6-86.4-518.4 28.8-608 256-86.4 230.4 44.8 486.4 294.4 572.8 249.6 86.4 518.4-28.8 608-256C979.2 649.6 972.8 636.8 960 630.4z"
                        p-id="5924" fill="#d4c1f3"></path>
                </svg>
            </div>

            <div class="search-list" v-show="searchKey">
                <ContactSearchResult :data="item" v-for="item in searchList" :key="item"
                    @click="searchClickHandler(item)">
                </ContactSearchResult>
            </div>
        </template>

        <template #right-content>
            <div class="drag-panel drag">
                <div class="title-panel">
                    {{ rightTitle }}
                </div>
                <router-view v-slot="{ Component }">
                    <component :is="Component" ref="componentRef"></component>
                </router-view>
            </div>

        </template>
    </layout>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
import { ref, reactive, getCurrentInstance, nextTick, computed, watch, onMounted, onUnmounted } from 'vue'
const { proxy } = getCurrentInstance()
import { useMessageCountStore } from '@/stores/MessageCountStore'
const messageCountStore = useMessageCountStore();
const route = useRoute();
const router = useRouter();
import ContactSearchResult from './ContactSearchResult.vue';



const partList = ref([
    {
        partName: '我的助理',
        contactData: [],
        contactPath: '/contact/userDetail',
        contactId: 'contactId',
        contactName: 'contactName',
    },


    {
        partName: '新朋友',
        children: [
            {
                name: '搜好友',
                icon: 'icon-search',
                iconBgColor: '#fa9d3b',
                path: '/contact/search'
            },

            {
                name: '新的朋友',
                icon: 'icon-plane',
                iconBgColor: '#cfb0f7',
                path: '/contact/contactNotice',
                showTitle: true,
                countkey: 'contactApplyCount'
            }

        ],
    },

    {
        partName: '我的群聊',
        children: [
            {
                name: '新建群聊',
                icon: 'icon-add-group',
                iconBgColor: '#5485ee',
                path: '/contact/createGroup'
            }
        ],
        contactId: 'groupId',
        contactName: 'groupName',
        showTitle: true,
        contactData: [],
        contactPath: '/contact/groupDetail'
    },
    {
        partName: "我加入的群聊",
        contactId: 'contactId',
        contactName: 'contactName',
        showTitle: true,
        contactData: [],
        contactPath: '/contact/groupDetail',
        emptyMsg: '暂无加入的群聊'
    },
    {
        partName: '联系人',
        children: [],
        contactId: 'contactId',
        contactName: 'contactName',
        contactData: [],
        contactPath: '/contact/userDetail',
        emptyMsg: '暂无联系人'
    }

]);


const rightTitle = ref();
//点击跳转函数

const goTo = (child, part) => {
    if (part.showTitle) {
        rightTitle.value = child.name;
    } else {
        if (child.showTitle) {
            rightTitle.value = child.name;
        } else {
            rightTitle.value = '';
        }

    }
    //更新数量
    if (child.countkey) {
        messageCountStore.setCount(child.countkey, 0, true)
    }
    //更新数据库
    window.ipcRenderer.send('updateContactNoReadCount')


    router.push(child.path);
};

const contactDetail = (contact, part) => {
    if (part.showTitle) {
        rightTitle.value = contact[part.contactName];
    } else {
        rightTitle.value = '';
    }
    router.push({
        path: part.contactPath,
        query: {
            contactId: contact[part.contactId] //传的参数 传的是groupId或是userId
        }
    })


}


import { robot_UID } from '@/utils/Constants'

const loadContactList = async (contactType) => {

    let result = await proxy.Request({
        url: proxy.Api.loadContact,
        params: {
            contactType
        },
        showLoading: false
    })
    if (!result) {
        return;
    }

    if (contactType == 'USER') {
        //查robot
        let robot = result.data.find(item => item.contactId == robot_UID)
        if (robot) {
            if (partList.value[0].contactData.length == 0) {
                partList.value[0].contactData.push(robot);
            } else {
                partList.value[0].contactData[0] = robot
            }
        }
        partList.value[4].contactData = result.data.filter(item => item.contactId != robot_UID);
    } else if (contactType == 'GROUP') {
        partList.value[3].contactData = result.data;
    }
};

//加载我的群聊
const loadMyGroup = async () => {
    partList.value[2].contactData = []
    let result = await proxy.Request({
        url: proxy.Api.loadMyGroup,
        showLoading: false
    })
    if (!result) {
        return;
    }

    partList.value[2].contactData = result.data;
    partList.value[2].contactData.forEach((item, index) => {
    dynamicStore.setData("refresh" + item.groupId, new Date().getTime())
        
    });
};




loadMyGroup();
loadContactList('USER');
loadContactList('GROUP');




import { useContactStateStore } from '@/stores/ContactStateStore';
const contactStateStore = useContactStateStore();


const searchKey = ref("")
const searchList = ref([])
const search = () => {
    if (searchKey.value == "") {
        return
    }
    searchList.value = []
    const regex = new RegExp("(" + searchKey.value + ")", 'gi')
    let allContactList = []
    partList.value.forEach(item => {
        allContactList = allContactList.concat(item.contactData)
    })


    allContactList.forEach(item => {
        let contactName = item?.groupId !== undefined ? item.groupName : item?.contactName;
        if (contactName && contactName.includes(searchKey.value)) {
            let newItem = Object.assign({}, item);
            // 高亮搜索的内容
            const regex = new RegExp(`(${searchKey.value})`, 'gi');
            newItem.searchContactName = contactName.replace(regex, "<span class='highlight'>$1</span>");

            newItem.contactId = item.groupId !== undefined ? item.groupId : item?.contactId;
            searchList.value.push(newItem);
        }
    });
}
const searchClickHandler = (item) => {
    searchKey.value = undefined
    //如果是群聊详情
    if (item.groupId !== undefined) {
        contactDetail(item, partList.value[3])
    } else {
        contactDetail(item, partList.value[4])
    }


}

const componentRef = ref()
const onReceiveMessage = () => {
    window.ipcRenderer.on("receiveMessage", (event, message) => {
        if (message.messageType == 4) { //刷新申请消息
            window.ipcRenderer.send("loadContactApply", {})
        }
    })
}

const onLoadContactApplyCallBack = () => {
    window.ipcRenderer.on("loadContactApplyCallBack", (event) => {
        if (route.path === '/contact/contactNotice' && componentRef.value) {
            componentRef.value.loadApply("BLANK")
        }
    })
}





watch(
    () => contactStateStore.contactReload,
    (newVal, oldVal) => {
        if (!newVal) {
            return;
        }
        switch (newVal) {
            case 'MY':
                router.push('/contact/blank');
                loadMyGroup();
                rightTitle.value = '';
                break;
            case 'USER':
            case 'GROUP':
                loadContactList(newVal);
                break;
            case 'ALL':
                loadContactList('USER');
                loadContactList('GROUP');
                loadMyGroup()
                break;

            case 'REMOVE_USER':
                loadContactList('USER');
                router.push('/contact');
                rightTitle.value = '';
                break;


            case 'DISSOLTION_GROUP':
                loadMyGroup()
                router.push('/contact');
                rightTitle.value = '';
                break;

            case 'LEAVE_GROUP':
                loadContactList('GROUP');
                router.push('/contact/blank');
                rightTitle.value = '';
                break;
            case 'BLANK':
                router.push('/contact/blank');
                rightTitle.value = '';
                break;
        }
        contactStateStore.setContactReload(null) //修复到初始状态
    },
    { immediate: true, deep: true }
)
import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore();
import { useUserInfoStore } from '@/stores/UserInfoStore';
const userInfoStore = useUserInfoStore();
const onRefreshAvatar = () => {
    window.ipcRenderer.on('refreshAvatar', (event, data) => {
        loadContactList('GROUP')
        loadContactList('USER')
        if (data && data.sendUserId != null) {
            setTimeout(() => { dynamicStore.setData("refresh" + data.sendUserId, new Date().getTime()) }, 2000)
        } else {
            dynamicStore.setData("refresh" + userInfoStore.getInfo().userId, new Date().getTime())
        }

    });
}
const reloadContact = () => {
    contactStateStore.setContactReload('ALL')
}

const onContactMessage = () => {
    window.ipcRenderer.on("contactMessage", (event, data) => {
        switch (data) {
            case "RELOAD":
                loadContactList('USER');
                loadContactList('GROUP');
                break;

            case "RELOAD_GROUP":
                loadContactList('GROUP');
                loadMyGroup();
                break;
        }
    })
}

const onSaveGroupAvatarCallBack = () => {
    window.ipcRenderer.on('saveGroupAvatarCallBack', (event, groupId) => {
        console.log("保存群头像成功")
        dynamicStore.setData("refresh" + groupId, new Date().getTime())
    });
}


onUnmounted(() => {
    window.ipcRenderer.removeAllListeners('refreshAvatar');
    window.ipcRenderer.removeAllListeners('contactMessage');
    window.ipcRenderer.removeAllListeners('saveGroupAvatarCallBack');


});

onMounted(() => {
    onContactMessage()
    onLoadContactApplyCallBack()
    onReceiveMessage()
    onRefreshAvatar();
    onSaveGroupAvatarCallBack()
})


</script>

<style lang="scss" scoped>
.drag-panel {
    height: 25px;
    background: #f7f7f7;
}

.top-search {
    padding: 0px 10px 9px 10px;
    background: #f7f7f7;
    display: flex;
    align-items: center;

    .iconfont {
        font-size: 12px;
    }
}

.contact-list {
    border-top: 1px solid #ddd;
    height: calc(100vh - 62px);
    overflow: hidden;
    background: #ebebea;

    &:hover {
        overflow: auto;
    }
}

.part-title {
    color: #515151;
    padding-left: 10px;
    margin-top: 10px;
    margin-bottom: 5px;
}

.part-list {
    border-bottom: 1px solid #ddd;

    .part-item {
        display: flex;
        align-items: center;
        padding: 10px 10px;
        position: relative;
        border-bottom: 1px solid #e0dfdf;
        &:hover {
            cursor: pointer;
            background: #ede3f5;
            transition: all 0.3s;
        }

        //&是父选择器的引用

        .iconfont {
            width: 35px;
            height: 35px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            color: #fff;
            border-radius: 3px;
        }

        .text {
            flex: 1;
            color: #000000;
            margin-left: 10px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    }
}

.no-data {
    text-align: center;
    font-size: 12px;
    color: #9d9d9d;
    line-height: 30px;
}

.active {
    background: #ede3f5;

    &:hover {
        background: #ede3f5;
    }
}

.title-panel {
    width: 100%;
    height: 60px;
    display: flex;
    align-items: center;
    padding-left: 10px;
    font-size: 18px;
    color: #000000;
}

.icon-reload {
    width: 20px;
    height: 20px;
    margin: 10px;

    //鼠标
    &:hover {
        cursor: pointer;
    }

    //居中
    margin-left: calc(50% - 10px);
}
</style>
