<template>
    <div class="avatar-upload">
        <div class="avatar-show">
            <template v-if="modelValue">
                <el-image v-if="preview" :src="localFile" fit="scale-down" ></el-image>
                <ShowLocalImage
                :fileId="props.modelValue"
                :partType="'avatar'"
                :width="40"
                :showCover="true"
                v-else ></ShowLocalImage>
            </template>
            <template v-else>
                <el-upload
                name="file"
                :show-file-list="false"
                accept=".jpg,.jpeg,.png,.gif,.bmp,.webp,.svg"
                :multiple="false"
                :http-request="uploadImage" >
                <span class="iconfont icon-add"></span>
                </el-upload>
            </template>
        </div>
        <div class="select-btn">
            <el-upload
                name="file"
                :show-file-list="false"
                accept=".jpg,.jpeg,.png,.gif,.bmp,.webp,.svg"
                :mutiple="false"
                :http-request="uploadImage" >
                <el-button size="small" type="primary">选择</el-button>
            </el-upload>
        </div>
    </div>
    
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed, onUnmounted , onMounted} from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
    modelValue: {
        type: [String, Object],
        default: null
    }
})

const uploadImage = async (file) => {
    file = file.file
    //上传文件逻辑 到主进程 并生成缩略图
    window.ipcRenderer.send('createCover', file.path)

}

const emit = defineEmits(["coverFile"]) 
import {useAvatarInfoStore} from "@/stores/AvatarUpdateStore"
const avatarInfoStore = useAvatarInfoStore()
import {useUserInfoStore} from "@/stores/UserInfoStore"
const userInfoStore = useUserInfoStore()
const userId = userInfoStore.getInfo().userId

const localFile = ref(null)
const createCoverBackListener = (event, {avatarStream, coverStream}) => {
    const coverBlob = new Blob([coverStream], { type: 'image/png' });
    const coverFile = new File([coverBlob], "thumbnail.jpg");
    let img = new FileReader();
    img.readAsDataURL(coverFile);
    img.onload = ({ target }) => {
        localFile.value = target.result;
    };

    const avatarBlob = new Blob([avatarStream], { type: 'image/png' });
    const avatarFile = new File([avatarBlob], "thumbnail.jpg");
    emit("coverFile", { avatarFile, coverFile }); 
};

onMounted(() => {
    window.ipcRenderer.on('createCoverBack', createCoverBackListener);
});

onUnmounted(() => {
    window.ipcRenderer.removeListener('createCoverBack', createCoverBackListener);
});




const preview = computed(() => {
    return props.modelValue instanceof File
})


</script>

<style lang="scss" scoped>
.avatar-upload{
    display: flex;
    justify-content: center;
    align-items: center;
    line-height:normal;
    .avatar-show{
        background: transparent;
        width: 60px;
        height: 60px;
        display: flex;
        align-content: center;
        overflow: hidden;
        position: relative;
        .icon-add{
            font-size: 30px;
            color: #b9b9b9;
            width: 60px;
            text-align: center;
            line-height: 60px;
        }
        img{
            width: 100%;
            height: 100%;
        }
        .op{
            position: absolute;
            color:#0e8aef;
            top:80px
        }
    }
    .select-btn{
        vertical-align: bottom;
        margin-left: 5px;
    }

}
</style>
