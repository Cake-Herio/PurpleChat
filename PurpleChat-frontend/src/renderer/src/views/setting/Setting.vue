<template>
   <layout>
      <template #left-content>
         <div class="title-panel drag">设置</div>
         <div class="menu-list">
            <div :class="['menu-item', route.path == item.path ? 'menu-active' : '']" v-for="item in settingMenuList"
               @click="jump(item)" :key="item.name">
               <div :class="['iconfont', item.icon]" :style="{ background: item.bgColor }"></div>
               <div class="menu-name">{{ item.name }}</div>
            </div>
         </div>
      </template>



      <template #right-content>
         <div class="title-panel drag"></div>

         <router-view v-slot="{ Component }">
            <keep-alive include="UserInfo">
               <component :is="Component" />
            </keep-alive>
         </router-view>

      </template>
   </layout>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const settingMenuList = ref([
   {
      name: "基本设置",
      icon: 'iconfont icon-setting',
      path: '/setting/userInfo',
      bgColor: '#0294f5'
   }, {
      name: "文件管理",
      icon: 'icon-folder',
      path: '/setting/fileManage',
      bgColor: '#ffd04f'
   }, {
      name: "关于",
      icon: 'icon-about',
      path: '/setting/about',
      bgColor: '#cfb0f7'
   }
])



//按钮函数
const jump = (item) => {
   router.push(item.path)
}
import { useUserInfoStore } from '@/stores/UserInfoStore';
const userInfoStore = useUserInfoStore();
import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore();

const onRefreshAvatar = () => {
   window.ipcRenderer.on('refreshAvatar', () => {
      dynamicStore.setData("refresh" + userInfoStore.getInfo().userId, new Date().getTime())
   });
}


const isDisplayError = ref(false)


onMounted(() => {
   onRefreshAvatar();
});
onUnmounted(() => {
   window.ipcRenderer.removeAllListeners('refreshAvatar');
});




</script>







<style lang="scss" scoped>
.title-panel {
   height: 60px;
   background: #f7f7f7;
   text-align: center;
   line-height: 60px;
}

.menu-list {
   border-top: 1px solid #ddd;
   background: #f7f7f7;

   .menu-item {
      display: flex;
      align-items: center;
      border-bottom: 1px solid #ddd;

      padding: 10px;

      &:hover {
         cursor: pointer;
         background: #ede3f5;
         transition: all 0.3s;
      }

      .iconfont {
         font-size: 20px;
         width: 35px;
         height: 35px;
         color: #fff;
         display: flex;
         align-items: center;
         justify-content: center;
         border-radius: 3px;
      }

      .menu-name {
         color: #000000;
         margin-left: 10px;
         flex: 1;
      }

   }

   .menu-active {
      background: #ede3f5;

      &:hover {
         background: #ede3f5;
      }
   }
}
</style>