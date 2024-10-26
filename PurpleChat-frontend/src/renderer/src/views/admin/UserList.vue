<template>
    <div class="top-panel">
        <el-card>
            <el-form :model="searchForm" label-width="70px" label-position="right">
                <el-row>
                    <el-col :span="5">
                        <el-form-item label="UID" label-width="40px">
                            <el-input class="password-input" v-model="searchForm.userId" clearable
                                @keyup="loadDataList"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="5">
                        <el-form-item label="昵称">
                            <el-input class="password-input" v-model="searchForm.nickNameFuzzy" clearable
                                @keyup="loadDataList" placeholder="支持模糊搜索">

                            </el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="4" :style="{ paddingLeft: '10px' }">
                        <el-button type="success" @click="loadDataList()">查询</el-button>
                    </el-col>
                </el-row>
            </el-form>
        </el-card>
    </div>

    <el-card class="table-data-card">
        <Table :columns="column" :fetch="loadDataList" :dataSource="tableData" :options="tableOptions">
            <template #slotAvatar="{ row }">
                <avatarBase :width="40" :userId="row.userId" :partType="'avatar'"></avatarBase>
            </template>

            <template #slotNickName="{ row }">
                {{  row.nickName }} 
                ({{ row.userId }})
                <span v-if="row.sex == 1" class="iconfont icon-man"></span>
                <span v-if="row.sex == 0" class="iconfont icon-woman"></span>
            </template>

            <template #slotStatus="{ row }">
                <span style="color:red" v-if="row.status == 0">禁用</span>
                <span style="color:green" v-else>启用</span>
            </template>

            <template #slotOnline="{ row }">
                <span style="color:green" v-if="row.onlineType == 1">在线</span>
                <span style="color:#8a8a8a" v-else>离线</span>
            </template>

            <template #slotOperation="{ row }">
                <el-dropdown placement="bottom-end" trigger="click" v-if="userInfo.userId != row.userId">
                    <span class="iconfont icon-more"></span>
                    <template #dropdown>
                        <el-dropdown-item @click="changeAccountStatus(row)">{{ row.status == 0? '启用' : '禁用' }}</el-dropdown-item>
                        <el-dropdown-item @click="forceOffLine(row)" v-if="row.onlineType == 1"> 强制下线 </el-dropdown-item>
                    </template>
                </el-dropdown>
                <div v-else>管理员</div>
            </template>
                
        </Table>
    </el-card>
</template>






<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const changeAccountStatus = (data) => {
    let status = data.status == 0 ? 1 : 0
    let info = status == 0 ? '禁用' : '启用'
    proxy.Confirm({
        message: `确认要将${data.nickName} ${info}吗？`,
        okfun: async() => {
            let result = await proxy.Request({
                url: proxy.Api.updateUserStatus,
                params: {
                    userId: data.userId,
                    status
                }
            })
            if (!result) {
                return
            }
            proxy.Message.success('操作成功')
            if(status == 0){
                let result = await proxy.Request({
                url: proxy.Api.forceOffLine,
                params: {
                    userId: data.userId
                }
            })
            if (!result) {
                return
            }
            }
            loadDataList()
        }
    })
}

const forceOffLine = (data) => {
    proxy.Confirm({
        message: `确认要将${data.nickName} 强制下线吗？`,
        okfun: async() => {
            let result = await proxy.Request({
                url: proxy.Api.forceOffLine,
                params: {
                    userId: data.userId
                }
            })
            if (!result) {
                return
            }

            proxy.Message.success('操作成功')
            loadDataList()
        }
    })
}


import {robot_UID} from '@/utils/Constants'
const loadDataList = async() => {
    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize,
    }
    Object.assign(params, searchForm.value)
    let result = await proxy.Request({
        url: proxy.Api.loadAdminAccount,
        params,
        showLoading: true
    })
    if (!result) {
        return;
    }
    //筛掉机器人
    const list = result.data.list.filter(item => item.userId !== robot_UID);
    Object.assign(tableData.value, result.data)

    tableData.value.list = list
}

const tableData = ref({})

const tableOptions = {}

const column = [
    {
        label: '头像',
        prop: 'userId',
        width: 85,
        scopedSlots: 'slotAvatar'
    },
    {
        label: '昵称',
        prop: 'nickName',
        width: 260,
        scopedSlots: 'slotNickName',
    },
    {
        label: '加入时间',
        prop: 'createTime',
        width: 210,
    },
    {
        label: '地区',
        prop: 'areaName',
        width: 150,
    }, {
        label: '用户状态',
        prop: 'status',
        width: 100,
        scopedSlots: 'slotStatus',
    },
    {
        label: '在线状态',
        prop: 'onlineType',
        width: 100,
        scopedSlots: 'slotOnline',
    },
    {
        label: '操作',
        prop: 'operation',
        width: 100,
        scopedSlots: 'slotOperation'
    },
]

const searchForm = ref({})

const userInfo = ref({})
const getUserInfo = async() => {
    let result = await proxy.Request({
        url: proxy.Api.getUserInfo
    })
    if (!result) {
        return;
    }
    userInfo.value = result.data
}








getUserInfo()
</script>

<style lang="scss" scoped>
.icon-man {
    color: #2cb6fe
}

.icon-woman {
    color: #fb7373;
}
</style>
