<!-- eslint-disable prettier/prettier -->
<!-- eslint-disable prettier/prettier -->
<!-- eslint-disable prettier/prettier -->

<template>
    <el-config-provider :locale="locale" :message="config">
    <router-view>
    </router-view>
  </el-config-provider>
</template>

<script setup>
  import { ElConfigProvider } from 'element-plus'
  import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { reactive } from 'vue';

  const locale = zhCn;

  // eslint-disable-next-line prettier/prettier
  const config = reactive({
    max: 1
  })

</script>

<style lang="less">
</style>
