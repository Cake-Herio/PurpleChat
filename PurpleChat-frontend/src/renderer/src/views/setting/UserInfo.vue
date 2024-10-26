<template>
  <ContentPanel>
    <div v-if="showType == 0">
      <UserInfoDetail :userInfo="userInfo"  @changePartOne="changePart(1)"  @changePartTwo="changePart(2)" :key="userInfoKey"></UserInfoDetail>
    </div>

    <div v-if="showType == 1">
      <UserInfoEdit :data="userInfo" @editBack="editBack"></UserInfoEdit>
    </div>

    <div v-if="showType == 2">
      <UserInfoPassword @editBack="editBack"></UserInfoPassword>
    </div>
  </ContentPanel>

</template>



<script setup>
import UserInfoDetail from "./UserInfoDetail.vue";
import UserInfoEdit from "./UserInfoEdit.vue";
import UserInfoPassword from "./UserInfoPassword.vue";
const userInfoKey = ref(0)


import { ref, reactive, getCurrentInstance, nextTick,onDeactivated, onMounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const userInfo = ref({})
import {useUserInfoStore} from "@/stores/UserInfoStore"
const userInfoStore = useUserInfoStore()

const getUserInfo = async () => {

  let result = await proxy.Request({
    url: proxy.Api.getUserInfo,
    params: {
      userId: userInfoStore.getInfo().userId
    }
  })
  if (!result) {
    return;
  }

  userInfo.value = result.data
}
//当前页面有好几种显示方式
const showType = ref(0)



//按钮函数集合
const changePart = (type) => {
  showType.value = type
}

const editBack = () => {
  showType.value = 0 
  userInfoKey.value += 1  //重新渲染组件
  getUserInfo()
}

//退出登录
const logout = () => {
  proxy.Confirm({
    message: '确定要退出登录吗？',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.logout
      })
      if (!result) {
        return;
      }
      router.push('/loginDirect')
    } 
  })
}

//渲染时直接调用
getUserInfo()

onDeactivated(() => {
  // 在组件停用时保存滚动位置
  showType.value = 0

});
onMounted(() => {
  // onSaveAvatarCallBack();
});



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
