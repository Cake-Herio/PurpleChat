<template>
    <div class="show-info">
        <div class="user-info">
            <UserBaseInfo :userInfo="userInfo" :showArea="true" ></UserBaseInfo>
            <div class="more-op">
                <el-dropdown placement="bottom-end" trigger="click">
                    <span class="el-dropdown-link">
                        <div class="iconfont icon-more"></div>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item @click="changePart(1)">修改个人信息</el-dropdown-item>
                            <el-dropdown-item @click="changePart(2)">修改密码</el-dropdown-item>
                            <el-dropdown-item @click="changePart(3)">退出登录</el-dropdown-item>

                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </div>

        <div class="part-item">
            <div class="part-title">朋友权限</div>
            <div class="part-content">
                {{ userInfo.joinType == 0 ? "允许任何人添加" : "需要验证" }}
            </div>
        </div>
        <div class="part-item">

            <div class="part-title">个性签名</div>
            <div class="part-content">
                {{ userInfo.personalSignature == 'null' ||userInfo.personalSignature == ''? '-' :userInfo.personalSignature  }}    
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
import { useUpdateByTimeStampStore } from '@/stores/UpdateByTimeStampStore';
const updateByTimeStampStore = useUpdateByTimeStampStore();
const emit = defineEmits(['changePartOne', 'changePartTwo'])

const changePart = (type) => {
    if (type == 1) {
        emit('changePartOne')
    } else if (type == 2) {
        emit('changePartTwo')
    } else {
        //退出登录
        proxy.Confirm({
            message: '确定要退出登录吗？',
            okfun: async () => {
                let result = await proxy.Request({
                    url: proxy.Api.logout
                })
                if (!result) {
                    return;
                }
                window.ipcRenderer.send("relogin")
            }
        })
    }
}



const props = defineProps({
    userInfo: {
        type: Object,
        default: {}
    }
})
import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore();
import {useUserInfoStore} from "@/stores/UserInfoStore"
const userInfoStore = useUserInfoStore()
const onSaveAvatarCallBack = () => {
    window.ipcRenderer.on('saveAvatarCallBack', (event) => {
        dynamicStore.setData("refresh" + userInfoStore.getInfo().userId, new Date().getTime())
        // setTimeout(() => {
        //     dynamicStore.setData("refresh" + userInfoStore.getInfo().userId, new Date().getTime())
        // }, 1000)
    });
}

onMounted(() => {
    onSaveAvatarCallBack()
})

</script>

<style lang="scss" scoped>
.show-info {
    .user-info {
        position: relative;

        .more-op {
            position: absolute;
            right: 0px;
            top: 20px;

            .icon-more {
                color: #9e9e9e;

                &:hover {
                    background: #dddddd;
                }
            }
        }
    }

    .part-item {
        display: flex;
        border-bottom: 1px solid #eaeaea;
        padding: 20px 0px;

        .part-title {
            width: 60px;
            color: #9e9e9e;
        }

        .part-content {
            flex: 1;
            margin-left: 15px;
            color: #161616;
        }
    }

    .logout {
        text-align: center;
        margin-top: 20px;
    }
}
</style>
