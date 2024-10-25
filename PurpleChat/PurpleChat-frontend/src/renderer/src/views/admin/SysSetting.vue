<template>
  <div class="form-panel">
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="160px" @submit.prevent>
      <el-form-item label="最多可创建群聊数" prop="maxGroupCount">
        <el-input clearable placeholder="请输入每人最多可创建群聊数" v-model.number="formData.maxGroupCount" />
      </el-form-item>

      <el-form-item label="群聊最大成员数" prop="maxGroupMemberCount">
        <el-input clearable placeholder="请输入每个群聊最大成员数" v-model.number="formData.maxGroupMemberCount" />
      </el-form-item>

      <!--input输入-->
      <el-form-item label="图片大小" prop="maxImageSize">
        <el-input clearable placeholder="请输入允许上传的图片大小" v-model.number="formData.maxImageSize">
          <template #append>MB</template>
        </el-input>
      </el-form-item>

      <!--textarea输入-->
      <el-form-item label="视频大小" prop="maxVideoSize">
        <el-input clearable placeholder="请输入允许上传的视频大小" v-model.number="formData.maxVideoSize">
          <template #append>MB</template>
        </el-input>
      </el-form-item>

      <el-form-item label="其他文件大小" prop="maxFileSize">
        <el-input clearable placeholder="请输入允许上传的文件大小" v-model.number="formData.maxFileSize">
          <template #append>MB</template>
        </el-input>
      </el-form-item>

      <el-form-item label="机器人昵称" prop="robotNickName">
        <el-input clearable placeholder="请输入机器人昵称" v-model.trim="formData.robotNickName" maxlength="20" />
      </el-form-item>

      <!-- 下拉框 -->
      <el-form-item label="机器人头像" prop="robotFile">
        <AvatarUpload v-model="formData.robotCover" @coverFile="saveCover"></AvatarUpload>
      </el-form-item>

      <el-form-item label="欢迎消息" prop="robotWelcome">
        <el-input clearable placeholder="请输入新用户注册机器人欢迎信息" v-model="formData.robotWelcome" type="textarea" rows="5"
          maxlength="300" :show-word-limit="true" resize="none">
        </el-input>
      </el-form-item>

      <el-form-item label="">
        <el-button type="primary" @click="saveSysSetting">保存设置</el-button>
      </el-form-item>
    </el-form>

  </div>
</template>

<script setup>
import AvatarUpload from "@/components/AvatarUpload.vue";
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const formData = ref({})
const formDataRef = ref(null)
const rules = {
  maxGroupCount: [
    { required: true, message: "请输入每人最多可创建群聊数", trigger: "blur" },
    { type: "number", message: "请输入数字", trigger: "blur" }
  ],
  maxGroupMemberCount: [
    { required: true, message: "请输入每个群聊最大成员数", trigger: "blur" },
    { type: "number", message: "请输入数字", trigger: "blur" }
  ],
  maxImageSize: [
    { required: true, message: "请输入允许上传的图片大小", trigger: "blur" },
    { type: "number", message: "请输入数字", trigger: "blur" }
  ],
  maxVideoSize: [
    { required: true, message: "请输入允许上传的视频大小", trigger: "blur" },
    { type: "number", message: "请输入数字", trigger: "blur" }
  ],
  maxFileSize: [
    { required: true, message: "请输入允许上传的文件大小", trigger: "blur" },
    { type: "number", message: "请输入数字", trigger: "blur" }
  ],
  robotNickName: [
    { required: true, message: "请输入机器人昵称", trigger: "blur" },
    { max: 20, message: "最多输入20个字符", trigger: "blur" }
  ],
  // robotFile: [
  //   { required: true, message: "请上传机器人头像", trigger: "blur" }
  // ],
  robotWelcome: [
    { required: true, message: "请输入新用户注册机器人欢迎信息", trigger: "blur" },
    { max: 300, message: "最多输入300个字符", trigger: "blur" }
  ]
}

const saveCover = ({ avatarFile, coverFile }) => {
  formData.value.robotFile = avatarFile;
  formData.value.robotCover = coverFile;
};





const getSysSetting = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getSysSetting4Admin
  });
  if (!result) {
    return;
  }
  formData.value = result.data
  formData.value.robotCover = result.data.robotUid ? result.data.robotUid : null;
}

const saveSysSetting = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    params = Object.assign({}, formData.value); // 浅拷贝 formData.value 到 params
    let result = await proxy.Request({
      url: proxy.Api.saveSysSetting,
      params
    });
    if (!result) {
      return;
    }
    proxy.Message.success('保存成功');
  });
}





getSysSetting()
</script>

<style lang="scss" scoped>
.form-panel {
  width: 500px;
}
</style>
