<template>
  <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
    <el-form-item label="群名称" prop="groupName">
      <el-input v-model="formData.groupName" clearable placeholder="请输入分组名称"></el-input>
    </el-form-item>
    <el-form-item label="封面" prop="avatarFile">
      <AvatarUpload v-model="formData.avatarFile" ref="avatarUploadRef" @coverFile="saveCover">
      </AvatarUpload>
    </el-form-item>
    <el-form-item label="加入权限" prop="joinType">
      <el-radio-group v-model="formData.joinType">
        <el-radio :value="0">允许任何人加入</el-radio>
        <el-radio :value="1">需要管理员同意</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="群公告" prop="groupNotice">
      <el-input type="textarea" v-model="formData.groupNotice" placeholder="请输入群公告" clearable rows="5" maxlength="300"
        :show-word-limit="true" resize="none"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submit" style="margin-left: 15%;">
        {{ formData.groupId ? '修改群聊' : '创建群聊' }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
const { proxy } = getCurrentInstance()
const formData = ref({});
const formDataRef = ref();
const rules = {
  groupName: [{ required: true, message: '请输入群名称', trigger: 'blur' }],
  joinType: [{ required: true, message: '请选择加入权限', trigger: 'blur' }],
  avatarFile: [{ required: true, message: '请上传头像', trigger: 'blur' }],
};

import { useContactStateStore } from '@/stores//ContactStateStore'
const contactStateStore = useContactStateStore()
import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore();
const emit = defineEmits(['editBack']);
const submit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    params = Object.assign({}, formData.value); // 浅拷贝 formData.value 到 params
    avatarInfoStore.setForceReload(params.groupId, false)


    let result = await proxy.Request({
      url: proxy.Api.saveGroup,
      params
    });
    if (!result) {
      return;
    }
    if (formData.value.avatarCover) {
      saveAvatar(formData.value.avatarFile, formData.value.avatarCover);
    }
    if (params.groupId) {
      proxy.Message.success('修改成功');
      emit('editBack')
    } else {
      proxy.Message.success('创建成功');
      contactStateStore.setContactReload("BLANK");
    }
    formDataRef.value.resetFields();
    contactStateStore.setContactReload("MY")//通过组件管理刷新我创建的群聊列表



    if (params.groupId) {
      avatarInfoStore.setForceReload(params.groupId, true)
    }


  });
};

import { useAvatarInfoStore } from '../../stores/AvatarUpdateStore';
const avatarInfoStore = useAvatarInfoStore();

const saveAvatar = async (avatar_file, cover_file) => {
  const fileToArrayBuffer = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => resolve(reader.result); // 结果是ArrayBuffer
      reader.onerror = reject;
      reader.readAsArrayBuffer(file); // 读取为ArrayBuffer
    });
  };

  // 将avatarFile和coverFile都转换为文件流（ArrayBuffer）
  const [avatarArrayBuffer, coverArrayBuffer] = await Promise.all([
    fileToArrayBuffer(avatar_file),
    fileToArrayBuffer(cover_file),
  ]);
  // 现在你可以将这些ArrayBuffer通过IPC发送到主进程
  window.ipcRenderer.send('saveAvatar', {
    avatarStream: avatarArrayBuffer,
    coverStream: coverArrayBuffer,
    groupId: formData.value.groupId,
    type: 'group',
  });
}



//保存封面
const saveCover = ({ avatarFile, coverFile }) => {

  formData.value.avatarFile = avatarFile;
  formData.value.avatarCover = coverFile;

};

//从父组件拿信息
const show = (data) => {
  formDataRef.value.resetFields(); //重置表单
  formData.value = Object.assign({}, data); // 浅拷贝  将父组件传递的数据赋值给formData
  formData.value.avatarFile = data.groupId
};

defineExpose({
  show
})


</script>

<style lang="scss" scoped></style>
