<template>
  <div class="main">
    <div class="left-sider">
      <div>
        <Avatar :width="35" :userId="userId" :borderRadius="5"></Avatar>
      </div>



      <div class="menu-list">
        <template v-for="item in menuList" :key="item.name">
          <div :class="['tab-item iconfont', item.icon, item.path == currentMenu.path ? 'active' : '']"
            v-if="item.position == 'top'" @click="changeMenu(item)">
            <Badge :count=messageCountStore.getCount(item.countkey) :top="1" :left="10"></Badge>
          </div>

        </template>
      </div>
      <div class="menu-button">
        <template v-for="item in menuList" :key="item.name">
          <div :class="['tab-item iconfont', item.icon, item.path == currentMenu.path ? 'active' : '']"
            v-if="item.position == 'bottom'" @click="changeMenu(item)"></div>
        </template>
      </div>
    </div>
    <div class="right-container">
        <router-view v-slot="{ Component }">
          <keep-alive include="Chat,Contact">
            <component :is="Component" />
          </keep-alive>
        </router-view>

    </div>
  </div>
  <win-op></win-op>
  <Update></Update>
</template>

<script setup>

import Update from './Update.vue'
import { ref, reactive, getCurrentInstance, nextTick, onMounted, watch, onUnmounted, onActivated, onDeactivated, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const { proxy } = getCurrentInstance()
import { useMessageCountStore } from '@/stores/MessageCountStore'
const messageCountStore = useMessageCountStore()
const isChat = computed(() => {
  console.log(router.currentRoute.value.name == '聊天')
  return router.currentRoute.value.name == '聊天'
})
import { useUpdateByTimeStampStore } from '@/stores/UpdateByTimeStampStore';
const updateByTimeStampStore = useUpdateByTimeStampStore();
const menuList = ref([
  {
    name: 'chat',
    icon: 'icon-chat',
    path: '/chat',
    countkey: 'chatCount',
    position: 'top'
  },
  {
    name: 'contact',
    icon: 'icon-user',
    path: '/contact',
    countkey: 'contactApplyCount',
    position: 'top'
  },
  {
    name: 'mysetting',
    icon: 'icon-more2',
    path: '/setting',
    countkey: 'bottom',
    position: 'bottom'
  }
])

import { useUserInfoStore } from '@/stores/userInfoStore'
const userInfoStore = useUserInfoStore()
const userId = ref(userInfoStore.getInfo().userId)
const currentMenu = ref(menuList.value[0])
const changeMenu = (item) => {
  currentMenu.value = item
  router.push(item.path)
}





//添加用户信息接口
const getLoginInfo = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getUserInfo,
    params: {
      userId: userId.value
    }
  })
  if (!result) {
    return;
  }
  userInfoStore.setInfo(result.data)
  window.ipcRenderer.send('getLocalStore', result.data.userId + 'localServerPort')
}

import { useGlobalInfoStore } from '@/stores/globalInfoStore'
const globalInfoStore = useGlobalInfoStore()
import { useSysSettingStore } from '@/stores/sysSettingStore'
const sysSettingStore = useSysSettingStore()
const getSysSetting = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getSysSetting
  })
  if (!result) {
    return;
  }

  sysSettingStore.setSysSetting(result.data)

}

import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore();


const onInit = () => {
  window.ipcRenderer.on("init", (event, initdata) => {
    dynamicStore.setData("email", initdata.email)
  })
}



const onForceGetAvatarCallBack = () => {
  window.ipcRenderer.on('forceGetAvatarCallBack', (event, fileId) => {
    dynamicStore.setData("refresh" + fileId, new Date().getTime())
  })
}

const onForceGetChatImageCallBack = () => {
  window.ipcRenderer.on('forceGetChatImageCallBack', (event, fileId) => {
    console.log('forceGetChatImageCallBack')
    dynamicStore.setData("refresh" + fileId, new Date().getTime())
  })
}


//文件保存本地成功
const onSaveSuccess = () => {
  window.ipcRenderer.on('saveSuccess', (event) => {
    proxy.Message.success('保存成功')

  })
}

//监听后端错误输出给前端
const onError2Front = () => {
  window.ipcRenderer.on('error2Front', (event, error) => {
    proxy.Message.error(error)
  })
}




onMounted(() => {
  onInit()
  getSysSetting()
  getLoginInfo()
  onForceGetAvatarCallBack()
  onForceGetChatImageCallBack()
  onError2Front()
  console.log('onMounted')
  window.ipcRenderer.on('getLocalStoreBack', (event, serverPort) => {
    globalInfoStore.setInfo("localServerPort", serverPort)
  })


  window.ipcRenderer.on('reloginCallBack', (event) => {
    //退回到登录界面
    router.push('/loginDirect')
  })
  updateByTimeStampStore.updateTimestamp()
  onSaveSuccess()
})

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('getLocalStoreBack');
  window.ipcRenderer.removeAllListeners('reloginCallBack');
  window.ipcRenderer.removeAllListeners('forceGetAvatarCallBack');
  window.ipcRenderer.removeAllListeners('init');
  window.ipcRenderer.removeAllListeners('forceGetChatImageCallBack');

  window.ipcRenderer.removeAllListeners('saveSuccess');
  window.ipcRenderer.removeAllListeners('error2Front');
})



const menuSelect = (path) => {
  let menu = menuList.value.find(item => item.path == path)
  if (menu) {
    currentMenu.value = menu
  }
}

const route = useRoute()

//监听路由变换 及时更新渲染
watch(
  () => route.path,
  (newVal, oldVal) => {
    if (newVal) {
      menuSelect(newVal)
    }
  },
  { immediate: true, deep: true }
)


</script>





<style lang="scss" scoped>
.main {
  background: #ddd;
  display: flex;
  border-radius: 0px 3px 3px 0px;
  overflow: hidden;

  .left-sider {
    width: 55px;

    //从上到下渐变
    background: linear-gradient(180deg, #c395ff 0-50%, #010101 100%);
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 35px;
    border: 1px solid #2e2e2e;
    border-right: none;
    padding-bottom: 10px;
  }

  .menu-list {
    width: 100%;
    flex: 1;

    .tab-item {
      color: #d3d3d3;
      font-size: 20px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-top: 10px;
      cursor: pointer;
      font-size: 22px;
      position: relative;
    }

    .active {
      color: #cfb0f7;
    }
  }


  .menu-button {
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    color: #d3d3d3;
    font-size: 20px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 10px;
    cursor: pointer;
    font-size: 22px;
    position: relative;

    .active {
      color: #cfb0f7;
    }
  }

  .right-container {
    flex: 1;
    overflow: hidden;
    border: 1px solid #ddd;
    border-left: none;
  }

  .popover-user-panel {
    padding: 10px;

    .popover-user {
      display: flex;
      border-bottom: 1px solid #ddd;
      padding-bottom: 20px;
    }

    .send-message {
      margin-top: 10px;
      text-align: center;
      padding: 20px, 0, 0, 0;
    }
  }
}


</style>
