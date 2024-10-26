<template>
  

  <div class="login-panel">
    <div class="title drag">EasyChat</div>
    <div v-if="showLoading" class="loading-panel">
      <img src="../assets/img/loading.gif" />
    </div>

    <div class="login-form" v-else>
      <div class="error-msg">{{ errorMsg }}</div>

      <el-form
        :model="formData"
        :rules="rules"
        ref="formDataRef"
        label-width="10px"
        @submit.prevent
      >
        <!--input输入-->
        <el-form-item prop="email" v-if="isloginByEmail || !isLogin">
          <el-input
            size="large"
            clearable
            placeholder="请输入邮箱"
            v-model.trim="formData.email"
            maxlength="30"
            @focus="cleanVerify"
          >
            <template #prefix>
              <span class="iconfont icon-email"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="userId" v-if="!isloginByEmail && isLogin">
          <el-input
            size="large"
            clearable
            placeholder="请输入账号"
            v-model.trim="formData.userId"
            maxlength="12"
            @focus="cleanVerify"
          >
            <template #prefix>
              <span class="iconfont icon-email"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="emailCheck" v-if="!isLogin">
          <div class="check-code-panel">
            <el-input
              size="large"
              clearable
              placeholder="请输入邮箱验证码"
              v-model.trim="formData.emailCheck"
              maxlength="30"
              @focus="cleanVerify"
            >
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>

            <el-button type="primary" @click="submitEmailCheckByRule" :disabled="disableButton">
              {{ isLogin ? '禁止' : '获取' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="nickname" v-if="!isLogin">
          <el-input
            size="large"
            clearable
            placeholder="请输入昵称"
            v-model.trim="formData.nickname"
            maxlength="15"
            @focus="cleanVerify"
          >
            <template #prefix>
              <span class="iconfont icon-user-nick"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            size="large"
            show-password
            placeholder="请输入密码"
            v-model.trim="formData.password"
            @focus="cleanVerify"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="repassword" v-if="!isLogin">
          <el-input
            size="large"
            show-password
            placeholder="请再次输入密码"
            v-model.trim="formData.repassword"
            @focus="cleanVerify"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="checkcode">
          <div class="check-code-panel">
            <el-input
              size="large"
              clearable
              placeholder="请输入验证码"
              v-model.trim="formData.checkcode"
              @focus="cleanVerify"
              >//表示绑定聚焦事件 ，点击则调用函数
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
            <span class="a-link">{{ isloginByEmail ? '账号登陆' : '邮箱登录' }}</span>
          </div>
        </div>
      </el-form>
    </div>
  </div>

  <win-op :showSetTop = false :showMin = false :showMax = false></win-op>
</template>



<script setup>
// eslint-disable-next-line no-unused-vars
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
import { useRouter } from 'vue-router' //用于路由跳转
import { md5 } from 'js-md5' //用于加密
import { useUesrInfoStore } from '@/stores/UserInfoStore'

const { proxy } = getCurrentInstance()
const formData = ref({}) //新的vue3格式  ref表示
const formDataRef = ref()
const errorMsg = ref(null)
const isLogin = ref(true)
const isClick = ref(false)
const showLoading = ref(false)
const userInfoStore = useUesrInfoStore()
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

const checkValue = (type, value, msg) => {
  if (proxy.Utils.isEmpty(value)) {
    errorMsg.value = msg
    return false
  }
  if (type && !proxy.Verify[type](value)) {
    errorMsg.value = msg
    return false
  }
  return true
}


const checkyourRepassword = (rule, value, callback) => {
  if (value != formData.value.password) {  
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
}

const checkUserId = (value) => {
  return /^U\d{11}$/.test(value);
}

const submit = () => {
  cleanVerify()

  if (!checkValue('checkEmail', formData.value.email, '请输入正确的邮箱')) {
    return
  }
  if (!isLogin.value && !checkValue(null, formData.value.nickname, '请输入昵称')) {
    return
  }
  if (
    !checkValue('checkPassword', formData.value.password, '密码只能是数字、字母、特殊字符8~18位')
  ) {
    return
  }
  if (!isLogin.value && checkRepassword(formData.value.password, formData.value.repassword)) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  if (!checkValue(null, formData.value.checkcode, '请输入验证码')) {
    return
  }
}

const disableButton = ref(false)
const submitEmailCheckByRule = async () => {
  if (formData.value.email == null) {
    proxy.Message.error('邮箱不能为空')
    return
  } else {
    if (disableButton.value) {
      proxy.Message.warning('请等一下再请求')
      return
    }

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
        email: formData.value.email
      }
    })
    if (!result) {
      return
    }
    localStorage.setItem('checkEmailCodeKey', result.data.checkEmailCodeKey)
  }
}

const submitByRule = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    let result = await proxy.Request({
      url: isLogin.value ? proxy.Api.login : proxy.Api.register,
      showLoading: isLogin.value ? false : true,
      showerror: false,
      params: {
        email: formData.value.email,
        userId: formData.value.userId,
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

    if (isLogin.value) {
      showLoading.value = true
      userInfoStore.setInfo(result.data)
      localStorage.setItem('token', result.data.token)
      setTimeout(function () {
        router.push('/main')
        window.ipcRenderer.send('openChat', {
          email: result.data.email,
          admin: result.data.admin, 
          token: result.data.token,
          userId: result.data.userId,
          nickName: result.data.nickName
        })
      }, 200) //由于这里可能会出现主进程快于渲染进程的情况，然后会让加载界面闪一下，所以这里设置一个定时器任务
    } else {
      proxy.Message.success('注册成功')
      changeOntype()
    }
  })
}

const rules = reactive({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  userId: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { validator: (rule, value) => checkUserId(value) ? true : '请输入以U开头，后跟11位数字的账号', trigger: 'blur' }
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
      message: '密码只能是数字、字母、特殊字符8~18位',
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
  checkCodeUrl.value = result.data.checkCode
  localStorage.setItem('checkCodeKey', result.data.checkCodeKey) //这是验证码的token，之后发送验证码给服务器校验的时候需要通过token 在redis中找到正确的值 进行比对
}

changeCheckCode()
</script>

//css样式控制
<style lang="scss" scoped>
.email-select {
  width: 250px;
}
.loading-panel {
  height: calc(100vh - 32px);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  img {
    width: 300px;
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
  margin-top: 20px;
  width: 100%;
  background: #07c160;
  height: 36px;
  font-size: 16px;
}

.bottom-links-container {
  display: flex;
  justify-content: space-between;
}

.bottom-link {
  margin-top: 15px;
}
</style>

