<template>
  <ContentPanel v-loading="copying" element-loading-text="正在复制文件">
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
      <!--input输入-->
      <el-form-item label="缓存路径" prop="" class="file-manage">
        <div class="file-input" :title="formData.sysSetting">{{ formData.sysSetting }}</div>
        <div class="tips">文件的默认保存位置</div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="changeFolder">更改</el-button>
        <el-button type="primary" @click="openLocalFolder">打开文件夹</el-button>
      </el-form-item>


      

    </el-form>


  </ContentPanel>
</template>




<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const formData = ref({});
const formDataRef = ref();
const rules = {
  title: [{ required: true, message: "请输入内容" }],
};
const getSetting = () => {
  window.ipcRenderer.send("getSysSetting");
}

//按钮函数
const changeFolder = () => {
  window.ipcRenderer.send("changeLocalFolder");
}

onMounted(() => {
  //获取文件缓存
  getSetting()
  window.ipcRenderer.on("getSysSettingCallBack", (event, sysSetting) => {
    copying.value = false
    sysSetting = JSON.parse(sysSetting);
    formData.value = {
      sysSetting: sysSetting.localFileFolder,
    };
  });

  //监听路径拷贝
  window.ipcRenderer.on("copyingCallBack", (event, data) => {
    copying.value = true
  });

})


onUnmounted(() => {
  window.ipcRenderer.removeAllListeners("getSysSettingCallBack");
})



const openLocalFolder = () => {
  window.ipcRenderer.send("openLocalFolder");
}


const copying = ref(false)
</script>

<style lang="scss" scoped>
.file-manage {
  :deep(.el-form-item__content) {
    display: block;
    
  }
  .file-input {
    padding: 0px 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 16px;
    border-bottom: 1px solid #ddd;
  }
  .tips {
    color: #888888;
    font-size: 13px;
    // 居左
    text-align: left;
  }
}

</style>

