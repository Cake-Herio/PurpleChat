<template>
  <layout>
    <template #left-content>
      <!-- 顶部可拖动区域 -->
      <div class="drag-panel drag"></div>
      <div class="top-search">
        <el-input clearable placeholder="搜索" v-model="searchKey" size="small" @keyup="search">
          <template #suffix>
            <span class="iconfont icon-search"></span>
          </template>
        </el-input>
      </div>

      <div class="chat-session-list" v-if="!searchKey">

        <ErrorInternet v-if="isDisplayError" :key="isDisplayError"></ErrorInternet>
        <template v-for="item in chatSessionList" :key="item.contactId">
          <ChatSession :data="item" @contextmenu.stop="onContextMenu(item, $event)"
            @click="chatSessionClickHandler(item)" :currentChatSession="currentChatSession.contactId == item.contactId">
          </ChatSession>
        </template>
      </div>

      <div class="search-list" v-show="searchKey">
        <SearchResult :data="item" v-for="item in searchList" :key="item" @click="searchClickHandler(item)">
        </SearchResult>
      </div>



    </template>

    <template #right-content>
      <div class="title-panel drag" v-if="Object.keys(currentChatSession).length > 0">
        <div class="title">
          <span>{{ currentChatSession.contactName }}</span>
          <span v-if="currentChatSession.contactType == 1">
            ({{ currentChatSession.memberCount }})
          </span>
        </div>
      </div>
      <div v-if="currentChatSession.contactType == 1" class="iconfont icon-more no-drag" @click="showGroupDetail"></div>
      <div class="chat-panel" v-show="Object.keys(currentChatSession).length > 0">
        <div class="message-panel">
          <div class="message-item" v-for="(data, index) in messageList" :id="'message' + data.messageId"
            :key="data.messageId"><!-- 系统消息分开处理 -->
            <template
              v-if="data.messageType == 1 || data.messageType == 3 || data.messageType == 8 || data.messageType == 9 || data.messageType == 11 || data.messageType == 12 || data.messageType == 13">
              <ChatMessageSys :data="data"></ChatMessageSys>
            </template>
            <!-- 间隔时间过久应展示发送消息的时间 -->
            <template v-if="isShowTime(index) && (data.messageType == 2 || data.messageType == 5)">
              <ChatMessageShowTime :data="data"></ChatMessageShowTime>
            </template>


            <!-- 展示发送的消息  -->
            <template v-if="data.messageType == 1 || data.messageType == 2 || data.messageType == 5">
              <ChatMessage :data="data" :currentChatSession="currentChatSession"
                @showMediaDetail="showMediaDetailHandler" :key="data.messageId + '_' + forceRefresh[data.messageId]">
              </ChatMessage>
            </template>
          </div>
        </div>


        <transition name="fade-slide">
          <!-- 新消息弹窗 -->
          <div class="newMessage" v-if="newMessageVisible" @click="scrollToBottom">
            <span class="iconfont icon-new-message">
              <svg t="1726284211477" class="icon" viewBox="0 0 1024 1024" version="1.1"
                xmlns="http://www.w3.org/2000/svg" p-id="22098" width="200" height="200">
                <path
                  d="M236.8 460.8c0 26.88 20.48 47.36 47.36 47.36s47.36-20.48 47.36-47.36-20.48-47.36-47.36-47.36-47.36 21.76-47.36 47.36zM523.52 460.8c0-26.88-20.48-47.36-47.36-47.36s-47.36 20.48-47.36 47.36 20.48 47.36 47.36 47.36c25.6 1.28 47.36-21.76 47.36-47.36zM711.68 460.8c0-26.88-20.48-47.36-47.36-47.36s-47.36 20.48-47.36 47.36 20.48 47.36 47.36 47.36c25.6 1.28 47.36-21.76 47.36-47.36z"
                  fill="#d4c1f3" p-id="22099"></path>
                <path
                  d="M847.36 125.44h-742.4c-49.92 0-89.6 40.96-89.6 89.6v496.64c0 49.92 40.96 89.6 89.6 89.6h209.92c19.2 0 37.12-16.64 38.4-35.84 0-10.24-3.84-19.2-10.24-26.88-6.4-6.4-16.64-11.52-26.88-11.52l-186.88-2.56c-21.76 0-39.68-19.2-39.68-40.96V239.36c0-21.76 19.2-40.96 40.96-40.96h693.76c21.76 0 40.96 19.2 40.96 40.96v442.88c0 21.76-17.92 40.96-40.96 40.96l-272.64 2.56c-12.8 0-24.32 5.12-33.28 14.08L349.44 903.68c-8.96 8.96-12.8 20.48-10.24 33.28 1.28 5.12 2.56 8.96 6.4 12.8 6.4 10.24 19.2 17.92 30.72 17.92 8.96 0 19.2-3.84 25.6-10.24l151.04-147.2c6.4-6.4 15.36-8.96 23.04-8.96h271.36c49.92 0 89.6-40.96 89.6-89.6V213.76c1.28-48.64-39.68-88.32-89.6-88.32z"
                  fill="#d4c1f3" p-id="22100"></path>
                <path d="M889.6 175.36m-119.04 0a119.04 119.04 0 1 0 238.08 0 119.04 119.04 0 1 0-238.08 0Z"
                  fill="#d4c1f3" p-id="22101"></path>
              </svg>
            </span>
            <span class="info">新消息</span>
          </div>
        </transition>




        <MessageSend :currentChatSession="currentChatSession" @sendMessage2Local="sendMessage2LocalHandler"
          @reloadMessageStatus="reloadMessageStatusHandler">
        </MessageSend>
      </div>

      <div class="chat-blank" v-show="Object.keys(currentChatSession).length == 0">
        <Blank :msg="'chat'" ></Blank>
      </div>
    </template>
  </layout>

  <ChatGroupDetail ref="chatGroupDetailRef" @delChatSessionCallBack="delChatSession"></ChatGroupDetail>
