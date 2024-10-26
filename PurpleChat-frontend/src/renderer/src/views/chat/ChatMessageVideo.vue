<template>
    <div>
        <ShowLocalImage :fileId="data.messageId" :fileType="data.fileType" :partType="'chat'" :status="data.status" :showPlay="true" @updateStatus="updateStatusHandler" :data="data"></ShowLocalImage>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const props = defineProps({
    data: {
        type: Object,
        default: {}
    }
})

const updateStatusHandler = (status) => {
    props.data.status = status
} 

</script>

<style lang="scss" scoped></style>