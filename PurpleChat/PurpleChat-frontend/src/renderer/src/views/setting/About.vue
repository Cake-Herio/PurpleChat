<template>
  <ContentPanel>
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
      <el-form-item label="Tips">
        <div class="info">
          大家好，这是本人开发的第一个软件作品。其外观模仿的是微信，技术学习指导是罗老师。作为一个聊天软件，这款产品拥有即时通信，发送图片、视频、文件等多种功能。
          <br>&nbsp;&nbsp;&nbsp;&nbsp;在基本设置中，允许用户更改头像，昵称。在登陆界面中，当用户在本机进行过一次登陆后，一天时间内允许快速登录而无需频繁输入密码。
          在聊天界面中，多媒体文件的发送拥有及时查看发送进度，选择性下载等功能。您可以选择任意大小，各种类型的文件发送或接收，并且下载网速不做任何限制。
          <br>&nbsp;&nbsp;&nbsp;&nbsp;这是第一代产品，后续可能会进行迭代升级，产品会自动检测是否为最新版本，并为您提供最新版本的下载的地址。祝您使用愉快。
          <br>&nbsp;&nbsp;&nbsp;&nbsp;对该产品如有任何疑问，欢迎添加VX:17764979389与我联系。
          
        </div>

      </el-form-item>






      <el-form-item label="版本信息">
        <div class="version-info">
          <div>PurpleChat {{ config.version }}</div>
          <div>
            <el-button type="primary" @click="checkUpdate" style="margin-left: 70%;">检查更新</el-button>
          </div>
        </div>

      </el-form-item>
    </el-form>
  </ContentPanel>
  <Update ref="updateRef"></Update>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

import config from "../../../../../package.json"
import Update from "../Update.vue";
const updateRef = ref();
//检查更新
import { useUserInfoStore } from "@/stores/UserInfoStore"
const userInfoStore = useUserInfoStore()
const userId = userInfoStore.getInfo().userId
const checkUpdate = async () => {
  updateRef.value.checkUpdate();
}
const formData = ref({});
const formDataRef = ref();
const rules = {
  title: [{ required: true, message: "请输入内容" }],
};
</script>

<style lang="scss" scoped>
.version-info {
  text-align: center;
}

.info{
  
  font-family: 'Courier New', Courier, monospace;
  color:rgb(175, 128, 230);
  //加粗
  font-weight:bold;
  //首行缩进
  text-indent: 2em;
  text-align: left;
}
</style>
