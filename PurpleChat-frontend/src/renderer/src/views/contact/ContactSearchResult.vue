<template>
    <div class="search-item">
        <Avatar :userId="data.contactId" :showDetail="false" :forceGet="true"></Avatar>
        <div class="contact-info">
            <div class="contact-name" v-html="data.searchContactName"></div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
const props = defineProps({
    data:{
        type: Object,
        default: {}
    }
})
</script>

<style lang="scss" scoped>
.search-item{
    align-items: center;
    padding: 10px;
    position: relative;
    display: flex;
    border-bottom: 1px solid #ddd;
    background: #f2f2f1;
    .contact-info {
        margin-left: 10px;
        :deep(.highlight) {
            color: red;
        }
        .last-message{
            color: #999999;
        }
    }

    &:hover{
        background: #ededed ;
    }
}
</style>
