<template>
  <!-- 对当前页面赋予无限滚动的功能 当下滑到最下面时自动加载 -->
  <ContentPanel :showTopBorder="true" :infinite-scroll-immediate="false" v-infinite-scroll="loadApply">

    <div class="no-data-msg" v-if="applyList.length == 0">——暂无数据——</div>


    <div v-for="item in applyList" :key="item.contactId" v-else>



      <div class="apply-item">
        <AvatarBase :width="50" :userId="item.applyUserId" :forceGet="true"></AvatarBase>
        <div class="contact-info">
          <div class="nick-name">{{ item.contactType == 0 ? item.contactName : item.contactName }}</div>
          <div class="apply-info">
            <div v-if="item.contactType == 1" style="display: flex;">
              <AvatarBase :width="17" :userId="item.applyUserId" :forceGet="true" style="margin-right: 2px">
              </AvatarBase>：
            </div>{{ item.applyInfo }}
          </div>
        </div>
        <div class="op-btn">
          <!-- 只展示状态为初始时的列表 -->
          <div v-if="item.status == 0">
            <el-dropdown placement="bottom-end" trigger="click">
              <span class="el-dropdown-link">
                <el-button type="primary">接受</el-button>
              </span>
              <template #dropdown>
                <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 1)">同意</el-dropdown-item>
                <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 2)">拒绝</el-dropdown-item>
                <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 3)">拉黑</el-dropdown-item>
              </template>
            </el-dropdown>
          </div>
          <div v-else>
            <div class="result-name" v-if="item.status == 1">已同意</div>
            <div class="result-name" v-if="item.status == 2">已拒绝</div>
            <div class="result-name" v-if="item.status == 3">已拉黑</div>
          </div>





        </div>
      </div>
    </div>
  </ContentPanel>
</template>

<script setup>
import { ref, getCurrentInstance, watch, onMounted, onUnmounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

let pageNo = 0;
let pageTotal = 1;

// 每次去请求后端数据只需要请求一页的数据 因此需要一个变量来记录当前请求的页数
const applyList = ref([]);
const loadApply = async () => {
  pageNo++;
  if (pageNo > pageTotal) {
    return;
  }

  let result = await proxy.Request({
    url: proxy.Api.loadApply,
    params: {
      pageNo: pageNo,
    },
  });
  if (!result) {
    console.error('Failed to load apply data');
    return;
  }

  // 当请求第一页的时候需要先清空全部数据
  if (result.data.pageNo == 1) {
    applyList.value = [];
  }
  applyList.value = applyList.value.concat(result.data.list);
  pageNo = result.data.pageNo;
};

// 监听新朋友数量改变
import { useContactStateStore } from '@/stores/ContactStateStore';
const contactStateStore = useContactStateStore();




const dealWithApply = (applyId, contactType, status) => {

  // 弹是否确认的窗口
  proxy.Confirm({
    title: '提示',
    message: '是否确认操作',
    okfun: async () => {
      // 确认操作
      let result = await proxy.Request({
        url: proxy.Api.dealWithApply,
        params: {
          applyId: applyId,
          contactType: contactType,
          status: status,
        },
      });
      if (!result) {
        return;
      }
      // 处理完成后刷新页面
      pageNo = 0;
      loadApply();

      // 状态管理实现刷新联系人列表
      if (contactType == 0 && status == 1) {
        contactStateStore.setContactReload('USER');
      } else {
        contactStateStore.setContactReload('GROUP');
      }
    },
  });
};
import { useMessageCountStore } from "../../stores/MessageCountStore";
const messageCountStore = useMessageCountStore();

//当页面处于当前页面并未进行操作时 有人发申请应当重新加载数据
watch(
  () => messageCountStore.messageCount.contactApplyCount,
  (newVal, oldVal) => {
    if (newVal > oldVal) {
      pageNo = 1;
      loadApply();
    }
  },
  { immediate: true, deep: true } // 立即执行
)
defineExpose({
  loadApply
})


const onReloadApply = () => {
  window.ipcRenderer.on('reloadApply', (event) => {
    pageNo = 0;
    pageTotal = 1;
    loadApply()
  }
  )
}

// 进入页面即调用的函数
onMounted(() => {
  loadApply();
  onReloadApply()
});

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('reloadApply')
});
</script>

<style lang="scss" scoped>
.apply-item {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ddd;
  padding: 10px 0px;
}

.contact-type {
  display: flex;
  justify-content: center;
  writing-mode: vertical-rl;
  vertical-align: middle;
  background: #2cb6fe;
  color: #fff;
  border-radius: 5px 0px 0px 5px;
  height: 50px;
}

.user-contact {
  background: #cfb0f7 !important; // 确保样式生效
}

.group-contact {
  background: #2cb6fe !important; // 确保样式生效
}

.contact-info {
  width: 260px;
  margin-left: 10px;
  text-align: left;
}

.nick-name {
  color: #000000;
}

.apply-info {
  display: flex;
  color: #999999;
  font-size: 12px;
  margin-top: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.op-btn {
  width: 50px;
  text-align: center;
}

.result-name {
  color: #999999;
  font-size: 12px;
}

.no-data-msg {
  text-align: center;
  padding: 20px;
  color: #b5b5b5;
  font-size: 12px;
  //垂直居中




}
</style>
