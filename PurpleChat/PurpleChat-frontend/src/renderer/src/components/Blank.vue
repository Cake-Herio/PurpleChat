<template>
    <div class="top drag" v-if="msg=='chat'"></div>
    <div class="iconfont icon-chat"></div>
</template>

<script setup>
const props = defineProps({
    msg:{
        type: String,
    }
})
</script>

<style lang="scss" scoped>
.top{
    height: 60px;
}
.iconfont{
    display: flex;
    align-items: center;
    justify-content: center;
    font-size:100px;
    color: #e1bcfa;
    height:calc(100vh - 122px);
}
</style>
