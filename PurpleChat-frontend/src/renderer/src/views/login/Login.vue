<template>
  <div class="login-panel">
    <div class="title drag">PurpleChat</div>
    <transition name="fade">
      <div v-show="showLoading" class="loading-panel">
        <img src="../../assets/img/loading.gif" />
      </div>
    </transition>


    <div class="login-form" v-show="showForm">
      <div class="error-msg">{{ errorMsg }}</div>

      <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="10px" @submit.prevent>
        <!--input输入-->
        <el-form-item prop="accountId" v-if="isloginByEmail && isLogin">
          <div class="email-panel">
            <el-input size="large" clearable placeholder="请输入邮箱/账号" v-model.trim="formData.accountId" maxlength="30"
              @focus="cleanVerify">
              <template #prefix>
                <span class="iconfont icon-email"></span>
              </template>
            </el-input>
            <!-- 如果能查到值就做成下拉的 -->
            <el-dropdown v-if="isLogin && localUserList.length > 0" trigger='click'>
              <span class="iconfont icon-down"></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="item in localUserList" :key="item.email">
                    <div class="email-select" @click="formData.accountId = item.email">
                      <div class="drop-panel">
                        <AvatarBase :width="30" :userId="item.userId" :partType="'avatar'"></AvatarBase>
                        <div class="user-id">{{ item.email }}</div>
                      </div>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-form-item>

        <el-form-item prop="email" v-if="!isloginByEmail || !isLogin">
          <div class="email-panel">
            <el-input size="large" clearable placeholder="请输入邮箱" v-model.trim="formData.email" maxlength="30"
              @focus="cleanVerify">
              <template #prefix>
                <span class="iconfont icon-email"></span>
              </template>
            </el-input>
            <!-- 如果能查到值就做成下拉的 -->
            <el-dropdown v-if="isLogin && localUserList.length > 0" trigger='click'>
              <span class="iconfont icon-down"></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="item in localUserList" :key="item.email">
                    <div class="email-select" @click="formData.email = item.email">
                      <div class="drop-panel">
                        <AvatarBase :width="30" :userId="item.userId" :partType="'avatar'" :isLogin="true"></AvatarBase>
                        <div class="user-id">{{ item.email }}</div>
                      </div>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-form-item>

        <el-form-item prop="emailCheck" v-if="!isloginByEmail || !isLogin">
          <div class="check-code-panel">
            <el-input size="large" clearable placeholder="请输入邮箱验证码" v-model.trim="formData.emailCheck" maxlength="30"
              @focus="cleanVerify">
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>

            <el-button type="primary" @click="submitEmailCheckByRule" :disabled="disableButton">
              获取
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="nickname" v-if="!isLogin">
          <el-input size="large" clearable placeholder="请输入昵称" v-model.trim="formData.nickname" maxlength="15"
            @focus="cleanVerify">
            <template #prefix>
              <span class="iconfont icon-user-nick"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password" v-if="isloginByEmail || !isLogin">
          <el-input size="large" show-password placeholder="请输入密码" v-model.trim="formData.password"
            @focus="cleanVerify">
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>



        <el-form-item prop="repassword" v-if="!isLogin">
          <el-input size="large" show-password placeholder="请再次输入密码" v-model.trim="formData.repassword"
            @focus="cleanVerify">
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="checkcode">
          <div class="check-code-panel">
            <el-input size="large" clearable placeholder="请输入验证码" v-model.trim="formData.checkcode" @focus="cleanVerify"
              @keyup.enter="submitByRule">//表示绑定聚焦事件 ，点击则调用函数
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img :src="checkCodeUrl" @click="changeCheckCode" class="check-code" />
          </div>
        </el-form-item>

        <el-button type="primary" @click="submitByRule" class="login-btn">{{
          isLogin ? '登录' : '注册'
        }}</el-button>

        <div class="bottom-links-container">
          <div class="bottom-link" @click="changeOnType">
            <span class="a-link">{{ isLogin ? '没有账号' : '已有账号' }}</span>
          </div>
          <div class="bottom-link" @click="changeOnLoginType" v-if="isLogin">
            <span class="a-link">{{ isloginByEmail ? '忘记密码' : '邮箱登录' }}</span>
          </div>
        </div>
      </el-form>
    </div>
  </div>

  <win-op :showSetTop=false :showMin=false :showMax=false :closeType="0"></win-op>
</template>



<script setup>
// eslint-disable-next-line no-unused-vars
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router' //用于路由跳转
import { md5 } from 'js-md5' //用于加密
import { useUserInfoStore } from '@/stores/UserInfoStore'
const showForm = ref(true)





const { proxy } = getCurrentInstance()
const formData = ref({}) //新的vue3格式  ref表示
const formDataRef = ref()
const errorMsg = ref(null)
const isLogin = ref(true)
const isClick = ref(false)
const showLoading = ref(false)
const userInfoStore = useUserInfoStore()
const router = useRouter()
const isloginByEmail = ref(true)

const changeOnType = () => {
  //以更为简洁的方式定义函数 属于ES6版本
  isLogin.value = !isLogin.value

  nextTick(() => {
    formDataRef.value.resetFields()
    formData.value = {}
    cleanVerify()
    changeCheckCode()
  }) //用于在登录注册切换时清空表单数据
  window.ipcRenderer.send('loginOrRegister', isLogin.value) //传参数到主进程
}

const changeOnLoginType = () => {
  isloginByEmail.value = !isloginByEmail.value
  nextTick(() => {
    formDataRef.value.resetFields()
    formData.value = {}
    cleanVerify()
    changeCheckCode()
  }) //用于在登录注册切换时清空表单数据
}

const cleanVerify = () => {
  errorMsg.value = null
}




