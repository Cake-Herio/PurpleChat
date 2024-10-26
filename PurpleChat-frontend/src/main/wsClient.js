import {
  WebSocket
} from "ws";
import {
  getWindow
} from "./windowProxy";
const NODE_ENV = process.env.NODE_ENV;

import store from "./store";
import {
  saveOrUpdateChatSessionBatch4Init,
  updataSessionInfoMessage,
  selectUserSessionByContactId,
  updateNoReadCount,
  updateGroupName
} from "./db/ChatSessionUserModel";
import {
  saveMessageBatch,
  updateMessage,
  saveMessage,
  initUpdateMessageStatus
} from "./db/ChatMessageModel";
import {
  updateContactNoReadCount,
  addUserSetting
} from "./db/UserSettingModel";
import Constants from "./Constants";
import {
  forctGetAvatar
} from "./ipc"
process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

let ws = null;
let wsUrl = null;
let sender = null;
let needReConnect = false;
let connectTimes = 0;
//定义重连锁 每次只允许一个重连
let reConnectLock = false;
let tempWsUrl = null;
const initWs = (config, _sender) => {
  //判断是生产环境还是开发环境
  wsUrl = `${NODE_ENV!== "development" ? store.getData("prodWsDomain") : store.getData("devWsDomain")}?token=${config.token}`;
  tempWsUrl = Constants.wsTempUse + `?token=${config.token}`
  sender = _sender;
  needReConnect = true;

  createWs()

}

const closeWs = () => {
  needReConnect = false
}

const createWs = () => {
  if (wsUrl == null) {
    console.log('暂时无连接')
    wsUrl = tempWsUrl
  }
  console.log("开始连接ws:", wsUrl);
  ws = new WebSocket(wsUrl);


  



  //监听ws的事件 如果连接成功则调用下方函数
  ws.onopen = function () {
    getWindow('main').webContents.send("reconnectSuccess")
    if (ws.readyState === WebSocket.OPEN) {
      ws.send("heart beat");
    }
  }

  //监听从服务器接受信息
  ws.onmessage = async function (e) {
    try {
      const message = JSON.parse(e.data)
      console.log("接收到消息:", JSON.stringify(message))
      const messageType = message.messageType
      //根据消息类型分别处理
      switch (messageType) {
        case 0: //ws连接成功 用于保存会话信息
          await saveOrUpdateChatSessionBatch4Init(message.extendData.chatSessionUsers) //插入会话
          //保存消息
          let totalNoReadCount = await saveMessageBatch(message.extendData.chatMessageList)

          //更新所有数据库状态为0的 到 状态为-2  条件是发送给别人的消息 我接受的消息状态为2的改为-2 因为 都代表加载中
          await initUpdateMessageStatus()

          //更新联系人数量
          await updateContactNoReadCount(store.getUserId(), message.extendData.applyCount)

          //添加用户设置
          await addUserSetting(store.getUserId(), store.getUserData("email"), store.getUserData("token"), store.getUserData("nickName"))

          //发送消息类型 不同的消息类型 渲染处理是不一样的
          setTimeout(() => {
            sender.send("receiveMessage", {
              messageType: messageType,
              totalNoReadCount: totalNoReadCount
            })
          }, 1000)

          break


        case 1: //添加好友成功
          sender.send("contactMessaage", "RELOAD")
        case 2: //处理收到的普通消息
        case 3: //群创建成功
        case 5: //处理多媒体消息
        case 8: //解散群聊
        case 9: //好友加入群聊
        case 11: //好友退出群聊
        case 12: //踢出群聊
        case 13: //发给申请人 添加好友成功
          //判断消息 如果是自己发的无需再存一次
          if (message.sendUserId == store.getUserId()) {
            break
          }

          const sessionInfo = {}
          //普通消息的extendData 应该是null 所以不用管 直接从dto取即可
          Object.assign(sessionInfo, message)
          if (message.extendData && typeof message.extendData === "object") {
            Object.assign(sessionInfo, message.extendData)
          } else {
            Object.assign(sessionInfo, message)
            if (message.contactType == 0 && messageType != 1) {
              sessionInfo.contactName = message.sendUserNickName
            }
            sessionInfo.lastReceiveTime = message.sendTime
          }
          sessionInfo.lastReceiveTime = message.sendTime

          if (messageType == 9 || messageType == 11 || messageType == 12) {
            sessionInfo.memberCount = message.memberCount
          }
          sessionInfo.status = 1

          //更新本地数据库 会话信息
          await updataSessionInfoMessage(store.getUserData("currentSssionId"), sessionInfo)

          //保存内容
          await saveMessage(message)
          const dbSessionInfo = await selectUserSessionByContactId(message.contactId)
          message.extendData = dbSessionInfo

          //发送消息给本地渲染
          sender.send("receiveMessage", message)


          if (messageType == 13) {
            sender.send("contactMessage", "RELOAD")
          }
          break

        case 4: //处理好友申请消息
          await updateContactNoReadCount({
            userId: store.getUserId(),
            noReadCount: 1
          })

          sender.send("receiveMessage", message)
          break




        case 6: //文件上传完成后的消息操作
          const data = {
            status: 1
          }
          const paramData = {
            messageId: message.messageId,
            userId: store.getUserId()
          }
          updateMessage(data, paramData)
          sender.send("receiveMessage", message)
          break

        case 7: //强制下线
          sender.send("receiveMessage", message)
          closeWs()
          break



        case 10: //修改群昵称
          updateGroupName(message.contactId, message.extendData)
          sender.send("receiveMessage", message)
          break

        case 14: //处理头像更新操作
          forctGetAvatar({
            fileId: message.contactId
          })
          break
        case 15: //封面文件上传完成 下载方显示封面
          console.log(JOSN.stringify(message))
          if(store.store.getUserId() == message.sendUser){
            return
          }

          let updateData = {}
          updateData.status = message.status
          let param = {
            messageId: message.messageId,
            userId: store.getUserId()
          }
          updateMessage(updateData, param)
          sender.send("receiveMessage", message)
          break


      }
    } catch (error) {
      console.error("处理 WebSocket 消息时出错:", error);
      setTimeout(() => {
        getWindow('main').webContents.send("wsCreateFail");

      },1000)
      console.error("WebSocket 连接失败");
    }
  }

  //监听关闭
  ws.onclose = function (event) {
    console.log("连接失败")
    getWindow("main").webContents.send("reconnect", {
      showConfirm: false
    });
    reconnect()
  }

  ws.onerror = function () {
    console.log("连接失败")

    getWindow("main").webContents.send("reconnect", {
      showConfirm: false
    });
    reconnect()
  }




  //发心跳包
  setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      try {
        ws.send("heart beat");
      } catch (error) {
        getWindow("main").webContents.send("reconnect", {
          showConfirm: false
        });
        reconnect()
      }
    }
  }, 5000);




}

//定义重连函数
let num = 0
const reconnect = (clearConnect) => {
  num++

  if (clearConnect == true) {
    connectTimes = 0
    reConnectLock = false
  }

  if (!needReConnect) {
    return
  }
  if (reConnectLock) {
    return
  }
  reConnectLock = true;

  if (connectTimes < Constants.maxReConnectTimes) {
    connectTimes++;

    setTimeout(() => {
      createWs();
      reConnectLock = false; // 重连完成后释放锁 
    }, 1000);

  } else {
    reConnectLock = false; // 释放锁
    if (needReConnect) {
      getWindow("main").webContents.send("reconnect", {
        showConfirm: true
      });
    }
    reConnectLock = true
  }

}



export {
  initWs,
  closeWs,
  reconnect
}
