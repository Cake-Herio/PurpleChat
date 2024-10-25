<template>
    <div class="send-panel">
        <div class="toolbar">
            <!-- emoji -->
            <el-popover :visible="showEmojiPopover" trigger="click" placement="top" :teleported="false"
                @show="openPopover" @hide="closePopover" ref="emojiPopoverRef"
                :popper-style="{ padding: '0px 10px 10px 10px', width: '490px' }">
                <template #default>
                    <el-tabs v-model="activeEmoji" @click.stop>
                        <el-tab-pane :label="emoji.name" :name="emoji.name" v-for="emoji in emojiList"
                            :key="emoji.name">
                            <div class="emoji-list">
                                <div class="emoji-item" v-for="item in emoji.emojiList" @click="sendEmoji(item)"
                                    :key="item.name">
                                    {{ item }}
                                </div>
                            </div>
                        </el-tab-pane>
                    </el-tabs>

                </template>
                <template #reference>
                    <div class="iconfont icon-emoji" @click="showEmojiPopoverHandler" ref="emojiTriggerRef"></div>
                </template>
            </el-popover>


            <!-- 文件上传 -->
            <el-upload ref="uploadRef" name="file" :show-file-list="false" :multiple="true" :limit="FILE_LIMIT"
                :http-request="uploadFile" :on-exceed="uploadExceed">
                <div class="iconfont icon-folder"></div>
            </el-upload>


        </div>
        <div class="input-area" @drop="dropHandler" @dragover="dragOverHandler">
            <el-input rows="5" v-model="msgContent" type="textarea" spellcheck="false" class="custom-input"
                show-word-limit  @keydown.enter="sendMessage" @paste="pasteFile"></el-input>
        </div>

        <div class="send-btn-panel">
            <el-popover trigger="click" :visible="showSendMsgPopover" :hide-after="1500" placement="top-end"
                :teleported="false" @show="openPopover" @hide="closePopover" :popper-style="{
                    padding: '5px',
                    'min-width': '0px',
                    width: '120px'
                }">
                <template #default>
                    <span class="empty-msg">不能发送空白信息</span>
                </template>
                <template #reference>
                    <span class="send-btn" @click="sendMessage">发送(S)</span>
                </template>
            </el-popover>
        </div>




    </div>
</template>

<script setup>
import emojiList from "@/utils/Emoji"
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
import { useUserInfoStore } from "@/stores/UserInfoStore"
const userInfoStore = useUserInfoStore()
import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore();



const route = useRoute()
const router = useRouter()
const showSendMsgPopover = ref(false)
const showEmojiPopover = ref(false)
const emojiTriggerRef = ref(null)
const emojiPopoverRef = ref(null)
const activeEmoji = ref("smile")

const openPopover = () => {
}
const closePopover = () => {
}


const hidePopover = () => {
    showEmojiPopover.value = false
    showSendMsgPopover.value = false
}
const props = defineProps({
    currentChatSession: {
        type: Object,
        default: {}
    }
})
const sendEmoji = (emoji) => {
    //添加到输入框
    msgContent.value += emoji
    //然后关闭emoji面板
    showEmojiPopover.value = false
}
const showEmojiPopoverHandler = () => {
    showEmojiPopover.value = !showEmojiPopover.value
    if (showEmojiPopover.value) {

        document.addEventListener('click', handleClickOutside);
    }
}

const handleClickOutside = (event) => {
    if (
        emojiPopoverRef.value &&
        !emojiPopoverRef.value.$el.contains(event.target) &&
        emojiTriggerRef.value &&
        !emojiTriggerRef.value.contains(event.target)
    ) {
        hidePopover()
        document.removeEventListener('click', handleClickOutside);
    }
};



const msgContent = ref("")

const sendMessage = (e) => {
    if (e.shiftKey && e.keyCode == 13) {
        return
    }
    e.preventDefault();
    if (msgContent.value.trim() == "") {
        showSendMsgPopover.value = true
        setTimeout(() => {
            showSendMsgPopover.value = false
        }, 1500);
        return
    }
    const messageContent = msgContent.value.trim()

    sendMessageDo({
        messageContent,
        messageType: 2
    }, true)


}

