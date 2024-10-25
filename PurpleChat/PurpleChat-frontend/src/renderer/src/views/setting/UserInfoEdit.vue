<template>
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
        <el-form-item label="头像" prop="avatarFile">
            <AvatarUpload v-model="formData.avatarFile" @coverFile="saveCover">
            </AvatarUpload>
        </el-form-item>

        <el-form-item label="昵称" prop="nickName">
            <el-input v-model="formData.nickName" placeholder="请输入昵称" clearable maxlength="150"></el-input>
        </el-form-item>

        <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="formData.sex">
                <el-radio :value="0">女</el-radio>
                <el-radio :value="1">男</el-radio>
            </el-radio-group>
        </el-form-item>

        <el-form-item label="朋友权限" prop="joinType">
            <el-switch v-model="formData.joinType" :active-value="1" :inactive-value="0"></el-switch>
            <div class="info">加我为好友时需要验证</div>
        </el-form-item>

        <el-form-item label="地区" prop="area">
            <AreaSelect v-model="formData.area"></AreaSelect>
        </el-form-item>

        <el-form-item label="个性签名" prop="personalSignature">
            <el-input v-model.trim="formData.personalSignature" rows="6" type="textarea" resize="none"
                placeholder="请输入个性签名" clearable maxlength="30" :show-word-limit="true"></el-input>
        </el-form-item>

        <el-form-item>
            <el-button type="primary" @click="saveUserInfo" style="margin-left: 25%;">保存信息</el-button>
            <el-button link @click="cancel">取消</el-button>
        </el-form-item>

    </el-form>
</template>

<script setup>
import AreaSelect from "@/components/AreaSelect.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const props = defineProps({
    data: {
        type: Object,
        default: () => ({})
    }
});

const formData = reactive({
    ...props.data,
    avatarFile: props.data.userId,
    area: {
        areaCode: props.data.areaCode ? props.data.areaCode.split(",") : [],
        areaName: props.data.areaName ? props.data.areaName.split(",") : []
    }
});

const formDataRef = ref();
const rules = {
    avatarFile: [{ required: true, message: "请上传头像", trigger: "blur" }],
    nickName: [{ required: true, message: "请输入昵称", trigger: "blur" }],
    sex: [{ required: true, message: "请选择性别", trigger: "blur" }],
    joinType: [{ required: true, message: "请选择朋友权限", trigger: "blur" }],
};

const saveCover = ({ avatarFile, coverFile }) => {
    formData.avatarFile = avatarFile;
    formData.coverFile = coverFile;
};

import { useUserInfoStore } from "@/stores/UserInfoStore";
const userInfoStore = useUserInfoStore();

const saveAvatar = async(avatar_file, cover_file) => {
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
    });
}

import { useUpdateByTimeStampStore } from '@/stores/UpdateByTimeStampStore';
const updateByTimeStampStore = useUpdateByTimeStampStore();


import { useAvatarInfoStore } from "@/stores/AvatarUpdateStore";
const avatarInfoStore = useAvatarInfoStore();

import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore()
const emit = defineEmits(['editBack']);
const saveUserInfo = async () => {
    //头像相关
    avatarInfoStore.setForceReload(userInfoStore.getInfo().userId, false);
    updateByTimeStampStore.clearTimeStamp()
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {};
        Object.assign(params, formData);

        params.areaName = '';
        params.areaCode = '';

        if (params.area) {
            params.areaCode = params.area.areaCode.join(",");
            params.areaName = params.area.areaName.join(",");
            delete params.area;
        }

        let result = await proxy.Request({
            url: proxy.Api.saveUserInfo,
            params,
        });
        if (!result) {
            return;
        }


        proxy.Message.success("保存成功");
        userInfoStore.setInfo(result.data);
        avatarInfoStore.setForceReload(userInfoStore.getInfo().userId, true);


        if(formData.avatarFile&&formData.coverFile)
            saveAvatar(formData.avatarFile, formData.coverFile);
        updateByTimeStampStore.updateTimestamp() 

        //更新sys数据库中的昵称信息
        window.ipcRenderer.send('updateUserSetting', {
            nickName: result.data.nickName
        });
        emit('editBack');
    });
};




const cancel = () => {
    emit('editBack');
};
</script>

<style lang="scss" scoped>
.info {
    margin-left: 5px;
    color: #949494;
    font-size: 12px;
}
</style>
