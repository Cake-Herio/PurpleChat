<template>
  <div class="top-panel">
    <el-card>
      <el-form :model="searchForm" label-width="70px" label-position="right">
        <el-row>
          <el-col :span="9">
            <el-form-item label="发布日期" label-width="70px">
              <el-date-picker v-model="searchForm.createTimeRange" type="daterange" range-separator="至"
                start-placeholder="开始日期" end-placeholder="结束日期" clearable @change="loadDataList">
              </el-date-picker>
            </el-form-item>
          </el-col>

          <el-col :span="14" :style="{ paddingLeft: '10px' }">
            <el-button type="success" @click="loadDataList()">查询</el-button>
            <el-button type="primary" @click="showEdit()">发布版本</el-button>
          </el-col>

        </el-row>
      </el-form>
    </el-card>
  </div>

  <el-card class="table-data-card">
    <Table :columns="columns" :fetch="loadDataList" :dataSource="tableData" :options="tableOptions">
      <template #slotUpdateDesc="{ row }">
        <div v-for="(item, num) in row.updateDescArray" :key="item">
          <div class="num">{{ num + 1 }}.</div>
          {{ item }}
        </div>
      </template>

      <template #fileTypeSlot="{ row }">
        <div v-if="row.fileType == 0">本地文件</div>
        <div v-if="row.fileType == 1">{{ row.outerLink }}</div>
      </template>

      <template #slotStatus="{ row }">
        <div style="color: #f56c6c" v-if="row.status == 0">未发布</div>
        <div style="color: #f7ba2a" v-if="row.status == 1">灰度发布</div>
        <div style="color: #529b2e" v-if="row.status == 2">全网发布</div>
      </template>

      <template #slotOperation="{ row }">
        <el-dropdown placement="bottom-end" trigger="click">
          <span class="iconfont icon-more"></span>
          <template #dropdown>
            <el-dropdown-item @click="showEdit(row)" v-if="row.status == 0">修改</el-dropdown-item>
            <el-dropdown-item @click="updatePost(row)">发布</el-dropdown-item>
            <el-dropdown-item @click="del(row)" v-if="row.status == 0">删除</el-dropdown-item>
          </template>
        </el-dropdown>
      </template>
    </Table>
  </el-card>
  <UpdateEdit ref="updateEditRef" @reload="loadDataList()"></UpdateEdit>
  <UpdatePost ref="updatePostRef" @reload="loadDataList()"></UpdatePost>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
import moment from 'moment';
const router = useRouter()
import UpdateEdit from "./UpdateEdit.vue";
import UpdatePost from "./UpdatePost.vue";

const columns = [
  {
    label: '版本',
    prop: 'version',
    width: 120
  },
  {
    label: '更新内容',
    prop: 'updateDesc',
    scopedSlots: 'slotUpdateDesc',
    width: 200
  },
  {
    label: '发布时间',
    prop: 'createTime',
    width: 180
  },
  {
    label: '文件类型',
    prop: 'fileType',
    scopedSlots: 'fileTypeSlot'
  },
  {
    label: '状态',
    prop: 'status',
    scopedSlots: 'slotStatus',
    width: 80
  },
  {
    label: '操作',
    prop: 'operation',
    scopedSlots: 'slotOperation',
    width: 80
  },

];
const tableData = ref({})

const tableOptions = {}
const searchForm = ref({})
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
  }
  if (searchForm.value.createTimeRange) {
    params.createTimeStart = moment(searchForm.value.createTimeRange[0]).format('YYYY-MM-DD')
    params.createTimeEnd = moment(searchForm.value.createTimeRange[1]).format('YYYY-MM-DD')
  }
  delete params.createTimeRange
  Object.assign(params, searchForm.value)

  let result = await proxy.Request({
    url: proxy.Api.loadUpdateDataList,
    params
  })
  if (!result) {
    return;
  }

  result.data.list.forEach(item => {
    // 替换时分秒中的短横线为冒号
    item.createTime = item.createTime ? item.createTime.replace(/(\d{2})-(\d{2})-(\d{2})$/, '$1:$2:$3') : '';

  })



  Object.assign(tableData.value, result.data)
}

const updateEditRef = ref()
const showEdit = (data) => {
  updateEditRef.value.showEdit(data)
}

const updatePostRef = ref()
const updatePost = (data) => {
  updatePostRef.value.showEdit(data)
}

const del = async (data) => {
  proxy.Confirm({
    message: `确认要删除版本${data.version}吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delUpdate,
        params: {
          id: data.id
        }
      })
      if (!result) {
        return
      }
      proxy.Message.success('删除成功')
      loadDataList()
    }
  })
}


</script>

<style lang="scss" scoped>
.num {
  //加重
  font-weight: bold;
  //不换行
  white-space: nowrap;
  display: inline-block;
}
</style>
