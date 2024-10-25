<template>
  <div>
    <el-cascader
    :options="AreaData"
    v-model="modelValue.areaCode"
    @change="change"
    ref="areaSeletRef"
    clearable
    ></el-cascader>

  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

import AreaData from './AreaData'

const props = defineProps({
    modelValue: {
        type: Object,
        default:{}
    }
})

const emit = defineEmits(['update:modelValue'])
const areaSeletRef = ref()

//TODO 需要花时间想明白这里的代码含义
const change = (val) => {
    const areaData = {
        areaCode: [],
        areaName: []
    }
    const checkedNodes = areaSeletRef.value.getCheckedNodes()[0]
    if(!checkedNodes){
        emit('update:modelValue', areaData)
        return
    }
    
    const pathValues = checkedNodes.pathValues
    const pathLabels = checkedNodes.pathLabels
    areaData.areaCode = pathValues
    areaData.areaName = pathLabels
    emit('update:modelValue', areaData)



}


</script>

<style lang="scss" scoped>
</style>
