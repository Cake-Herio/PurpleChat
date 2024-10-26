<template>
  <div>
    
    <ShowLocalImage :fileId="data.messageId" :fileType="data.fileType" :partType="'chat'"  @updateStatus="updateStatusHandlder" v-model:status="data.status" :data="data"></ShowLocalImage>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
const emit = defineEmits(['loaded'])


const props = defineProps({
  data:{
    type: Object,
    default: {}
  }
})

const data = ref(props.data)

const updateStatusHandlder = (status) => {
  console.log("改变status", status)
  data.value.status = status
  // props.data.status = status
}








</script>

<style lang="scss" scoped>
</style>
