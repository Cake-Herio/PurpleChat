import { app, shell, BrowserWindow,ipcMain, ipcRenderer } from 'electron'
import store from './store'
import { initWs, closeWs, reconnect } from './wsClient'
import {selectUserSessionList, delChatSession, topChatSession, updataSessionInfoMessage,readAll, updateStatus, delChatSessionCompletely} from "./db/ChatSessionUserModel"
import {selectChatMessageList, saveMessage, updateMessage, delMessageCompletely, selectMessage}  from "./db/ChatMessageModel"
import {selectUserSettingInfo, updateContactNoReadCount, loadLocalUser, saveUserToken,deleteUserSetting, updateUserSetting } from "./db/UserSettingModel"
import { getLocalFilePath,saveDefautLocalUserAvatar,saveMyGroupAvatar2Local,addFlie2Local, createCover,saveAs, saveClipBoardFile, closeLocalServer, openLocalFolder, changeLocalFolder, downloadUpdate, startTempServer, saveUserAvatar2Local, addAvatar2Local,downloadFile, reloadOnline } from './file'
import { create } from 'js-md5'
import { getWindow, saveWindow,  deleteWindow} from './windowProxy'
import { is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset';
import path, { join } from 'path';
import utils from './utils'
const fs = require('fs');
const fse = require('fs-extra');
const cover_image_suffix = "_cover.png";
const image_suffix = ".png";

const NODE_ENV = process.env.NODE_ENV
const onLoadLocalUser = () => {
    ipcMain.on("loadLocalUser", async(e) => {
        let userList = await loadLocalUser()

        e.sender.send("loadLocalUserCallBack", userList)
    })
}

//临时启动本地服务
const onStartTempLocalServer = () => {
    ipcMain.on("startTempServer", (e, ) => {
        startTempServer()

    })
}

const onDeleteUserSetting = () => {
    ipcMain.on("deleteUserSetting", async(e, userId) => {
        await deleteUserSetting(userId)
    })
}




const onLoginOrRegister = (callback) => {
    ipcMain.on("loginOrRegister", (e, isLogin) => {
        callback(isLogin)
    })

}

const onLoginSuccess = (callback) => {
    ipcMain.on("openChat", async(e, config) => {
        store.initUserId(config.userId)
        if(config.email){store.setUserData("email", config.email)}
        if(config.nickName){store.setUserData("nickName", config.nickName)}
        if(config.token){store.setUserData("token", config.token)}
         //存储token到数据库 重新设置过期时间
            if(config.isDirect == false){
            saveUserToken(config.token)
        }
        initWs(config, e.sender)
        //数据库调用获取email
        setTimeout(async() => {
            let email = (await selectUserSettingInfo()).email
            console.log("email", email)
            e.sender.send("init", {email})
        }, 1000)
        callback(config)
        e.sender.send("openChatCallBack")
        //保存最新的頭像到本地 只需要cover即可
        if(!config.userId){
            return
        }
        try {
            let avatarCoverPath = await getLocalFilePath('avatar', true, config.userId);
            await downloadFile(config.userId, avatarCoverPath, 'avatar', true);
            const userAvatarFolder = store.getData("userAvatarFolder");
            console.log("avatarPath", avatarCoverPath);
            
            let saveCoverPath = path.join(userAvatarFolder, config.userId + cover_image_suffix);
            fse.copyFileSync(avatarCoverPath, saveCoverPath);
            
            console.log("File copied successfully to:", saveCoverPath);
        } catch (error) {
            console.error("An error occurred during the process:", error);
        }
        
        

    })
}

const winTitleOp = (callback) => {
    ipcMain.on("winTitleOp", (e, data) => {
        callback(e, data)
    })
}

//通过监听实现保存值 和取值
const onSetLocalStore = () => {
    ipcMain.on("setLocalStore", (e, {key,value})  => {
        store.setData(key, value)
    })
}

const onGetLocalStore = () => {
    ipcMain.on("getLocalStore", (e, key) => {
        //渲染进程去取值
        e.sender.send("getLocalStoreBack", store.getData(key))
    })
}


//查询数据库会话信息
const onLoadChatSession = () => {
    ipcMain.on("loadChatSessionData", async(e, data) => {
        const dataList = await selectUserSessionList()
        e.sender.send("loadChatSessionDataCallBack", dataList)})}

const onDelChatSession = () => {
    ipcMain.on("delChatSessionData", async(e, data) => {
        delChatSession(data.contactId)
        //刪除contactID 的全部message
        delMessageCompletely(data.sessionId)
    })}

const onTopChatSession = () => {
    ipcMain.on("topChatSession", async(e, data) => {
        await topChatSession(data.contactId, data.topType)
    })

}

const onloadChatMessageData = () => {
    ipcMain.on("loadChatMessageData", async(e, data) => {
        //查询数据库
        const result = await selectChatMessageList(data.sessionId, data.pageNo, data.maxMessageId)
        e.sender.send("loadChatMessageDataCallBack", result)
    })
}

const onAddAvatar2Local = () => {}
    ipcMain.on("addAvatar2Local", async(data) => {
        addAvatar2Local(data)
    })

//监听发送消息
const onAddLocalMessage = () => {
    ipcMain.on("addLocalMessage", async(e, data) => {
        //更新消息库
        let messageId = await saveMessage(data)
        //如果messagestatus为 -1需要回调给主函数
        if(data.status == -1){
            e.sender.send("refreshMessageStatus", {status:-1, messageId: messageId})
            return
        }
        //保存文件到本地 
        if(data.status != -1 && data.messageType == 5){
            await addFlie2Local(data.messageId, data.filePath, data.fileType)
        }



        //更新会话库
        //我本地发送的是sendtime 改为receiveTime
        data.lastReceiveTime = data.sendTime
        await updataSessionInfoMessage(store.getUserData("currentSssionId"), data)
        //给一个回调函数
        // e.sender.send("addLocalMessageCallBack", {status:1, messageId:data.messageId})
    })
}

//设置当前选中的会话 同时清空未读数 
const onSetSessionSelect = () => {
    ipcMain.on("setSessionSelect", (e, {contactId, sessionId}) => {
        if(sessionId){
            store.setUserData("currentSssionId", sessionId)
            //清空未读数
            readAll(contactId)
        }else{
            store.deleteUserData("currentSssionId")
        }

    })
}

//删除好友时清空会话和消息
const onRemoveContact = () =>{
    ipcMain.on("removeContact", async(e, contactId) => {
        await delChatSessionCompletely(contactId)
        //获取session
        let list = await selectUserSessionList()
        if(list && list.length > 0){
            await delMessageCompletely(list[0].sessionId)
        }

        const dataList = await selectUserSessionList()
        e.sender.send("loadChatSessionDataCallBack", dataList)
    })

}

const onClearCurrentChatSession = () =>{
    ipcMain.on("clearCurrentChatSession", async(e) => {
        store.setUserData("currentSssionId", {})
    })
}


const onUpdateMessageStatus = () => {
    ipcMain.on("updateMessageStatus", async(e, data) => {
        await updateMessage({status:data.status}, {messageId:data.messageId})
    })
}



//图片上床交互
const onCreateCover = () => {
    ipcMain.on("createCover", async(e, filePath) => {
        const stream = await createCover(filePath)
        e.sender.send("createCoverBack",stream)
    })

}

//保存头像到本地 
const onSaveAvatar = () => {
    ipcMain.on("saveAvatar", async(e, data) => {
        if(data.type == 'group'){
            saveMyGroupAvatar2Local(data.avatarStream, data.coverStream, data.groupId)
        }else{
            saveUserAvatar2Local(data.avatarStream, data.coverStream)
        }
    })
    }



//监听创建新窗口
const onOpenNewWindow = () => {
    ipcMain.on("newWindow", (e, config) => {
        openWindow(config)
    })
}
const openWindow = ({
    windowId,
    title="PurpleChat",
    path,
    width=960,
    height=720,
    data
}) => {
    const serverPort = store.getUserData("localServerPort")
    data.serverPort = serverPort
    let newWindow = getWindow(windowId)
    if(!newWindow){
        
        newWindow = new BrowserWindow({
            icon: icon,
            width: width,
            height: height,
            fullscreenable: true,
            fullscreen: false,
            maximizable:true,
            autoHideMenuBar: true,
            transparent: false,
            frame: true,
            hasShadow: false,
            titleBarStyle: 'hidden',
            webPreferences: {
                preload: join(__dirname, '../preload/index.js'),
                sandbox: false,
                contextIsolation: false,
                webSecurity: false
            }
        })

        saveWindow(windowId, newWindow)
        newWindow.setMinimumSize(600, 400);

        if(is.dev && process.env['ELECTRON_RENDERER_URL']){
            newWindow.loadURL(`${process.env['ELECTRON_RENDERER_URL']}/index.html#${path}`)
        }
        else{
            newWindow.loadFile(join(__dirname, '../renderer/index.html'), {hash: `${path}`})
        }

        //如果是开发环境则打开开发者工具
        if(NODE_ENV === 'development'){
            newWindow.webContents.openDevTools()
        }

        //设置标题等元素
        newWindow.on('ready-to-show', () => {
            newWindow.setTitle(title)
            newWindow.show()
        })

        //将数据传递给窗口
        newWindow.once('show', ()=> {
            setTimeout(() => {
                newWindow.webContents.send("pageInitData", data)
            }, 500)
        }) //等待500ms后再显示窗口

        newWindow.on('closed', () => {
            deleteWindow(windowId)
        })
    } else {
        newWindow.show()
        newWindow.setSkipTaskbar(false)
        newWindow.webContents.send("pageInitData", data)
    }
}

const onSaveAs = () => {
    ipcMain.on("saveAs", (e, data) => {
        saveAs(data)
    })
}

const onSaveClipBoardFile = () => {
    ipcMain.on('saveClipBoardFile', async(e, data) => {
        const result = await saveClipBoardFile(data)
        e.sender.send("saveClipBoardFileCallBack", result)
    })
}

//未读消息
const onLoadContactApply = () => {
    ipcMain.on("loadContactApply", async(e, data) => {
        const result = await selectUserSettingInfo()
        let contactNoRead = 0

        if(result){
            contactNoRead = result.contactNoRead
        }
        e.sender.send("loadContactApplyCallBack", contactNoRead)
    })
}

const onReconnectTrue = () => {
    ipcMain.on("reconnectTrue", (e) => {
        reconnect(true)
    })
}




//清空消息
const onUpdateContactNoReadCount = () => {
    ipcMain.on("updateContactNoReadCount", async(e, data) => {
        await updateContactNoReadCount({userId:store.getUserId()})
    })
}

//强制下线
const onRelogin = (callback) => {
    ipcMain.on("relogin", (e, data) => {
        callback()
        e.sender.send("reloginCallBack")     
        //关掉ws
        closeWs()
        //关掉server
        closeLocalServer()

    })
}

//打开本地文件夹
const onOpenLocalFile = () => {
    ipcMain.on("openLocalFolder", (e) => {
        openLocalFolder()
    })
}

//获取文件夹
const onGetSysSetting = () => {
    ipcMain.on("getSysSetting", async(e) => {
        let result = await selectUserSettingInfo(store.getUserId())
        let sysSetting = result.sysSetting
        e.sender.send("getSysSettingCallBack",  sysSetting)

    })
}

//更新sys
const onUpdateUserSetting = () => {
    ipcMain.on("updateUserSetting", async(e, data) => {
        await updateUserSetting(data)
    })
}


//改变文件目录
const onChangeLocalFolder = () => {
    ipcMain.on("changeLocalFolder", (e, data) => {
        changeLocalFolder("copyingCallBack")
    })
}

const onReloadSession = () => {
    ipcMain.on("reloadSession", async(e, data) => {
        //更新数据库
        await updateStatus(data.contactId)
        //然后加载当前会话列表
        const sessionList = await selectUserSessionList()
        const contactId = data.contactId
        e.sender.send("reloadSessionCallBack", {contactId, sessionList})
    })
}

//关于更新
const onOpenUrl = () => {
    ipcMain.on("openUrl", (e, url) => {
        shell.openExternal(url)
    })
}

const onDownloadUpdate = () => {
    ipcMain.on("downloadUpdate", (e, {id ,fileName}) => {
        downloadUpdate(id, fileName)
    })
}

const onDownloadFile = () => {
    ipcMain.on("downloadFile", async(e, {fileId}) => {
        let savePath = await getLocalFilePath('chat', false, fileId)
        downloadFile(fileId, savePath, "file", false)
    })
}

//平行组件之间的中转站 通过回调函数实现
const onTransferStation = () => {
    ipcMain.on("transferStation", (e, type) => {
        switch(type){
            case "RELOAD_SESSION":
                e.sender.send("refreshChatSession")
                break
            case "RELOAD_CONTACT":
                e.sender.send("contactMessage", "RELOAD_GROUP")
                break
            case "RELPAD_APPLY":
                e.sender.send("reloadApply")
            case "RELPAD_APPLY":
                e.sender.send("reloadApply")
            
        }
    })
}


//点击头像即可立即获取最新的头像
const onForceGetAvatar = () => {
    ipcMain.on("forceGetAvatar", async(e, data) => {
        forctGetAvatar(data)
    })
}

const onFoceGetChatImage = () => {
    ipcMain.on("forceGetChatImage", async(e, data) => {
        forceGetChatImage(data)
    })
}

const forctGetAvatar = async(data) => {
    const localPath = await getLocalFilePath('avatar', false, data.fileId)
    const coverPath = utils.removeFileExtension(localPath) + cover_image_suffix;
    await downloadFile(data.fileId, coverPath, "avatar", true)
    getWindow('main').webContents.send("forceGetAvatarCallBack", data.fileId)
    downloadFile(data.fileId, localPath, "avatar", false)
}

const forceGetChatImage = async(data) => {
    const localPath = utils.ensureDoubleBackslashes(await getLocalFilePath('chat', true, data.fileId))
    await downloadFile(data.fileId, localPath, "chat", true)
    await downloadFile(data.fileId, localPath, "chat", false)
    console.log("forceGetChatImageCallBack", data.fileId)
    getWindow('main').webContents.send("forceGetChatImageCallBack", data.fileId)
}
//直接完全关闭软件
const onExit = () => {
    ipcMain.on("exit", (e) => {
        app.exit()
    })
}

const onReloadOnLine = () => {
    ipcMain.on("reloadOnline", (e, fileId) => {
        console.log("reloadOnLine")
        reloadOnline(fileId)
    })
}


export { 
    onLoginOrRegister,
    onLoginSuccess,
    winTitleOp,
    onSetLocalStore,
    onGetLocalStore,
    onLoadChatSession,
    onDelChatSession,
    onTopChatSession,
    onloadChatMessageData,
    onAddLocalMessage,
    onSetSessionSelect,
    onCreateCover,
    onOpenNewWindow,
    onSaveAs,
    onSaveClipBoardFile,
    onLoadContactApply,
    onUpdateContactNoReadCount,
    onRelogin,
    onOpenLocalFile,
    onGetSysSetting,
    onChangeLocalFolder,
    onReloadSession,
    onOpenUrl,
    onDownloadUpdate,
    onDownloadFile,
    onLoadLocalUser,
    openWindow,
    onStartTempLocalServer,
    onSaveAvatar,
    onReconnectTrue,
    onRemoveContact,
    onAddAvatar2Local,
    onClearCurrentChatSession,
    onTransferStation,
    onUpdateMessageStatus,
    onForceGetAvatar,
    forceGetChatImage,
    onDeleteUserSetting,
    forctGetAvatar,
    onExit,
    onFoceGetChatImage,
    onReloadOnLine,
    onUpdateUserSetting
}