</template>

<script setup>
import SearchResult from './SearchResult.vue';
import ChatGroupDetail from './ChatGroupDetail.vue';
import ChatMessageSys from './ChatMessageSys.vue';
import { SHOW_TIME_GAP } from '@/utils/Constants'
import ChatMessageShowTime from "./ChatMessageShowTime.vue";
import Blank from "@/components/Blank.vue";
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
import MessageSend from "./MessageSend.vue";
import ContextMenu from "@imengyu/vue3-context-menu";
import ChatSession from "./ChatSession.vue";
import ChatMessage from './ChatMessage.vue';
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted, watch, onActivated, onDeactivated } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
const chatSessionList = ref([])
const forceRefresh = ref(0)
const currentChatSession = ref({})
const messageList = ref([{
}])
const messagePanelHeigth = ref(null)
const messagePanel = ref(null)
const scrollPosition = ref(0);
const reloadList = ref(0)
//记录选中会话的分页 每次都需要重置
const messageCountInfo = ref({
  totalPage: 0,
  pageNo: 1,
  maxMessageId: null,
  noData: false
})
const newMessageVisible = ref(false)

const isShowTime = (index) => {
  const message = messageList.value[index]
  const lastMessage = index > 0 ? messageList.value[index - 1] : null
  if (message.messageType == 2 || message.messageType == 5) {
    if (index > 1 && message.sendTime - lastMessage.sendTime >= SHOW_TIME_GAP) {
      return true
    } else if (messageCountInfo.value.pageNo == messageCountInfo.value.totalPage + 1 && index == 0) {

      return true
    }
  }
  return false
}

