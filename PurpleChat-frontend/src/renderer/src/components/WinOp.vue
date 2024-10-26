<template>
    <div class="win-op no-drag">
        <div
            v-if="showSetTop"
            :class="['iconfont icon-top', isTop ? 'win-top' : '']"
            @click="setTop"
            :title="isTop ? '取消置顶' : '置顶'"
        ></div>

        <div
            v-if="showMin"
            class="iconfont icon-min"
            @click="setMin"
            title="最小化"
        ></div>

        <div
            v-if="showMax"
            :class="['iconfont icon-max', isMax ? 'win-top' : '']"
            @click="setMax"
            :title="isMax ? '还原' : '最大化'"
        ></div>

        <div
            v-if="showClose"
            class="iconfont icon-close"
            @click="close"
            title="关闭"
        ></div>



    </div>


</template>

<script setup>
import { ref, getCurrentInstance, reactive, nextTick, onMounted} from 'vue'
const { proxy } = getCurrentInstance()

const isMax = ref(false)
const isTop = ref(false)


const props = defineProps({
    showSetTop: {
        type: Boolean,
        default: true
    },
    showMax: {
        type: Boolean,
        default: true
    },
    showClose: {
        type: Boolean,
        default: true
    },
    showMin: {
        type: Boolean,
        default: true
    },

    closeType: {
        type: Number,
        default: 1
    } // 0 关闭 1 隐藏  登陆界面关闭则是关闭 其他界面关闭则是隐藏
})


onMounted(() => {
    isMax.value = false
    isTop.value = false
})
//关闭置顶这些操作都是由主进程来操作的，所以需要通过ipc来通信
const winOp = (action, data) => {
    window.ipcRenderer.send('winTitleOp', { action, data })
}


const emit = defineEmits('closeCallback')

const close = () => {
    emit('closeCallback')
    winOp('close', {closeType:props.closeType})
}
const setTop = () => {
    isTop.value = !isTop.value
    winOp('setTop', isTop.value)
}
const setMin = () => {
    winOp('minimize')
}
const setMax = () => {
    console.log(isMax.value)
    if(isMax.value){
        isMax.value = false
        winOp('unmaximize')
    }else{
        winOp('maximize')
        isMax.value = true
    }
}

defineExpose({
    setMax,
});


</script>

<style lang="scss" scoped>
.win-op {
    top: 0px;
    right: 0px;
    position: absolute;
    z-index: 1;
    overflow: hidden;
    border-radius: 0px 3px 0px 0px;

    .iconfont {
        float: left;
        font-size: 12px;
        text-align: center;
        display: flex;
        justify-content: center;
        cursor: pointer;
        height: 25px;
        align-items: center;
        padding: 0px 10px;

        &:hover {
            background: #ddd;
        }
    }

    .icon-close {
        &:hover {
            background: #fb7373;
            color: #fff;
        }
    }
}

.win-top {
    background: #ddd;
    color: #cfb0f7;
}
</style>