const sendMessageDo = async (
    messageObj = {
        messageContent,
        messageType,
        localFilePath,
        fileSize,
        filePath,
        fileName,
        fileType
    },
    cleanMsgContent) => {
    if (messageObj.fileSize == 0) {
        proxy.Confirm({
            message: `${messageObj.fileName}文件为空，请重新选择`,
            showCancelButton: false,

        })
        return
    }

    //判断文件大小
    if (!checkFileSize(messageObj.fileType, messageObj.fileName, messageObj.fileSize)) {
        return
    }
    //判断文件数量


    messageObj.sessionId = props.currentChatSession.sessionId
    messageObj.sendUserId = userInfoStore.getInfo().userId
    messageObj.sendTime = new Date().getTime()
    messageObj.userId = userInfoStore.getInfo().userId
    messageObj.status = 0

    //为messageObj起一个临时的id
    

    //本地先渲染 如果是媒体文件需要等到得到messageid后再渲染
    if(messageObj.messageType != 5){
        emit("sendMessage2Local", messageObj)
    }else{
        if(messageObj.fileType != 2){
            messageObj.status = -3  
        }
            
    }
    
    
    if (cleanMsgContent) {
        msgContent.value = ""
    }
    let currentMessageStatus = null
    //然后发送消息
    let result = await proxy.Request({
        url: proxy.Api.sendMessage,
        params: {
            messageContent: messageObj.messageContent,
            messageType: messageObj.messageType,
            contactId: props.currentChatSession.contactId,
            fileSize: messageObj.fileSize,
            fileName: messageObj.fileName,
            fileType: messageObj.fileType,
            status: messageObj.status,
        },
        showLoading: false,
        showError: false,
        errorCallback: (responseData) => {
            //当发送失败时 需要调用callback 提示重新申请 （如果对方已经把你删除或者拉黑
            messageObj.status = -1
            currentMessageStatus = -1
            window.ipcRenderer.send("addLocalMessage", messageObj)
            proxy.Confirm({
                message: responseData.info,
                okfun: () => {
                    //重新渲染本地信息
                    addContact(props.currentChatSession.contactId, props.currentChatSession.contactType)
                },
                okText: "重新申请",
            })
            return
        }
    })
    if (!result) {
        //网络异常的情况处理
        if (currentMessageStatus == null) {
            console.log("网络异常")
            messageObj.status = -1
            emit("sendMessage2Local", messageObj)
            return;
        }
    }
    if(result){
        //把得到的值传给messageObj
        Object.assign(messageObj, result.data)

        //如果是文字 更改status
        if(messageObj.messageType == 2){
            messageObj.status = 1
            emit("reloadMessageStatus", messageObj)
        }

        if(messageObj.messageType == 5){
            //如果是媒体文件 需要等到得到messageid后再渲染
            if(messageObj.fileType != 2){
                messageObj.status = -3
            }
            dynamicStore.setStatus('MEDIA_LOADED')
            emit("sendMessage2Local", messageObj)
        }
        //渲染发送的消息 
        //发送信息给服务端 存储消息到数据库
        window.ipcRenderer.send("addLocalMessage", messageObj)
    }
    
}

const emit = defineEmits(["sendMessage2Local", "reloadMessageStatus"])


const addContact = async (contactId, contactType) => {

    let result = await proxy.Request({
        url: proxy.Api.applyAdd,
        params: {
            contactId,
            contactType,
            applyInfo: contactType == 0 ? `${userInfoStore.getInfo().nickName}重新申请你为好友` : `${userInfoStore.getInfo().nickName}重新申请加入群聊`
        }
    })
    if (!result) {
        return;
    }

    if (result.data == 0) {
        proxy.Message.success("添加成功")
    }
    else {
        proxy.Message.success("申请已提交")
    }
}

//多媒体部分
import { getFileType, FILE_TYPE_NAME } from "../../utils/Constants";
const uploadRef = ref()
const uploadFile = (file) => {
    uploadFileDo(file.file)
    //清除
    uploadRef.value.clearFiles()

}



const getFileTypeByName = (filename) => {
    const fileSuffix = filename.substr(filename.lastIndexOf(".") + 1)
    return getFileType(fileSuffix)
}


const uploadFileDo = (file) => {

    const fileType = getFileTypeByName(file.name)
    sendMessageDo({
        messageContent: "[" + FILE_TYPE_NAME[fileType] + "]",
        messageType: 5,
        fileSize: file.size,
        filePath: file.path,
        fileName: file.name,
        fileType
    }, false)

}
import { useSysSettingStore } from "../../stores/SysSettingStore";
const sysSettingStore = useSysSettingStore()