const searchKey = ref("")
const searchList = ref([])
const search = () => {

  if (searchKey.value == "") {
    return
  }
  searchList.value = []
  const regex = new RegExp("(" + searchKey.value + ")", 'gi')
  chatSessionList.value.forEach(item => {
    if (item.contactName.includes(searchKey.value) || item.lastMessage.includes(searchKey.value)) {
      let newItem = Object.assign({}, item)
      //需要将搜索的内容高亮
      newItem.searchContactName = item.contactName.replace(regex, "<span class='highlight'>$1</span>")
      newItem.searchLastMessage = item.lastMessage.replace(regex, "<span class='highlight'>$1</span>")
      searchList.value.push(newItem)
    }
  })
}





const setTop = (data) => {
  data.topType = data.topType == 0 ? 1 : 0
  //会话排序
  sortChatSessionList(chatSessionList.value)
  window.ipcRenderer.send("topChatSession", {
    contactId: data.contactId,
    topType: data.topType
  })
}


const delChatSession = (contactId,sessionId) => {
  //对当前列表直接过滤
  chatSessionList.value = chatSessionList.value.filter(item => item.contactId != contactId)
  //更新数据库
  window.ipcRenderer.send("delChatSessionData", {
    contactId,
    sessionId
  })
  //如果删除的当前选中的页面 需要对右边也更新
}

//组件 用于展示右键呼唤菜单
const onContextMenu = (data, e) => {
  ContextMenu.showContextMenu({
    x: e.x,
    y: e.y,
    items: [

      {
        label: data.topType == 0 ? '置顶' : '取消置顶',
        onClick: () => {
          setTop(data)
        }
      },
      {
        label: '删除',
        onClick: () => {
          proxy.Confirm({
            message: `确定删除与【${data.contactName}】的聊天吗？`,
            okfun: () => {
              delChatSession(data.contactId,data.sessionId)
              //删除后需要判断是否是当前选中的会话 如果是需要清空右边的数据
              if (currentChatSession.value.contactId == data.contactId) {
                currentChatSession.value = {}
                window.ipcRenderer.send("clearCurrentChatSession")
                messageList.value = []
              }
            }
          })
        }
      }
    ]
  })
}
import { useMessageCountStore } from '../../stores/MessageCountStore'
const messageCountStore = useMessageCountStore()
const chatSessionClickHandler = (item) => {
  //这里需要判断是否是同一个会话 如果是同一个会话就不需要再次加载
  if (currentChatSession.value.sessionId == item.sessionId) {
    return
  }
  messageList.value = []
  //点击后需要在主进程中更新当前选中的会话id 实现当前会话的数据清空
  setSessionSelect({ contactId: item.contactId, sessionId: item.sessionId })
  currentChatSession.value = Object.assign({}, item)
  //点击后消息记录数清空
  messageCountStore.setCount("chatCount", -item.noReadCount, false)
  item.noReadCount = 0
  //点击后重置页数记录
  clearPageRecord()
  loadChatMessage()
}

const clearPageRecord = () => {
  messageCountInfo.value.totalPage = 0
  messageCountInfo.value.pageNo = 1
  messageCountInfo.value.maxMessageId = null
  messageCountInfo.value.noData = false
}


const loadChatMessage = () => {
  window.ipcRenderer.send("loadChatMessageData", {
    sessionId: currentChatSession.value.sessionId,
    pageNo: messageCountInfo.value.pageNo,
    maxMessageId: messageCountInfo.value.maxMessageId
  })
}
//设置当前选中的会话 同时清空未读数 
const setSessionSelect = (data) => {
  window.ipcRenderer.send("setSessionSelect", data)
}
import { debounce } from 'lodash';
const scrollToBottom = debounce(() => {
  nextTick(() => {
    if (messagePanel.value) {
      scrollPosition.value = messagePanel.value.scrollHeight;
      setTimeout(() => {
        messagePanel.value.scrollTo({
          top: messagePanel.value.scrollHeight,
          behavior: 'smooth' // 添加平滑滚动效果
        });
      }, 60);

      if (newMessageVisible.value) {
        newMessageVisible.value = false;
      }
    }
  });
}, 150); // 150ms 防抖时间


