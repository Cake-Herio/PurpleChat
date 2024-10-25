<template>
  <div class="top-panel">
    <el-card>
      <el-form :model="searchForm" label-width="80px" label-position="right">
        <el-row>
          <el-col :span="5">
            <el-form-item label="群聊Id" label-width="55px">
              <el-input class="password-input" v-model="searchForm.groupId" clearable @keyup="loadDataList"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="5">
            <el-form-item label="群名称">
              <el-input class="password-input" v-model="searchForm.groupNameFuzzy" clearable @keyup="loadDataList"
                placeholder="支持模糊搜索">

              </el-input>
            </el-form-item>
          </el-col>

          <el-col :span="5">
            <el-form-item label="群主UID">
              <el-input class="password-input" v-model="searchForm.groupOwnerID" clearable @keyup="loadDataList"
                placeholder="支持模糊搜索">

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
        <AvatarBase :width="40" :userId="row.groupId" :partType="'avatar'"></AvatarBase>
      </template>

      <template #slotGroupName="{ row }">
        {{ row.groupName }}({{ row.groupId }})
      </template>

      <template #slotGroupOwnerNickName="{ row }">
        {{ row.groupOwnerName }}({{ row.groupOwnerId }})
      </template>

      <template #slotJoinType="{ row }">
        <div>{{ row.joinTpye == 0 ? '直接加入' : '管理员同意后加入' }}</div>
      </template>

      <template #slotStatus="{ row }">
        <span style="color:red" v-if="row.status == 0">已解散</span>
        <span style="color:green" v-else>正常</span>
      </template>


      <template #slotOperation="{ row }">
        <div class="row-op-panel">
          <a href="javascript:viod(0)" @click="dissolutionGroup(row)" v-if="row.status == 1">解散</a>
          <div v-else>无需操作</div>

        </div>
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


const column = [
  {
    label: '头像',
    prop: 'groupId',
    width: 85,
    scopedSlots: 'slotAvatar'
  },
  {
    label: '群名称',
    prop: 'groupName',
    scopedSlots: 'slotGroupName'
  },
  {
    label: '群主',
    prop: 'groupOwnerNickName',

    scopedSlots: 'slotGroupOwnerNickName'
  },
  {
    label: '群员',
    prop: 'memberCount',
    width: 60,
  },
  {
    label: '创建时间',
    prop: 'createTime',
    width: 200,
  },
  {
    label: '加入方式',
    prop: 'joinTpye',
    scopedSlots: 'slotJoinType'
  },
  {
    label: '状态',
    prop: 'status',
    width: 70,
    scopedSlots: 'slotStatus'
  },
  {
    label: '操作',
    prop: 'operation',
    width: 80,
    scopedSlots: 'slotOperation'
  },
]
const tableData = ref({})

const tableOptions = {}


const searchForm = ref({})

const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
  }
  Object.assign(params, searchForm.value)
  let result = await proxy.Request({
    url: proxy.Api.loadGroup,
    params
  })
  if (!result) {
    return;
  }
  Object.assign(tableData.value, result.data)
}

const dissolutionGroup = async (row) => {
  let result = await proxy.Request({
    url: proxy.Api.adminDissolutionGroup,
    params: {
      groupId: row.groupId
    }
  })
  if (!result) {
    return;
  }
  proxy.Message.success('解散成功')
  loadDataList()
}

</script>

<style lang="scss" scoped></style>
