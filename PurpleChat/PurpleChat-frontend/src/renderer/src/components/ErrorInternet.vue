<template>
    <div class="error-panel">
        <div class="icon-error">!</div>
        当前网络不可用,重新连接中...
    </div>
</template>

<script setup>

</script>

<style lang="scss" scoped>
.error-panel{
    background: #f2f2f1;
    display: flex;
    font-size: smaller;
    justify-content: center;
    align-items: center;
    color: #666;
    text-align: center;
    padding: 10px;
    box-sizing: border-box;
    background: rgb(245, 218, 218);

    .icon-error{
        content: "!";
        display: inline-block;
        margin-right: 15px;
        padding: 0% 2.5%;
        background-color: rgb(232, 94, 94);
        border-radius: 50%;
        color: white;
        font-size: 14px;
        font-weight: bold;

    }

}
</style>