//对会话列表进行排序
const sortChatSessionList = (sessionList) => {
  sessionList.sort((a, b) => {
    //置顶优先
    if (a.topType != b.topType) {
      return b.topType - a.topType
    }
    //时间排序
    return b.lastReceiveTime - a.lastReceiveTime
  })
}
import { useDynamicStore } from '@/stores/dynamicStore';
const dynamicStore = useDynamicStore()
import { useUserInfoStore } from "@/stores/UserInfoStore"
const userInfoStore = useUserInfoStore()


let isSendMedia = false

const reloadMessageStatusHandler = (messageObj) => {
  console.log("reloadMessageStatusHandler")
  for (let index = messageList.value.length - 1; index >= 0; index--) {
    const message = messageList.value[index];
    if (message.messageId === messageObj.messageId) {
      messageList.value[index].status = messageObj.status;
      forceRefresh.value = {
        ...forceRefresh.value, // 展开已有的数据
        [message.messageId]: Math.random() // 动态地添加或更新特定 messageId 的键
      };
      break; // 如果只需要匹配到第一个满足条件的元素，可以用 break 提前退出循环
    }
  }
}




const sendMessage2LocalHandler = (messageObj) => {

  if (messageObj.messageType == 5) {
    isSendMedia = true
  }

  //发送消息到本地
  messageList.value.push(messageObj)

  //更新会话
  chatSessionList.value.forEach(item => {
    if (item.sessionId == messageObj.sessionId) {
      item.lastMessage = messageObj.messageContent
      item.lastReceiveTime = messageObj.sendTime
    }
  })
  //重新排序
  sortChatSessionList(chatSessionList.value)
  //判断是我发送的才滚动
  if (messageObj.sendUserId == userInfoStore.getInfo().userId && messageObj.messageType != 5) {
    scrollToBottom()
  }
}


watch(
  () => dynamicStore.getStatus(),
  (newVal, oldVal) => {
    switch (newVal) {
      case "MEDIA_LOADED":
        if (isSendMedia == true) {
          console.log("发送完毕")
          isSendMedia = false
          scrollToBottom()
        }
        break
      case "RELOAD_SESSION":
        loadChatSession()
        break
    }
    dynamicStore.setStatus("")
  },
  { immediate: true }
)




nextTick(() => {
  messagePanel.value = document.querySelector(".message-panel")
})





//通过状态管理 实现图片完全渲染后再滚动到底部
watch(
  () => {
    if (messageList.value && messageList.value.length > 0 && messageList.value[messageList.value.length - 1].messageId) {
      return dynamicStore.getData(messageList.value[messageList.value.length - 1].messageId);
    }
    return null;
  },
  (newVal, oldVal) => {
    if (newVal || oldVal == undefined) {
      setTimeout(() => {
        scrollToBottom()
      }, 100)
    }
  },
  { immediate: true }
)




const loadChatSession = () => {
  window.ipcRenderer.send("loadChatSessionData", {})
}

import { useContactStateStore } from '@/stores/ContactStateStore';
const contactStateStore = useContactStateStore();




