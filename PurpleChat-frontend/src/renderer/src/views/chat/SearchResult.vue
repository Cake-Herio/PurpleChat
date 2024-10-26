<template>
    <div class="search-item">
        <Avatar :userId="data.contactId" ></Avatar>
        <div class="contact-info">
            <div class="contact-name" v-html="data.searchContactName"></div>
            <div class="last-message" v-html="data.searchLastMessage"></div>
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
            width: 180px;
            height: 15px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            font-size: 12px;
            color: #999999;
            margin-top: 5px;
        }
    }

    &:hover{
        background: #ededed ;
    }
}
</style>
