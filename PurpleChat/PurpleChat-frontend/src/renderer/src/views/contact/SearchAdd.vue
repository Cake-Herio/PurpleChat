<template>
  <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="400px"
    :showCancel="false" @close="dialogConfig.show = false">

    <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
      <!--input输入-->
      <el-form-item label="" prop="">
        <el-input type="textarea" :rows="5" clearable placeholder="输入申请信息，更容易被通过" v-model.trim="formData.applyInfo"
          resize="none" show-word-limit maxlength="100">
        </el-input>

      </el-form-item>
    </el-form>
  </Dialog>



</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
import Dialog from '@/components/Dialog.vue'
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()
const { proxy } = getCurrentInstance()

const formData = ref({});
const formDataRef = ref();
const rules = {
  title: [{ required: true, message: "请输入内容" }],
};

const dialogConfig = ref({
  show: false,
  title: "提交申请",
  buttons: [
    {
      type: "primary",
      text: "确定",
      click: (e) => {
        submitApply();
      },
    },
  ],
});


import { useContactStateStore } from '../../stores/ContactStateStore';
const contactStateStore = useContactStateStore();
const emit = defineEmits(['reload']); //定义emit方法
const submitApply = async () => {
  const { contactId, contactType, applyInfo } = formData.value;
  let result = await proxy.Request({
    url: proxy.Api.applyAdd,
    params: {
      contactId,
      contactType: contactType == 'USER' ? 0 : 1,
      applyInfo,
    }
  })
  if (!result) {
    return;
  }

  if (result.data == 0) {
    proxy.Message.success("添加成功")
  }
  else {
    proxy.Message.success("申请已提交")
  }

  dialogConfig.value.show = false; //关闭弹窗
  emit('reload'); //触发父组件的reload方法

  if (result.data == 0) {
    contactStateStore.setContactReload(contactType)
  }


};


const show = (data) => {
  dialogConfig.value.show = true;
  nextTick(() => {
    formDataRef.value.resetFields();     //重置表单
    formData.value = Object.assign({}, data); //将data赋值给formData data就是searchResult.value,由父组件传值
    if (data.contactType == 'USER') {
      formData.value.applyInfo = '我是' + userInfoStore.getInfo().nickName + '，请求添加您为联系人';
    } else {
      formData.value.applyInfo = '我是' + userInfoStore.getInfo().nickName + '，请求加入群聊【' + data.nickName + '】';
    }
  });
}; //显示弹窗


defineExpose({
  show,
}); //将方法暴露给外部使用

</script>

<style lang="scss" scoped></style>