const onReceiveMessage = () => {
  window.ipcRenderer.on("receiveMessage", (event, message) => {
    switch (message.messageType) {
      case 0:
        if (message.totalNoReadCount) {
          messageCountStore.setCount("chatCount", message.totalNoReadCount, true)
        }
      case 1: //我申请好友 被通过
        contactStateStore.setContactReload("ALL")
      case 2: //处理收到的普通消息
      case 3: //创建群聊成功
      case 5: //处理多媒体消息
      case 8: //群聊解散
      case 9: //好友加入群聊
        if (message.messageType == 9) {
          window.ipcRenderer.send("transferStation", "RELOAD_CONTACT")
        }
      case 11: //好友退出群聊
      case 12: //踢出群聊
      case 13: //添加别人自己好友成功 刷新session




        //如果是当前选中的会话 则需要更新消息列表
        if (currentChatSession.value.sessionId == message.sessionId) {
          messageList.value.push(message)
          //群聊人数更新
          if (message.messageType == 9 || message.messageType == 11 || message.messageType == 12) {
            currentChatSession.value.memberCount = message.memberCount
          }

          //如果滚动条在最底部 直接滚动条移动 否则添加弹窗说明有新消息
          if (messagePanel.value.scrollHeight - messagePanel.value.scrollTop - messagePanel.value.clientHeight < 10) {
            scrollToBottom()
          } else {
            newMessageVisible.value = true
          }
        }

        //如果不是选中会话 需要增加noRead
        else {
          chatSessionList.value.forEach(item => {
            if (item.sessionId == message.sessionId) {
              item.noReadCount++
              return
            }
          })
        }
        if (currentChatSession.value.sessionId != message.sessionId) {
          messageCountStore.setCount("chatCount", 1, false)
        }

        loadChatSession()
        return


      case 4: //处理好友添加 需要数据库查询
        loadContactApply()
        return


      case 6:
      case 15:
        //找到消息重新渲染
        const reloadMessage = messageList.value.find(item => item.messageId == message.messageId)
        if (reloadMessage != null) {
          console.log("reloadMessage case 15")
          reloadMessage.status = message.status
        }
        return
      case 7: //强制下线
        proxy.Confirm({
          message: message.messageContent,
          okfun: () => {
            window.ipcRenderer.send("relogin")
          },
          showCancelBtn: false
        })
        return

      case 10: //更新群昵称
        chatSessionList.value.forEach(item => {
          if (item.sessionId == message.sessionId) {
            item.contactName = message.contactName
          }
        })
        return
    }


    if (message.messageInfo) {
      switch (message.messageInfo) {
        case "RELOAD_SESSION":
          loadChatSession()
          break
      }
    }
  });
}


const refreshChatSession = () => {
  loadChatSession()
}

const onRefreshChatSession = () => {
  window.ipcRenderer.on("refreshChatSession", (event, data) => {
    currentChatSession.value = {}
    //发主进程设置当前session为空
    window.ipcRenderer.send("clearCurrentChatSession")
    refreshChatSession()
  })
}





const jumpToChatPage = (contactId, tryCount) => {

  if (chatSessionList.value.length == 0) {
    chatSessionList.value = dynamicStore.getData("chatSessionList")
  }


  let currentChatSession = chatSessionList.value.find(item => item.contactId == contactId)
  if (currentChatSession) {
    chatSessionClickHandler(currentChatSession)
    tryCount = 0
  } else {
    if (tryCount > 3) {
      //可能是因为数据库中没有找到会话
      return
    }


    tryCount++
    //如果没有找到会话 说明之前手动删除过 即数据库中状态为0 需要重新将状态设置为1
    window.ipcRenderer.send("reloadSession", { contactId: contactId })
  }
}
let tryCount = 0
//监听点击头像发消息快速导航
watch(
  () => route.query.timestamp,
  (newVal, oldVal) => {
    if (oldVal === undefined) {
      const initialTimestamp = new Date().getTime()
      oldVal = initialTimestamp; // 设置为默认值
    }

    if (newVal && route.query.contactId) {
      jumpToChatPage(route.query.contactId, tryCount)
    }
  },
  { immediate: true }

)


const onReloadSessionCallBack = () => {
  window.ipcRenderer.on("reloadSessionCallBack", (event, data) => {
    sortChatSessionList(data.sessionList)
    chatSessionList.value = data.sessionList

    let currentChatSession = chatSessionList.value.find(item => item.contactId == data.contactId)
    if (currentChatSession) {
      chatSessionClickHandler(currentChatSession)
      tryCount = 0
    } else {
      proxy.Confirm({
        message: "未找到会话，对方可能已将您删除T_T",
        showCancelBtn: false
      })
      return
    }

  })
}



