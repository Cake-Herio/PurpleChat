import {
  createTable,
  run,
  queryAll,
  queryOne,
  queryCount,
  insertOrReplace,
  insertOrIgnore,
  insert,
  update
} from "./ADB";

import store from "../store/";



const saveOrUpdateChatSessionBatch4Init = async (chatSessionList) => {
  try {
    for (let i = 0; i < chatSessionList.length; i++) {
      const sessionInfo = chatSessionList[i];

      //根据主键userId和sessionId联合查找判断是否存在 存在则更新 不存在则插入
      const userSessionInfo = await selectUserSessionByContactId(chatSessionList[i].contactId);
      if (userSessionInfo) {
        await updateChatSession(sessionInfo);
      } else {
        sessionInfo.status = 1; //设为可见
        await addChatSession(sessionInfo);
      }
    }
  } catch (error) {
    console.error('Error processing chat sessions:', error);
    throw error; // 重新抛出错误以确保调用者可以处理
  }
}



const addChatSession = async (sessionInfo) => {
  sessionInfo.userId = store.getUserId();
  insertOrIgnore("chat_session_user", sessionInfo);
}

const updateChatSession = async (sessionInfo) => {
  const paramDate = {
    userId: store.getUserId(),
    contactId: sessionInfo.contactId
  }
  //更新的数据不允许是userid 和 contactID
  const updateInfo = Object.assign({}, sessionInfo);
  updateInfo.userId = null
  updateInfo.contactId = null
  update("chat_session_user", updateInfo, paramDate);
}


//更新未读数
const updateNoReadCount = async (contactId, noReadCount) => {
  //!如果先查询后更新可能有并发问题
  let sql = "update chat_session_user set no_read_count = no_read_count + ? where user_Id = ? and contact_id = ?";
  return await run(sql, [noReadCount, store.getUserId(), contactId]);
}

//根据UserId查询会话
const selectUserSessionList = async () => {
  let sql = `select * from chat_session_user where user_Id = ? and status = 1`;
  return await queryAll(sql, [store.getUserId()]);

}


const delChatSession = async (contactId) => {
  let data = {
    status: 0
  }
  let paramData = {
    userId: store.getUserId(),
    contactId
  }
  await update("chat_session_user", data, paramData);
  

}





const delChatSessionCompletely = async (contactId) => {
  let sql = `delete from chat_session_user where user_Id = ? and contact_id = ?`
  return await run(sql, [store.getUserId(), contactId]);
}



const topChatSession = async (contactId, topType) => {
  let data = {
    topType
  }
  let paramData = {
    userId: store.getUserId(),
    contactId
  }
  return await update("chat_session_user", data, paramData);
}

//更新会话状态总的函数调用
const updataSessionInfoMessage = async (currentSessionId, {
  sessionId,
  contactName,
  lastMessage,
  lastReceiveTime,
  contactId,
  memberCount,
  contactType
}) => {
  if (!sessionId || !contactId) {
    console.error('Session ID or Contact ID is missing.');
    return;
  }

  try {
    if (!contactType) {
      contactType = contactId[0] === 'G' ? 1 : 0;
    }
    
    const params = [
      store.getUserId(),
      sessionId,
      contactId,
      lastMessage,
      contactType,
      lastReceiveTime,
      contactName || '',
      memberCount || 0
    ];

    let sql = `
      INSERT INTO chat_session_user (user_Id, session_id, contact_id, last_message, contact_type, last_receive_time, status, no_read_count, contact_name, member_count)
      VALUES (?, ?, ?, ?, ?, ?, 1, 1, ?, ?)
      ON CONFLICT(user_Id, contact_id)
      DO UPDATE SET 
        last_message = excluded.last_message,
        last_receive_time = excluded.last_receive_time,
        status = 1,
        contact_type = excluded.contact_type
    `;

    if (contactName) {
      sql += `, contact_name = excluded.contact_name`;
    }
    if (memberCount) {
      sql += `, member_count = excluded.member_count`;
    }

    if (currentSessionId !== sessionId) {
      sql += `, no_read_count = chat_session_user.no_read_count + 1`;
    }

    return await run(sql, params);
  } catch (error) {
    console.error('Error updating session info:', error);
    throw error;
  }
};


//清空contactId所指向的未读数
const readAll = async (contactId) => {
  let sql = `update chat_session_user set no_read_count = 0 where user_Id = ? and contact_id = ?`
  return await run(sql, [store.getUserId(), contactId]);
}

//更新群昵称
const updateGroupName = async (contactId, groupName) => {
  let sql = `update chat_session_user set contact_name = ? where user_Id = ? and contact_id = ?`
  return await run(sql, [groupName, store.getUserId(), contactId]);
}













const selectUserSessionByContactId = async (contactId) => {
  const sql = `select * from chat_session_user where user_Id = ? and contact_id = ?`;
  return await queryOne(sql, [store.getUserId(), contactId]);
}



const updateStatus = async (contactId) => {
  let sql = `update chat_session_user set status = 1 where user_Id = ? and contact_id = ?`
  return await run(sql, [store.getUserId(), contactId]);

}

export {
  saveOrUpdateChatSessionBatch4Init,
  updateNoReadCount,
  selectUserSessionList,
  delChatSession,
  topChatSession,
  updataSessionInfoMessage,
  readAll,
  selectUserSessionByContactId,
  updateGroupName,
  updateStatus,
  delChatSessionCompletely
}