const checkFileSize = (fileType, fileName, fileSize) => {
    const SIZE_MB = 1024 * 1024
    const sysSettingInfo = sysSettingStore.getSysSetting()
    const settingArray = Object.values(sysSettingInfo)
    const fileSizeNumber = settingArray[fileType] * SIZE_MB
    if (fileSize > fileSizeNumber * SIZE_MB) {
        proxy.Confirm({
            message: `${fileName}文件大小超过${settingArray[fileType]}MB，请重新选择`,
            showCancelBtn: false,
        })

        return false
    }
    return true
}
import { FILE_LIMIT } from "../../utils/Constants";
const checkFileLimit = (files) => {
    if (files.length > FILE_LIMIT) {
        proxy.Confirm({
            message: `最多只能上传${FILE_LIMIT}个文件`,
            showCancelBtn: false,
        })
        return false
    }
    return true
}


const uploadExceed = (fileList) => {
    checkFileLimit(fileList)
}

//拖入文件
const dragOverHandler = (e) => {
    e.preventDefault()
}

const dropHandler = (event) => {
    event.preventDefault();
    let files = event.dataTransfer.files
    if (!checkFileLimit(files)) {
        return
    }
    for (let i = 0; i < files.length; i++) {
        uploadFileDo(files[i])
    }
}

//粘贴文件
const pasteFile = async (event) => {

    let items = event.clipboardData && event.clipboardData.items //这里是条件链式判断 先判断event.clipboardData是否存在 然后才返回
    const fileData = {}
    for (const item of items) {
        if (item.kind != 'file') {
            break
        } else {
            const file = await item.getAsFile()
            if (!file.path == '') {
                uploadFileDo(file)
            } else {
                const imageFile = new File([file], 'temp.jpg')
                let fileReader = new FileReader()
                fileReader.onloadend = function () {
                    const byteArray = new Uint8Array(this.result)
                    fileData.byteArray = byteArray
                    fileData.name = imageFile.name
                    window.ipcRenderer.send('saveClipBoardFile', fileData)
                }
                fileReader.readAsArrayBuffer(imageFile)
            }

        }
    }

}

const onSaveClipBoardFileCallback = () => {
    window.ipcRenderer.on('saveClipBoardFileCallBack', (e, file) => {
        let fileType = 0 //截图文件一定为图片
        sendMessageDo({
            messageContent: '[图片]',
            messageType: 5,
            fileSize: file.size,
            fileName: file.name,
            filePath: file.path,
            fileType: fileType
        }, false)
    })
}


onMounted(() => {
    onSaveClipBoardFileCallback()
})
//去除监听
onUnmounted(() => {
    window.ipcRenderer.removeAllListeners('saveClipBoardFileCallBack')
})








</script>

<style lang="scss" scoped>
.emoji-list {
    .emoji-item {
        float: left;
        font-size: 23px;
        padding: 2px;
        text-align: center;
        border-radius: 3px;
        margin-left: 10px;
        margin-top: 5px;
        cursor: pointer;

        &:hover {
            background: #f6f1fe;
        }
    }
}

.send-panel {
    height: 200px;
    border-top: 1px solid #ddd;




    .toolbar {
        height: 40px;
        display: flex;
        align-items: center;
        padding-left: 10px;


        .iconfont {
            color: #494949;
            font-size: 20px;
            margin-left: 10px;
            cursor: pointer;
        }

        :deep(.el-tabs__header) {
            margin-bottom: 0px;
        }

    }


    .input-area {
        padding: 0px 10px;
        outline: none;
        width: 100%;
        height: 115px;
        overflow: auto;
        word-wrap: break-word;
        word-break: break-all;

        :deep(.el-textarea__inner) {
            box-shadow: none;
            background: #f5f5f5;
        }

        :deep(.el-input__count) {
            background: none;
            right: 12px;
        }
    }



    .send-btn-panel {
        text-align: right;
        padding-top: 10px;
        margin-right: 22px;

        .send-btn {
            cursor: pointer;
            color: #ad73f9;
            background: #e9e9e9;
            border-radius: 5px;
            padding: 8px 25px;

            &:hover {
                background: #d2d2d2;
            }
        }

        .empty-msg {
            font-size: 13px
        }
    }
}
</style>