const onloadChatSessionDataCallBack = () => {
  window.ipcRenderer.on("loadChatSessionDataCallBack", (event, data) => {
    //获取总的未读数
    chatSessionList.value = data
    let totalNoReadCount = 0
    chatSessionList.value.forEach(item => {
      totalNoReadCount += item.noReadCount
    })
    messageCountStore.setCount("chatCount", totalNoReadCount, true)
    dynamicStore.setData("chatSessionList", data)
    sortChatSessionList(chatSessionList.value)


  })
}
let lastMessage = null
const onloadChatMessageDataCallBack = () => {
  window.ipcRenderer.on("loadChatMessageDataCallBack", (event, data) => {

    if (data.pageNo == data.pageTotal) {
      messageCountInfo.value.noData = true
    }
    //对拿到的消息进行排序
    data.dataList.sort((a, b) => {
      return a.messageId - b.messageId
    })
    messageList.value = data.dataList.concat(messageList.value)
    messageCountInfo.value.pageNo = data.pageNo
    messageCountInfo.value.totalPage = data.pageTotal


    //为了保证翻页没有问题 需要定一个边界 边界上面就是历史数据 
    //现在需要找的边界 也就是此时的最大messageId
    if (data.pageNo == 1) {
      const dataListLength = data.dataList.length
      dataListLength > 0 ? messageCountInfo.value.maxMessageId = data.dataList[dataListLength - 1].messageId : null
      messagePanelHeigth.value = messagePanel.value.scrollHeight
      lastMessage = messageList.value[0]
      nextTick(() => {
        scrollToBottom()
      })
    } else {
      //记录更新前的位置 使得滚动顺滑
      nextTick(() => {
        //定位到lastmessage的位置
        const lastMessageElement = document.getElementById("message" + lastMessage.messageId)
        //滚动条滑动到那个位置
        lastMessageElement.scrollIntoView({
          block: 'start'      // 将元素滚动到视图的顶部
        });
        messagePanel.value.scrollBy(0, -10);
        lastMessage = messageList.value[0]
      })
    }
    if (messageCountInfo.value.pageNo <= messageCountInfo.value.totalPage)
      messageCountInfo.value.pageNo++
  })
}

const onAddLocalMessageCallBack = () => {
  window.ipcRenderer.on("addLocalMessageCallBack", (event, { messageId, status }) => {
    const findMessage = messageList.value.find(item => item.messageId == messageId)
    if (findMessage != null) {
      findMessage.status = status
    }
    scrollToBottom()
  })
}

//处理查看媒体详情消息
const showMediaDetailHandler = (messageId) => {
  let showFileList = messageList.value.filter(item => item.messageType == 5 && item.fileType != 2)
  showFileList = showFileList.map(item => {
    return {
      partType: 'chat',
      fileId: item.messageId,
      fileType: item.fileType,
      fileName: item.fileName,
      fileSize: item.fileSize,
      forceGet: false
    }
  })

  //传给主进程
  window.ipcRenderer.send("newWindow", {
    windowId: "mediaDetail",
    title: "媒体详情",
    path: "/showMediaDetail",
    data: {
      currentFileId: messageId,
      fileList: showFileList
    }
  })
}

const chatGroupDetailRef = ref(null)

const showGroupDetail = () => {
  chatGroupDetailRef.value.show(currentChatSession.value.contactId)
}

const loadContactApply = () => {
  window.ipcRenderer.send("loadContactApply", {}) //处理未读
  window.ipcRenderer.send("transferStation", 'RELPAD_APPLY')
}

const onLoadContactApplyCallBack = () => {
  window.ipcRenderer.on("loadContactApplyCallBack", (event, contactNoRead) => {
    messageCountStore.setCount("contactApplyCount", contactNoRead, true)
  })
}

const searchClickHandler = (data) => {
  searchKey.value = undefined
  chatSessionClickHandler(data)
}
const isDisplayError = ref(false)

