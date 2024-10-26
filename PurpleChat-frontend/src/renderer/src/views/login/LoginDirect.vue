<template>
  <div class="login-panel">
    <div class="title drag">PurpleChat</div>


    <transition name="fade">  
      <div class="init-panel" v-show="ini">
        <img src="../../assets/icon/icon.png" />
        <div class="logo-word">Purple Chat</div>
        <img style="width: 40px; margin-top: 70px;" src="../../assets/img/ini-loading.gif" />
      </div>

    </transition>

    <transition name="fade">
      <div v-show="showLoading" class="loading-panel">
        <img src="../../assets/img/loading.gif" />
      </div>
    </transition>



    <transition name="fade">
      <div class="login-form" v-show="showForm">
        <el-form :model="formData" ref="formDataRef" label-width="10px" @submit.prevent>
          <!--input输入-->
          <el-form-item prop="avatarFile">
            <div class="avatar-panel">
              <AvatarBase :width="100" :userId="formData.userId" :partType="'avatar'" :forceGet="false" :isLogin="true"
                :borderRadius="8"></AvatarBase>
            </div>

            <!-- 如果能查到值就做成下拉的 -->
            <el-dropdown v-if="localUserList.length > 0" trigger='click'>
              <span class="iconfont icon-down"></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="item in localUserList" :key="item.email">
                    <div class="email-select" @click="formData = item">
                      <div class="drop-panel">
                        <AvatarBase :width="30" :userId="item.userId" :partType="'avatar'" :isLogin="true"></AvatarBase>
                        <div class="user-id">{{ item.userId }}</div>

                      </div>

                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-form-item>

          <el-form-item prop="userId" v-model="formData.nickName">
            <div class="login-nickname">
              {{ formData.nickName }}
            </div>

          </el-form-item>


          <el-button type="primary" @click="submit" class="login-btn">
            快速登录
          </el-button>

          <div class="bottom-links-container">
            <div class="bottom-link" @click="changeLoginType">
              <span class="a-link">密码登录</span>
            </div>
          </div>
        </el-form>
      </div>

    </transition>

  </div>

  <win-op :showSetTop=false :showMin=false :showMax=false :closeType="0"></win-op>
</template>



<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted, watch } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
const showLoading = ref(false)
const ini = ref(false)
const formData = ref({
  userId: '',
  email: '',
  nickName: '',
  token: '',
});
import { useDynamicStore } from '@/stores/dynamicStore';
const dynamicStore = useDynamicStore()
watch(
  () => dynamicStore.getStatus(),
  (newVal, oldVal) => {
    if(newVal == 'MEDIA_LOADED'){
      ini.value = false
      showForm.value = true

    }
  }
)





const formDataRef = ref();
const showForm = ref(false)

const init = () => {
  //发消息给主进程 ws相关信息
  window.ipcRenderer.send('setLocalStore',
    { key: "prodDomain", value: proxy.Api.prodDomain }
  )
  window.ipcRenderer.send('setLocalStore',
    { key: "devDomain", value: proxy.Api.devDomain }
  )
  window.ipcRenderer.send('setLocalStore',
    { key: "prodWsDomain", value: proxy.Api.prodWsDomain }
  )
  window.ipcRenderer.send('setLocalStore',
    { key: "devWsDomain", value: proxy.Api.devWsDomain }
  )
  window.ipcRenderer.send('loadLocalUser')

  window.ipcRenderer.send('startTempServer')

  globalInfoStore.setInfo('localServerPort', 60000)
}

const changeLoginType = () => {
  router.push('/login')
}



const localUserList = ref([])

import { useGlobalInfoStore } from '@/stores/GlobalInfoStore'
const globalInfoStore = useGlobalInfoStore()
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()
const submit = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loginDirect,
    params: {
      token: formData.value.token,
    },
    method: 'get',
    errorCallback: (response) => {
      //如果用户不存在 删除本地存储
      if (response.info == "未查找到用户，请核实ID后登录") {
        window.ipcRenderer.send('deleteUserSetting', formData.value.userId)
      } else if (response.info == "身份失效，请使用密码登录") {
        window.ipcRenderer.send('deleteUserSetting', formData.value.userId)
      }
      ini.value = false
      router.push('/login')
    }


  })
  if (!result) {
    return;
  }
  showForm.value = false
  showLoading.value = true
  userInfoStore.setInfo(result.data)
  localStorage.setItem('token', result.data.token)

  window.ipcRenderer.send('openChat', {
    email: formData.value.email,
    admin: result.data.admin,
    token: result.data.token,
    userId: result.data.userId,
    nickName: result.data.nickName,
    isDirect: true
  })

}


const onOpenChatCallBack = () => {
  window.ipcRenderer.on('openChatCallBack', (event) => {
    setTimeout(() => {
      // showLoading.value = false
      router.push('/main')
    }, 400)

  })
}





onMounted(() => {
  ini.value = true
  //定时
  init() 
  onOpenChatCallBack()


  window.ipcRenderer.on('loadLocalUserCallBack', (event, data) => {
    //如果数据为空 直接跳转密码登录
    if (data.length == 0) {
      router.push('/login')
      return
    }
    localUserList.value = data
    formData.value = data[0]
  })
})


onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('openChatCallBack')
  window.ipcRenderer.removeAllListeners('loadLocalUserCallBack')


})

</script>

<style lang="scss" scoped>
.email-select {
  width: 240px;
}


.loading-panel {
  height: calc(99vh - 48px);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  opacity: 0;
  transition: opacity 0.3s ease; // 添加过渡效果

  img {
    width: 120px;
  }

  &.fade-enter-active,
  &.fade-leave-active {
    transition: opacity 1s;
  }

  &.fade-enter-from,
  &.fade-leave-to {
    opacity: 0;
  }

  &.fade-enter-to,
  &.fade-leave-from {
    opacity: 1;
  }
}

.init-panel {
  opacity: 1; // Initial state
  transition: opacity 1s ease; // Transition for the fade effect

  img {

    width: 100px;
    margin: 30px auto;
    margin-bottom: 20px;
    display: block;
  }

  &.fade-enter-active {
    transition: opacity 0.3s ease;
  }

  &.fade-leave-active {
    transition: opacity 0.05s ease;
  }

  &.fade-enter-from,
  &.fade-leave-to {
    opacity: 0;
  }

  &.fade-enter-to,
  &.fade-leave-from {
    opacity: 1;
  }
}


.login-panel {
  background: #fff;
  border-radius: 3px;
  border: 10px solid white;

  .title {
    height: 30px;
    padding: 5px, 0px, 0px, 10px;
  }
}

.login-form {
  &.fade-enter-active,
  &.fade-leave-active {
    transition: opacity 0.3s ease;
  }

  &.fade-enter-from,
  &.fade-leave-to {
    opacity: 0;
  }

  &.fade-enter-to,
  &.fade-leave-from {
    opacity: 1;
  }
  background: #fff;
  padding: 5px;
  /* 为面板添加适当的填充，使内容不紧贴边缘 */
  margin: auto;
  /* 使面板在页面中水平居中 */
  display: flex;
  flex-direction: column;

  .avatar-panel {
    display: flex;
    /* 使用 flexbox 布局 */
    justify-content: center;
    /* 水平居中 */
    align-items: center;
    /* 垂直居中 */
    height: 100px;
    //水平居中
    margin: 0 auto;
    margin-top: 20px;
    margin-left: 65px;
  }

  .login-nickname {
    display: flex;
    /* 使用 flexbox 布局 */
    justify-content: center;
    /* 水平居中 */
    align-items: center;
    /* 垂直居中 */
    margin: 10px auto;
    display: flex;
    align-items: center;
    height: 30px;
    font-size: 16px;
    color: #6c6c6c;
    //字体加粗
    // color: #333;
    padding-right: 10px;


  }



}

.email-panel {
  align-items: center;
  width: 100%;
  overflow: hidden;
  display: flex;


  .icon-down {
    cursor: pointer;
    border: none;
    overflow: hidden; // 隐藏溢出部分
    overflow-x: auto;


  }
}

.drop-panel {
  display: flex;

  .user-id {
    flex: 1;
    margin-left: 20px;
    margin-top: 3px;
    text-align: left;
    //垂直居中

  }
}

.error-msg {
  line-height: 30px;
  height: 30px;
  color: #fb7373;
}

.check-code-panel {
  display: flex;

  .check-code {
    cursor: pointer;
    width: 120px;
    margin-left: 5px;
  }
}

.login-btn {
  //水平居中
  margin: 25px auto;
  display: flex;
  /* 使用 flexbox 布局 */
  justify-content: center;
  /* 水平居中 */
  background: #cfb0f7;
  height: 36px;
  font-size: 16px;
  width: 200px;

  &:hover {
    background: rgb(216, 191, 249);
    //动画对颜色进行过渡
    transition: all 0.5s;
  }
}

.bottom-links-container {
  display: flex;
  justify-content: space-between;

  .a-link {
    color: #deb8f8;

    &:hover {
      color: rgb(168, 108, 248);
      transition: all 0.5s;
    }
  }
}

.logo-word {
  font-size: 16.6px;

  text-align: center;
  margin-top: 20px;
  color: #d6b2fc;
  animation: glow 1.5s infinite ease-in-out, pulse 2s infinite ease-in-out;

}


@keyframes glow {
  0% {
    text-shadow: 0 0 5px #e6d7ff, 0 0 10px #e6d7ff, 0 0 20px #e6d7ff, 0 0 30px #f2eaff, 0 0 40px #f2eaff, 0 0 50px #f2eaff;
  }

  50% {
    text-shadow: 0 0 10px #e6d7ff, 0 0 20px #e6d7ff, 0 0 30px #f1e3fb, 0 0 40px #f2eaff, 0 0 50px #f2eaff, 0 0 50px #f2eaff;
  }

  100% {
    text-shadow: 0 0 5px #e6d7ff, 0 0 10px #e6d7ff, 0 0 20px #e6d7ff, 0 0 30px #f2eaff, 0 0 40px #f2eaff, 0 0 45px #f2eaff;
  }
}


@keyframes pulse {
  0% {
    transform: scale(0.97);
  }

  50% {
    transform: scale(1);
  }

  100% {
    transform: scale(0.97);
  }
}

.bottom-link {
  margin-top: 15px;
}
</style>
