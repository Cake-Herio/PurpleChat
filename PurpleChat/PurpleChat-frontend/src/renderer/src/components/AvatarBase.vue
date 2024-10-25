<template>
    <div class="user-avatar">
        <ShowLocalImage :width="width" :fileId="userId" :partType="'avatar'"
            @click="showDetailHandler" :isLogin="isLogin"
            :style="{ borderRadius: borderRadius + 'px', width: width + 'px', height: width + 'px' ,}"></ShowLocalImage>
    </div>
</template>


<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed, onMounted } from 'vue'
const { proxy } = getCurrentInstance()

import { useAvatarInfoStore } from '@/stores/AvatarUpdateStore';
const avatarInfoStore = useAvatarInfoStore();
import { useUpdateByTimeStampStore } from '@/stores/UpdateByTimeStampStore';
const updateByTimeStampStore = useUpdateByTimeStampStore();
const props = defineProps({
    userId: {
        type: String
    },
    width: {
        type: Number,
        default: 170
    },
    borderRadius: {
        type: Number,
        default: 3
    },
    showDetail: {
        type: Boolean,
        default: false
    },
    forceGet: {
        type: Boolean,
        default: false
    },
    isLogin: {
        type: Boolean,
        default: false
    }   
})

const showDetailHandler = () => {
    console.log("showDetailHandler1")
    if (!props.showDetail) {
        return
    }
    console.log("showDetailHandler")
    //传给主进程
    window.ipcRenderer.send("newWindow", {
        windowId: "mediaDetail",
        title: "媒体详情",
        path: "/showMediaDetail",
        data: {
            currentFileId: props.userId,
            fileList: [
                {
                    fileId: props.userId,
                    fileType: 0,
                    partType: "avatar",
                    status: 1,
                    forceGet: true
                }
            ]
        }
    })
}


</script>

<style lang="scss" scoped>
.user-avatar {
    overflow: hidden;
    cursor: pointer;
    align-items: center;
    justify-content: center;
}
</style>