const onReconnect = () => {
  window.ipcRenderer.on("reconnect", (event, data) => {
    isDisplayError.value = true
    if(data.showConfirm == false || data.showConfirm == 'false'){
      return
    }
    proxy.Confirm({
      message: "网络连接已断开，是否重新连接？若否，则退出登录",
      okfun: () => {
        window.ipcRenderer.send("reconnectTrue")
      },
      showCancelBtn: true,
      cancelFun: () => {
        window.ipcRenderer.send("relogin")
      }
    })
  })
}

const onReconnectSuccess = () => {
  window.ipcRenderer.on("reconnectSuccess", (event, data) => {
    isDisplayError.value = false
  })
}

const onUploadFileCallBack = () => {
  window.ipcRenderer.on('uploadFileCallBack', (event, data) => {
    //将list中的messageId 并更改状态
    for(let i = messageList.value.length-1 ; i>=0; i--){
      let message = messageList.value[i]
      
      if (message.messageId == data.messageId) {
        console.log("找到" + data.status)
        messageList.value[i].status = data.status
      }
    }
  })
}

const onRefreshMessageStatus = () => {
  window.ipcRenderer.on("refreshMessageStatus", (event, data) => {
    //将list中的messageId 并更改状态
    messageList.value.forEach((message, index) => {
      if (message.status === data.status && message.messageId == undefined) {
        messageList.value[index].status = data.status
        forceRefresh.value = {
          ...forceRefresh.value, // 展开已有的数据
          [message.messageId]: Math.random() // 动态地添加或更新特定 messageId 的键
        };
        return
      }
    });
  })
}

const onDownloadFileCallBack = () => {
  window.ipcRenderer.on('downloadFileCallBack', (event, data) => {
    // 将list中的messageId 并更改状态

    for(let i = messageList.value.length-1 ; i>=0; i--){
      let message = messageList.value[i]
      if (message.messageId == data.fileId) {
        console.log("找到" + data.status)
        messageList.value[i].status = data.status
      }
    }

  })
}


const onWsCreateFail = () => {
  console.log('onWsCreateFail监听')
  window.ipcRenderer.on('wsCreateFail', (event) => {
    console.log('wsCreateFail')
    proxy.Confirm({
      message: '初始化失败，请重新登录',
      showCancelBtn: false,
      okfun: () => {
        window.ipcRenderer.send('exit');
      }
    })
  })
}

const onExpressFail = () => {
  window.ipcRenderer.on('expressFail', (event, msg) => {
    console.log('expressFail')
    proxy.Confirm({
      message: msg,
      showCancelBtn: false,
      okfun: () => {
        window.ipcRenderer.send('exit');
      }
    })
  })
}


onMounted(() => {
  messagePanel.value = document.querySelector(".message-panel");
  onReceiveMessage()
  onUploadFileCallBack()
  onDownloadFileCallBack()
  onloadChatSessionDataCallBack()
  onloadChatMessageDataCallBack()
  onAddLocalMessageCallBack()
  onLoadContactApplyCallBack()
  onReloadSessionCallBack()
  onRefreshMessageStatus()
  onRefreshChatSession()
  onReconnect()
  onReconnectSuccess()
  onExpressFail()
  //为了避免还未渲染完成时 主进程发送了消息过来，导致无法处理，因此直接调用查库函数也是可以的
  loadChatSession()
  //首次渲染需调用noread
  loadContactApply()
  onWsCreateFail()

  nextTick(() => {
    messagePanel.value.addEventListener("scroll", (e) => {


      if (messagePanel.value.scrollTop == 0 && messageCountInfo.value.pageNo <= messageCountInfo.value.totalPage) {
        loadChatMessage()

      }
    })
  })

  setSessionSelect({})
  //同步未读消息数
})