const checkyourRepassword = (rule, value, callback) => {
  if (value != formData.value.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
}


const disableButton = ref(false)


const submitEmailCheckByRule = async () => {
  if (formData.value.email == null) {
    proxy.Message.error('邮箱不能为空')
    return
  } else {
    disableButton.value = true
    setTimeout(() => {
      disableButton.value = false
    }, 60000)

    //传到后端
    let result = await proxy.Request({
      url: proxy.Api.getEmailCode,
      showLoading: true,
      showerror: false,
      params: {
        email: formData.value.email,
        isLoginByEmail: isloginByEmail.value
      }
    })
    if (!result) {
      return
    }
    proxy.Message.success('已发送')
    localStorage.setItem('checkEmailCodeKey', result.data.checkEmailCodeKey)
  }
}

const submitByRule = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let result = null
    if (isloginByEmail.value == false && isLogin.value == true) {
      result = await proxy.Request({
        url: proxy.Api.loginForget,
        params: {
          email: formData.value.email,
          checkEmailCode: formData.value.emailCheck,
          checkEmailCodeKey: localStorage.getItem('checkEmailCodeKey'),
          checkCode: formData.value.checkcode,
          checkCodeKey: localStorage.getItem('checkCodeKey'),
        }
      })
      if (!result) {
        return;
      }
    } else {
      result = await proxy.Request({
        url: isLogin.value ? proxy.Api.login : proxy.Api.register,
        showLoading: isLogin.value ? false : true,
        showerror: false,
        params: {
          accountId: isLogin.value ? formData.value.accountId : formData.value.email,
          password: formData.value.password,
          checkCode: formData.value.checkcode,
          nickName: formData.value.nickname,
          checkCodeKey: localStorage.getItem('checkCodeKey'),
          checkEmailCodeKey: localStorage.getItem('checkEmailCodeKey'),
          checkEmailCode: formData.value.emailCheck
        },
        errorCallback: (response) => {
          changeCheckCode()
          showLoading.value = false
        }
      })
      if (!result) {
        return
      }
    }
    if (isLogin.value) {
      showLoading.value = true
      showForm.value = 
      userInfoStore.setInfo(result.data)
      localStorage.setItem('token', result.data.token)
      let email = null
      if (proxy.Verify.checkEmail(formData.value.accountId)) {
        email = formData.value.accountId
      }
      //发消息给主进程调整窗口大小
      window.ipcRenderer.send('openChat', {
        email,
        admin: result.data.admin,
        token: result.data.token,
        userId: result.data.userId,
        nickName: result.data.nickName,
        isDirect: false
      })
    } else {
      proxy.Message.success('注册成功')
      changeOnType()
    }




  })
}
const localUserList = ref([])

import { useGlobalInfoStore } from '@/stores/GlobalInfoStore'
const globalInfoStore = useGlobalInfoStore()


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

const onOpenChatCallBack = () => {
  window.ipcRenderer.on('openChatCallBack', (event) => {
    setTimeout(() => {
      showLoading.value = false
      router.push('/main')
    }, 400)
  })
}





onMounted(() => {

  init()
  window.ipcRenderer.send('loadLocalUser');

  window.ipcRenderer.on('loadLocalUserCallBack', (event, data) => {
    localUserList.value = data
  })

  onOpenChatCallBack()

})


onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('loadLocalUserCallBack')
  window.ipcRenderer.removeAllListeners('openChatCallBack')

})






const rules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  accountId: [
    { required: true, message: '请输入邮箱或账号', trigger: 'blur' },
  ],
  emailCheck: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/,
      message: '密码须是数字、大小写字母、特殊字符8~18位',
      trigger: 'blur'
    }
  ],
  repassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: checkyourRepassword, trigger: 'blur' }
  ],
  checkcode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})

//获取验证码--------------------------------------------------------------------------------------------------
const checkCodeUrl = ref() //从服务器拿到验证码图片，等会放在html框架上
const changeCheckCode = async () => {
  let result = await proxy.Request({
    //通过之前配置的代码碎片直接得到request请求代码块， request是一个函数 传入的参数是config 然后config是一个结构体 这里往结构体里面的url属性赋值
    url: proxy.Api.checkCode //Api已经放在全局 ，直接通过proxy调用Api.js相关代码
  })
  if (!result) {
    return
  }
  init()
  checkCodeUrl.value = result.data.checkCode
  localStorage.setItem('checkCodeKey', result.data.checkCodeKey) //这是验证码的token，之后发送验证码给服务器校验的时候需要通过token 在redis中找到正确的值 进行比对
}

changeCheckCode()
</script>

<!-- css样式控制 -->
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

  &.fade-enter-active {
    transition: opacity 2s;

  }

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
  padding: 0px, 15px, 29px, 15px;

  :deep(.el-input__wrapper) {
    box-shadow: none;
    border-radius: none;
  }

  .el-form-item {
    border-bottom: 1px solid #ddd;
  }
}

.email-panel {
  align-items: center;
  width: 100%;
  display: flex;

  .input {
    flex: 1;
  }

  .icon-down {
    margin-left: 3px;
    width: 16px;
    cursor: pointer;
    border: none;
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
  height: 40px;
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
  margin-top: 20px;
  width: 100%;
  background: #cfb0f7;
  height: 36px;
  font-size: 16px;

  &:hover {
    background: rgb(216, 191, 249);
    //动画对颜色进行过渡
    transition: all 0.5s;
  }
}

.bottom-links-container {
  display: flex;
  justify-content: space-between;
}

.bottom-link {
  margin-top: 15px;

  .a-link {
    color: #deb8f8;

    &:hover {
      color: rgb(168, 108, 248);
      transition: all 0.5s;
    }
  }
}
</style>
