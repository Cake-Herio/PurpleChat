<template>
    <ChatMessageShowTime :data="data"></ChatMessageShowTime>
    <div class="sys-message">
        {{ dataMessage.messageContent }}
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

import ChatMessageShowTime from "./ChatMessageShowTime.vue";
const props = defineProps({
    data: {
        type: Object,
        default: {}
    }
})

const dataMessage = computed(()=>{
    const data = {...props.data}
    switch(data.messageType){
        case 1:
            data.messageContent = '已经添加为好友，以下是打招呼的信息'
            break
        case 13:
            data.messageContent = '对方已同意，可以开始聊天啦'
    }
    
    return data
})





</script>

<style lang="scss" scoped>
.sys-message{
    text-align: center;;
    font-size: 12px;
    columns: #999999;
    margin-bottom:20px
}
</style>
