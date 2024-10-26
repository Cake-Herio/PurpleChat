<template>
    <div class="top-panel">
        <el-card>
            <el-form :model="searchForm" label-width="70px" label-position="right">
                <el-row>
                    <el-col :span="5">
                        <el-form-item label="靓号" label-width="40px">
                            <el-input class="password-input" v-model="searchForm.userId" clearable
                                @keyup="loadDataList"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="5">
                        <el-form-item label="邮箱">
                            <el-input class="password-input" v-model="searchForm.emailFuzzy" clearable
                                @keyup="loadDataList" placeholder="支持模糊搜索">

                            </el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="14" :style="{ paddingLeft: '10px' }">
                        <el-button type="success" @click="loadDataList()">查询</el-button>
                        <el-button type="primary" @click="editAccount()">新增靓号</el-button>

                    </el-col>
                </el-row>
            </el-form>
        </el-card>
    </div>

    <el-card class="table-data-card">
        <Table :columns="column" :fetch="loadDataList" :dataSource="tableData" :options="tableOptions">
            <template #slotEmail="{ row }">
                {{ row.email }}
            </template>

            <template #slotBeautyAccount="{ row }">
                {{ row.userId }}
            </template>

            <template #slotStatus="{ row }">
                <span style="color:red" v-if="row.status == 0">未使用</span>
                <span style="color:green" v-else>已使用</span>
            </template>


            <template #slotOperation="{ row }" >
                <el-dropdown placement="bottom-end" trigger="click" v-if="userInfo.userId != row.userId&&row.status == 0" >
                    <span class="iconfont icon-more"></span>
                    <template #dropdown >
                        <el-dropdown-item @click="delBeautyCount(row)" >删除</el-dropdown-item>
                    </template>
                </el-dropdown>

            </template>

        </Table>
    </el-card>
    <beautyAccountEdit ref="beautyAcountEditRef" @reload="loadDataList()"></beautyAccountEdit>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
import BeautyAccountEdit from "./BeautyAccountEdit.vue";
const tableData = ref({
})
const tableOptions = {}
const column = [
    {
        label: '邮箱',
        prop: 'email',
        scopedSlots: 'slotEmail'
    },
    {
        label: '靓号',
        prop: 'userId',
        scopedSlots: 'slotBeautyAccount'
    },
    {
        label: '状态',
        prop: 'status',
        scopedSlots: 'slotStatus'
    },
    {
        label: '操作',
        prop: 'operation',
        scopedSlots: 'slotOperation'
    },
]

const loadDataList = async () => {
    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize,
    }
    Object.assign(params, searchForm.value)
    let result = await proxy.Request({
        url: proxy.Api.loadBeautyAccount,
        params
    })
    if (!result) {
        return;
    }
    Object.assign(tableData.value, result.data)
}
const beautyAcountEditRef = ref()
const searchForm = ref({})
const editAccount = () => {
    beautyAcountEditRef.value.showEdit()
}
const userInfo = ref({})
const getUserInfo = async () => {
    let result = await proxy.Request({
        url: proxy.Api.getUserInfo
    })
    if (!result) {
        return;
    }
    userInfo.value = result.data
}

const delBeautyCount = async (row) => {
    let result = await proxy.Request({
        url: proxy.Api.delBeautyAccount,
        params:{
            userId: row.userId
        }
    })
    if (!result) {
        return;
    }
    proxy.Message.success('删除成功')
    loadDataList()
}


getUserInfo()

</script>

<style lang="scss" scoped></style>
