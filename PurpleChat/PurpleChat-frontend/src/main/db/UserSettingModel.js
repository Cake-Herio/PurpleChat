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
const fs = require('fs');
const os = require('os');
const fse = require('fs-extra');
const path = require('path');
//读本地文件缓存目录
const userDir = os.homedir()
const NODE_ENV = process.env.NODE_ENV;
//根据环境创建不同名称的文件夹
const fileFolder = path.join(userDir, NODE_ENV === "development" ? ".PurpleChatFileTest" : path.join(".PurpleChat", "File"));
import store from "../store";
import { startLocalServer, closeLocalServer, saveDefautLocalUserAvatar } from "../file";

if (!fs.existsSync(fileFolder)) {
    fse.ensureDirSync(fileFolder);
}


//这个表是为了保证一个设备有多个用户时，每个用户分开存储数据
//更新好友申请未读数
const updateContactNoReadCount = async ({userId, noReadCount}) => {
    let sql = null
    if(noReadCount == 0){
        return
    }

    if(noReadCount){
        sql = "update user_setting set contact_no_read = contact_no_read + ? where user_Id = ?"
    }else{
        noReadCount = 0
        sql = "update user_setting set contact_no_read = ? where user_Id = ?"
    }

    return await run(sql, [noReadCount, userId])
}

//添加设别用户于本地
const addUserSetting = async (userId, email, token, nickName) => {

    //拿到最大的端口号 方便为后续新进来的用户分配端口号
    let sql = "select max(server_port) server_port from user_setting"
    let {serverPort} = await queryOne(sql, [])  //为什么是{} 因为queryOne返回的是一个对象 通过解构赋值给serverPort
    if(!serverPort){
        serverPort = 50711
    }else{
        serverPort++
    }
    let sysSetting = {
        localFileFolder: fileFolder
    }
    //判断是否表中已经存在该用户 通过userId查询
    sql = "select * from user_setting where user_id = ?"

    let resultServerPort = null
    let localFileFolder = null
    let userInfo = await queryOne(sql, [userId])
    if(userInfo){
        resultServerPort = userInfo.serverPort
        //sysSetting是以字符串的形式存储的 需要转json.parse 即解析成对象
        localFileFolder = path.join(JSON.parse(userInfo.sysSetting).localFileFolder, userId)
        serverPort = resultServerPort
    }else{
        await insertOrIgnore("user_setting", {
            userId,
            email,
            sysSetting: JSON.stringify(sysSetting),
            contactNoRead: 0,
            serverPort,
            nickName
        })
        localFileFolder = path.join(fileFolder, userId)
        

    }



    saveUserToken(token)
    //启动本地服务
    //将port等信息存储到本地内存中
    startLocalServer(serverPort)
    store.setUserData("localServerPort", serverPort)
    store.setUserData("localFileFolder", localFileFolder)
}

//根据userId查询用户设置
const selectUserSettingInfo = async () => {
    let sql = "select * from user_setting where user_id = ?"
    let userId = store.getUserId()
    return await queryOne(sql, [userId])
}

//更新用户设置
const updateUserSetting = async (data) => {
    const paramsData = {
        userId: store.getUserId()
    }

    return await update("user_setting", data, paramsData)

}

//在必要的时候删除本地用户

const deleteUserSetting = async (userId) => {
    let sql = "delete from user_setting where user_id = ?"
    return await run(sql, [userId])
}



const loadLocalUser = async () => {
    let sql = "select * from user_setting where email is not null"
    const loadList = await queryAll(sql, [])
    //分别查看各个用户是否过期 通过token_expire字段 如果过期不添加到list
    let list = []
    for(let i = 0; i < loadList.length; i++){
        let item = loadList[i]
        if(item.tokenExpiry && new Date(item.tokenExpiry).getTime() < new Date().getTime()){
            continue
        }
        list.push(item)
    }

    return list
}

const saveUserToken = async (token) => {
    //更新用户token 设置过期时间为一天后
    let sql = "update user_setting set token = ?, token_expiry = datetime('now', '+1 day') where user_id = ?"


    return await run(sql, [token, store.getUserId()])
}



export {
    updateContactNoReadCount,
    addUserSetting,
    selectUserSettingInfo,
    updateUserSetting,
    loadLocalUser,
    saveUserToken,
    deleteUserSetting,
}