//记得在页面销毁时去掉监听  
onUnmounted(() => {
  window.ipcRenderer.removeAllListeners("receiveMessage")
  window.ipcRenderer.removeAllListeners("loadChatSessionDataCallBack")
  window.ipcRenderer.removeAllListeners("loadChatMessageDataCallBack")
  window.ipcRenderer.removeAllListeners("addLocalMessageCallBack")
  window.ipcRenderer.removeAllListeners("loadContactApplyCallBack")
  window.ipcRenderer.removeAllListeners("reloadSessionCallBack")
  window.ipcRenderer.removeAllListeners("reconnect")
  window.ipcRenderer.removeAllListeners("reconnectSuccess")
  window.ipcRenderer.removeAllListeners("refreshChatSession")
  window.ipcRenderer.removeAllListeners("uploadFileCallBack")
  window.ipcRenderer.removeAllListeners("downloadFileCallBack")
  window.ipcRenderer.removeAllListeners("wsCreateFail")
  window.ipcRenderer.removeAllListeners("refreshMessageStatus")
  window.ipcRenderer.removeAllListeners("expressFail")

  messagePanel.value.removeEventListener("scroll", () => { })
})

onActivated(() => {
  messagePanel.value = document.querySelector(".message-panel");
  // 在组件激活时恢复滚动位置
  if (messagePanel.value) {
    nextTick(() => {
      messagePanel.value.scrollTo({
        top: messagePanel.value.scrollHeight,
        behavior: 'smooth' // 添加平滑滚动效果
      });
    });
  }
});

onDeactivated(() => {
  // 在组件停用时保存滚动位置
  if (messagePanel.value) {
    scrollPosition.value = messagePanel.value.scrollHeight;
  }

  currentChatSession.value = {}
  //发主进程设置当前session为空
  window.ipcRenderer.send("clearCurrentChatSession")

});
//Util需要滚动到底部







</script>

<style lang="scss" scoped>
.drag-panel {
  height: 25px;
  background: #f7f7f7;
}

.top-search {
  padding: 0px 10px 9px 10px;
  background: #f7f7f7;
  display: flex;
  align-items: center;
}

.iconfont {
  font-size: 12px;
}

.chat-session-list {
  height: calc(100vh - 62px);
  overflow: hidden;
  border-top: 1px solid #ddd;
}

.chat-session-list:hover {
  overflow: auto;
}

.title-panel {
  display: flex;
  align-items: center;
}

.title {
  height: 60px;
  line-height: 60px;
  padding-left: 10px;
  font-size: 18px;
  color: #000000;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.icon-more {
  position: absolute;
  z-index: 1;
  top: 30px;
  right: 3px;
  width: 20px;
  font-size: 20px;
  margin-right: 5px;
  cursor: pointer;
}

.chat-panel {
  border-top: 1px solid #ddd;
  background: #f5f5f5;
}

.message-panel {
  padding: 10px 30px 0px 30px;
  height: calc(100vh - 200px - 62px);
  overflow-y: auto;
}

.message-item {
  margin-bottom: 15px;
  text-align: center;
}

.message-send-panel {
  margin-top: calc(100vh - 200px);
  z-index: 1;
  position: sticky;
}


.newMessage {
  position: fixed;
  bottom: 205px;
  left: 320px;
  /* 距离右边 20px */
  background-color: #fcfcfc;
  color: #000;
  padding: 10px 15px;
  border-radius: 5px;
  z-index: 1000;
  /* 确保它在高层级 */
  border: 1px solid #d1a0f9;
  box-shadow: 0 2px 10px rgba(153, 111, 226, 0.2);
  cursor: pointer;

  .info {
    font-size: 13px;
    color: #dcbefa;
    margin-left: 10px;
    //相对位移
    position: relative;
    top: -5px;
  }

}

.newMessage::before {
  content: '';
  position: absolute;
  bottom: -5px;
  /* 使三角形贴在提示框的底部 */
  left: 20px;
  /* 让三角形贴在右侧 */
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 5px solid #d1a0f9;
  /* 三角形的颜色与背景色相同 */
}

.newMessage:hover {
  background-color: #f0e3fb;
  color: #c15bf8;
  transition: 0.6s;
}

.icon-new-message {
  svg {
    width: 20px;
    height: 60%;
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
  /* 进入和离开时向下位移 */
}
</style>
