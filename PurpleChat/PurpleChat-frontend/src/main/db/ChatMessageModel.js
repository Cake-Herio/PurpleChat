import { 
    createTable,
    run,
    queryAll,
    queryOne,
    queryCount,
    insertOrReplace,
    insertOrIgnore,
    insert,
    update} from "./ADB";
import {updateNoReadCount} from "./ChatSessionUserModel";
import store from "../store";


const chatSessionCountMap = {}
const saveMessageBatch = async (chatMessageList) => { 
    
    //建立映射关系 获取各个session的未读数
    chatMessageList.forEach((message) => {
        let contactId = message.contactType === 1? message.contactId : message.sendUserId
        let noReadCount = chatSessionCountMap[contactId]

        if(!noReadCount){
            chatSessionCountMap[contactId] = 1
        }else{
            chatSessionCountMap[contactId] = noReadCount + 1
        }
    })
    //打印map
    let totalNoReadCount = chatMessageList.length

    //更新session的未读数
    for(let contactId in chatSessionCountMap){
        let noReadCount = chatSessionCountMap[contactId]
        await updateNoReadCount(contactId, noReadCount)
    }
    

    //然后插入消息
    chatMessageList.forEach(async (message) => {
        await saveMessage(message)
    }) //对于上面这个操作 是通过异步 因此不会阻塞 但是saveMessage(message这个操作是一个一个执行的

    return totalNoReadCount
}

const saveMessage = async (message) => {
    message.userId = store.getUserId()

    if(message.messageId === null || message.messageId === undefined){
        //获取当前最大的messageId
        let sql = `select max(message_id) as maxMessageId from chat_message where session_id = ? and user_id = ?`
        const maxMessageId = await queryOne(sql, [message.sessionId, message.userId])
        if(maxMessageId){
            message.messageId = maxMessageId.maxMessageId + 1
        }
    }
    await insertOrReplace("chat_message", message)
    return message.messageId
}

const updateMessage = async (data, paramData) => {
    paramData.userId = store.getUserId()
    update("chat_message", data, paramData)
}

const delMessageCompletely = async (contactId) => {
    let sql = `delete from chat_message where user_Id = ? and session_id = ?`
    return await run(sql, [store.getUserId(), contactId]);
}

//将数据库message文件为0的我发给别人全部改为-2
const initUpdateMessageStatus = async () => {
    let sql = `update chat_message set status = -2 where user_id = ? and status = 0 and send_user_id = ?`
    await run(sql, [store.getUserId(), store.getUserId()]);
    sql = `update chat_message set status = -2 where user_id = ? and status = 2 and send_user_id != ?`
    await run(sql, [store.getUserId(), store.getUserId()]);

}




const selectChatMessageList = async (sessionId, pageNo, maxMessageId) => {  
    //查当前总记录数
    let sql = null
    let userId =  store.getUserId()
    sql = `select count(1) as count from chat_message where session_id = ? and user_id = ?`
    const count = await queryCount(sql, [sessionId, userId])
    //处理数据
    const {pageTotal,offset,limit} = getPageOffset(pageNo, count)

    const params = [sessionId, userId]
    sql = `select * from chat_message where session_id = ? and user_id = ?`
    if(maxMessageId) {
        sql += ` and message_id <= ?`
        params.push(maxMessageId)
    }
    sql += ` order by message_id desc limit ?,?`
    params.push(offset)
    params.push(limit)
    const dataList = await queryAll(sql, params)
    return {dataList, pageTotal, pageNo}

}


//需要userid messageid  messageid不止
const selectMessage = async(messageId) => {  
    let sql = `select * from chat_message where message_id =? and user_id = ?`
    let userId = store.getUserId()
    return await queryOne(sql, [messageId, userId])
}





const getPageOffset = (pageNo, totalCount) => {

    pageNo<1? pageNo = 1 : null

    const pageSize = 20
    const pageTotal = Math.ceil(totalCount / pageSize)
    const offset = (pageNo - 1) * pageSize
    return {
        pageTotal,
        offset,
        limit: pageSize
    }
}


export{
    saveMessageBatch,
    selectChatMessageList,
    selectMessage,
    saveMessage,
    updateMessage,
    delMessageCompletely,
    initUpdateMessageStatus
}