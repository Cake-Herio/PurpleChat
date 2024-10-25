const fs = require('fs');
const fse = require('fs-extra');
const NODE_ENV = process.env.NODE_ENV;
const path = require('path');
const { app, ipcMain, shell } = require('electron');
const { exec } = require('child_process');
const FormData = require('form-data'); // 引入FormData模块（用于构建表单数据）
const axios = require('axios'); // 引入axios库
import store from "./store";
import {selectChatMessageList, saveMessage, updateMessage, selectMessage}  from "./db/ChatMessageModel"
const moment = require('moment');
moment.locale('zh-cn', {});
import utils from "./utils";
const { dialog } = require('electron');
import { selectUserSettingInfo, updateUserSetting } from './db/UserSettingModel';
import { getWindow } from "./windowProxy";
import { onDownloadUpdate } from "./ipc";
import Constants from "./Constants.js"
import someConfig from '../../package.json'
import { ElStep } from "element-plus";
const cover_image_suffix = "_cover.png";
const image_suffix = ".png";

const ffprobePath = "/assets/ffprobe.exe";
const ffmpegPath = "/assets/ffmpeg.exe";

const https = require('https');



const getResoucesPath = () => {
    let resourcesPath = app.getAppPath() 
    if(NODE_ENV !== "development"){
        resourcesPath = path.join(path.dirname(app.getPath("exe")), "resources");
    }
    return resourcesPath
}
const getffprobePath = () => {
    return `"${path.join(getResoucesPath(), ffprobePath)}"`;
}
const getffmpegPath = () => {
    return `"${path.join(getResoucesPath(), ffmpegPath)}"`;
}

const executeCommand = (command) => {
    return new Promise((resolve, reject) => {
        exec(command, (error, stdout, stderr) => {
            if (error) {
                console.error(`exec error: ${error}`);
                reject(error);
                return;
            }
            if (stderr) {
                console.warn(`Stderr: ${stderr}`);
            }
            resolve(stdout);
        });
    });
};

const getDomain = () => {
    return NODE_ENV === 'production' ? store.getData("prodDomain") : store.getData("devDomain");
}


const addAvatar2Local = async(filePath, coverPath) =>{
    if(store.getUserId() == null){
        return
    }
    let savePath = await getLocalFilePath("avatar",false, store.getUserId())
    fs.copyFileSync(filePath, savePath)
    let saveCoverPath = utils.removeFileExtension(savePath) + cover_image_suffix
    fs.copyFileSync(coverPath, saveCoverPath)
}





const addFlie2Local = async(messageId, filePath, fileType, key) => {
    try {
        console.log("fileTpye" + fileType)
        if(fileType != 2){
            let savePath = await getLocalFilePath("chat",false, messageId);
            let coverPath = utils.removeFileExtension(savePath) + cover_image_suffix;
            let command = `${getffmpegPath()} -i "${filePath}" -y -vframes 1 -vf "scale='min(170,iw*min(170/iw,170/ih))':'min(170,ih*min(170/iw,170/ih))':flags=bicubic" "${coverPath}"`;
            await executeCommand(command);
            
            await updateMessage({ status: 0 }, { messageId });

            if(!fs.existsSync(coverPath)){
                console.log("文件不存在" + coverPath)
            }


            fs.copyFileSync(filePath, savePath);
            //视频转码
            command = `${getffprobePath()} -v error -select_streams v:0 -show_entries stream=codec-name "${filePath}"`;
            let result = await executeCommand(command);
            if(result.includes("hevc")){
                command = `${getffmpegPath()} -y -i "${filePath}" -c:v libx264 -crf 20 "${savePath}"`;
                await executeCommand(command);
            }
            uploadCoverFile(messageId, coverPath)
            await uploadFile(messageId, savePath, null, fileType);
        } else {
            await uploadFile(messageId, filePath, null, fileType);
        }
    } catch (error) {
        console.error("Error in addFlie2Local: ", error);
    }
    

}
const agent = new https.Agent({
    rejectUnauthorized: false,
    keepAlive: true,
});


const uploadCoverFile = async(messageId, coverPath) => {
    try{
        const formdata = new FormData();
        formdata.append("messageId", messageId);
        formdata.append('file', fs.createReadStream(coverPath));
        const url = `${getDomain()}/api/chat/uploadCoverFile`;
        const token = store.getUserData("token");
        const config = {
            headers: {
                "token": token,
                "Content-Type": "multipart/form-data"
            },
            timeout: 0,
            httpsAgent: agent,
        };
        getWindow('main').webContents.send("uploadFileCallBack", { messageId, status: 0});
        await axios.post(url, formdata, config);
    }catch (error) {
        console.error('捕获的错误对象:', error); 
        if (error.response) {
            // 服务器返回的错误响应
            console.error('服务器响应错误:', error.response.status, error.response.data);
        } else if (error.code === 'ECONNABORTED') {
            // 请求超时错误
            console.error('请求超时:', error.message);
        } else {
            // 其他错误，比如网络问题或配置错误
            console.error('上传文件时发生错误:', error.message);
        }
        // 捕获所有未处理的错误
        console.error('上传文件时发生错误:', error);
        
        getWindow('main').webContents.send("uploadFileCallBack", { messageId, status: -2 , fileType: 0});
        updateMessage({ status: -2 }, { messageId });
    }
    
}



const uploadFile = async(messageId, filePath, coverPath, fileType) => {
    try {
        const formdata = new FormData();
        formdata.append("messageId", messageId);
        formdata.append('file', fs.createReadStream(filePath));
        if (coverPath) {
            formdata.append('cover', fs.createReadStream(coverPath));
        }
        console.log("上传文件path："+ filePath)
        const url = `${getDomain()}/api/chat/uploadFile`;
        const token = store.getUserData("token");
        let timer = null;
        let progress = null
        let fileSize = fs.statSync(filePath).size
        let fileSizeMB = fileSize / (1024 * 1024); // 转换为MB
        let factor =  fileSizeMB > 200? 200/(fileSizeMB * 6) : 0.15
        const config = {
            headers: {
                "token": token,
                "Content-Type": "multipart/form-data"
            },
            onUploadProgress: (progressEvent) => {
                    const complete = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                    // 如果进度超过60，开始人为干涉
                    console.log("complete:", complete)
                    if (complete >= 60) {
                        if (!timer) {
                            timer = setInterval(() => {
                                progress += (100 - progress) * factor;  // 每次增加剩余进度的20%
                                if (progress > 98 && progress < 99.97) {
                                    progress += (100 - progress) * 0.002;
                                }else if(progress > 99.97){
                                    progress = 99.97
                                    clearInterval(timer);
                                    timer = null;
                                }
                                console.log("发送progress:", progress)
                                getWindow('main').webContents.send("uploadProgress", { messageId, progress: progress });
                            }, 1000); // 每秒更新一次进度
                        }
                    } else {
                        // 进度小于60时，按照axios的进度计算
                        progress = complete;
                        console.log("发送progress:", progress)
                        getWindow('main').webContents.send("uploadProgress", { messageId, progress: progress });
                    }
                
            },
            maxContentLength: Infinity,
            maxBodyLength: Infinity,
            timeout: 0,
            httpsAgent: agent,
        };

        const response = await axios.post(url, formdata, config);
        clearInterval(timer);
        timer = null;
        console.log("上传文件成功:", response.data);
        getWindow('main').webContents.send("uploadProgress", { messageId, progress: 100 });
        updateMessage({ status: 1 }, { messageId });

    } catch (error) {
        clearInterval(timer); 
        console.error('捕获的错误对象:', error); 
        if (error.response) {
            // 服务器返回的错误响应
            console.error('服务器响应错误:', error.response.status, error.response.data);
        } else if (error.code === 'ECONNABORTED') {
            // 请求超时错误
            console.error('请求超时:', error.message);
        } else {
            // 其他错误，比如网络问题或配置错误
            console.error('上传文件时发生错误:', error.message);
        }
        // 捕获所有未处理的错误
        console.error('上传文件时发生错误:', error);
        getWindow('main').webContents.send("uploadFileCallBack", { messageId, status: -2 });
        updateMessage({ status: -2 }, { messageId });
    }
}



const getLocalFilePath = async (partType, showCover, fileId) => {
    try {
        let localFolder = store.getUserData("localFileFolder");
        let localPath = null;
        switch (partType) {
            case "avatar":
                if (!fs.existsSync(localFolder)) {
                    fse.ensureDirSync(localFolder); // 使用 fs-extra 的 ensureDirSync
                }
                localPath = path.join(localFolder, "avatars", fileId + (showCover ? cover_image_suffix : image_suffix));
                return localPath;

            case "chat":
                let messageInfo = await selectMessage(fileId);
                const month = moment(messageInfo.sendTime).format('YYYY-MM');
                localFolder = path.join(localFolder, "chat", month);

                if (!fs.existsSync(localFolder)) {
                    fse.ensureDirSync(localFolder); // 使用 fs-extra 的 ensureDirSync
                }

                let fileSuffix = messageInfo.fileName.substring(messageInfo.fileName.lastIndexOf('.') + 1);
                // 如果是图片 一律改为png 如果是视频 一律改为mp4
                fileSuffix = (Constants.FILE_TYPE[fileSuffix] === 0) ? ".png" : 
                            (Constants.FILE_TYPE[fileSuffix] === 1) ? ".mp4" : "." + fileSuffix;

                if (showCover === true || showCover === "true") {
                    fileSuffix = "_cover.png";
                }

                localPath = path.join(localFolder, fileId + fileSuffix);
                return localPath;

            case "tmp":
                localFolder = path.join(localFolder, "tmp");
                if (!fs.existsSync(localFolder)) {
                    fse.ensureDirSync(localFolder); // 使用 fs-extra 的 ensureDirSync
                }
                localPath = path.join(localFolder, fileId);
                return localPath;

            default:
                return "";
        }
    } catch (error) {
        console.error('Error in getLocalFilePath:', error);
        return null; // 返回 null 表示失败，或者根据需求处理
    }
};




//启动一个有关图片的服务器
const FILE_CONTENT_TYPE = {
    "0": "image/",
    "1": "video/",
    "2": "application/octet-steam",
    "avatar": "image/",
    "chat": "image/"
}


const express = require('express')
const expressServer = express()
let server = null
const startLocalServer = (serverPort) => {
    console.log("启动服务器")
    server = expressServer.listen(serverPort, () => {
        console.log(`服务器已启动，端口号为: ${serverPort}`);
    })

    server.on('error', (err) => {
        if (err.code === 'EADDRINUSE') {
            const msg = `端口 ${serverPort} 已被占用, 请终止占用端口的程序并重新登录`;
            // if(serverPort == 60000){return}
            // if(serverPort == 60000){return}
            // setTimeout(() => {
            //     getWindow('main').webContents.send("expressFail",msg);
            // }, 3000)
            console.error(`端口 ${serverPort} 已被占用`);
        } else {
            console.error(`启动服务器时发生错误: ${err.message}`);
            getWindow('main').webContents.send("expressFail",err.message);
        }
    });
}
// 

const os = require('os');
const userDir = os.homedir()
//根据环境创建不同名称的文件夹
const fileFolder = userDir + (NODE_ENV === "development" ? "/.PurpleChatFileTest" : "/.PurpleChat/File");

const startTempServer = () => {
    const userAvatarFolder = path.join(fileFolder, "localUserAvatar")
    //生成端口 
    let port = 60000
    startLocalServer(port);
    console.log("userAvatarFolder:",  userAvatarFolder)
    console.log("启动")
    
    if (!fs.existsSync(userAvatarFolder)) {
        fse.ensureDirSync(userAvatarFolder)
    }
    
    store.setUserData("localServerPort", port)
    store.setData("userAvatarFolder", userAvatarFolder)
}




const closeLocalServer = () => {
    if(server){
        server.close()
        server = null
    }
}

const reloadOnline = async(fileId) => {
    console.log("reloadOnline")
    let localPath = await getLocalFilePath("chat", false, fileId)
    if(!fs.existsSync){
        console.log("文件不存在")
    }
    downloadFile(fileId, localPath, "chat", false)
}

const downloadFile = async (fileId, savePath, partType, showCover) => {
    console.log("下载文件");
    console.log("fileID" + fileId);
    console.log("savePath" + savePath);
    console.log("showCover" + showCover);


    let url = `${getDomain()}/api/chat/downloadFile`;
    let token = store.getUserData("token");
    const fileSuffix = savePath.substring(savePath.lastIndexOf('.') + 1).toLowerCase();
    const fileType = Constants.FILE_TYPE[fileSuffix];
    const config = {
        responseType: "stream",
        headers: {
            "token": token,
        },
        params: {
            fileId,
            showCover,
        },
        onDownloadProgress: (progressEvent) => {
            if(showCover == 'true' || showCover == true || partType == "avatar"){
                return
            }
            let percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            console.log("percent" + percentCompleted)
            console.log("savePath" + savePath)
            if(percentCompleted == 100){
                getWindow("main").webContents.send("downloadProgress", { messageId: fileId, progress: 99 });
            }else{
                getWindow("main").webContents.send("downloadProgress", { messageId: fileId, progress: percentCompleted });
            }
            
        },
        maxContentLength: Infinity,
        maxBodyLength: Infinity,
        timeout: 0,
    };

    const stream = fs.createWriteStream(savePath); // 创建文件流
    const fileFolder = path.dirname(savePath);
    let resourecesPath = getResoucesPath();
    let timer = null;
    // 确保目标文件夹存在
    if (!fs.existsSync(fileFolder)) {
        fse.ensureDirSync(fileFolder); // 创建目标文件夹
    }

    try {
        const response = await axios.get(url, config);
        console.log("下载文件成功");
        console.log("状态码:", response.status); // 获取状态码

        if (response.status === 200) {
            console.log("请求成功，响应数据:", response.data);
            response.data.pipe(stream); // 将响应数据写入文件
        }

        // 监听文件写入完成
        await new Promise((resolve, reject) => {
            stream.on("finish", () => {
                getWindow("main").webContents.send("downloadProgress", { messageId: fileId, progress: 100 });

                    if(showCover == 'true' || showCover == true){
                        // getWindow("main").webContents.send("downloadFileCallBack", { fileId, status: 2,fileType });
                        // updateMessage({ status: 2 }, { messageId: fileId });
                    }else{
                        getWindow("main").webContents.send("downloadProgress", { messageId: fileId, progress: 100 });
                        updateMessage({ status: 3 }, { messageId: fileId });
                    }
                resolve();
            });

            stream.on("error", (err) => {
                console.error("文件写入错误:", err);
                reject(err);
            });
        });

    } catch (error) {
        clearInterval(timer); 
        console.error("下载文件时发生错误:", error.message);

        if (error.response) {
            console.error("请求失败，状态码:", error.response.status);
            if (error.response.status === 404) {
                console.error("资源未找到");
            } else if (error.response.status === 500) {
                console.error("服务器内部错误");
            }
            
            console.error("请求过程中发生错误:", error.message);
        }

        // 如果请求失败，返回默认图片
        if (partType === "avatar") {
            fs.createReadStream(path.join(resourecesPath, "assets", "user.png")).pipe(stream);
        } else if (partType === "chat") {
            fs.createReadStream(path.join(resourecesPath, "assets", "404.png")).pipe(stream);
        }



        // 接收失败信息 
        if(showCover){
            getWindow("main").webContents.send("downloadFileCallBack", { fileId, status: -1});
            updateMessage({ status: -1 }, { messageId: fileId });

        }else{
            getWindow("main").webContents.send("downloadFileCallBack", { fileId, status: -2});
            updateMessage({ status: -2 }, { messageId: fileId });
        }
    }
};




const getLocalAvatar = async(res, fileId) => {
    const avatarPath = path.join(store.getData("userAvatarFolder"), fileId + "_cover" + image_suffix);
    console.log("avatarPath:", avatarPath)
        if(!fs.existsSync(avatarPath)){
            console.log("不存在")
            saveDefautLocalUserAvatar(fileId)
        }
        try {
            // 创建可读流
            const readStream = fs.createReadStream(avatarPath);
            // 监听流的错误事件
            readStream.on('error', (err) => {
                console.error('读取文件时出错:', err);
                res.statusCode = 500;
                res.end('内部服务器错误：无法读取文件');
            });
            // 将流的内容发送到响应
            readStream.pipe(res);
        
            } catch (err) {
            console.error('发生错误:', err);
            res.statusCode = 500;
            res.end('内部服务器错误');
            }
}

expressServer.get('/file', async (req, res) => {
    console.log("file")
    let {partType, showCover, fileId, forceGet, isLogin} = req.query

    showCover = (showCover == "true" || showCover == true)
    forceGet = (forceGet == "true" || forceGet == true)
    isLogin = (isLogin == "true" || isLogin == true)

    console.log(showCover)
    console.log("islogin" + isLogin)

    if(fileId == null || fileId == undefined){
        console.log("fileId为空")
        return
    }
    if(isLogin){
        getLocalAvatar(res, fileId)
        return
    }

    console.log("partType:", partType)
    console.log("showCover:", showCover)
    console.log("fileId:", fileId)
    let localPathCover = await getLocalFilePath(partType, true, fileId)
    let localPathDetail = await getLocalFilePath(partType, false, fileId)


    if(forceGet){
        if(showCover){
            console.log("强制下载cover")
            await downloadFile(fileId, localPathCover, partType, true)
            downloadFile(fileId, localPathCover, partType, false)
        }else{
            console.log("强制下载detail")
            await downloadFile(fileId, localPathCover, partType, false)
            downloadFile(fileId, localPathCover, partType, true)
        }
    }else{
        if(showCover){
            if(!fs.existsSync(localPathCover)){
                console.log("找不到  cover")
                await downloadFile(fileId, localPathCover, partType, true)
                downloadFile(fileId, localPathCover, partType, false)
            }
        }else{
            if(!fs.existsSync(localPathDetail)){
                console.log("找不到  detail")
                await downloadFile(fileId, localPathDetail, partType, false)
                downloadFile(fileId, localPathDetail, partType, true)
            }   
        }
    }
    


    // if(((showCover == "true" || showCover == true)&&(!fs.existsSync(localPathCover) ))||((showCover == "false" || showCover == false)&&(!fs.existsSync(localPathDetail) ))|| forceGet == "true" || forceGet == true){
        
    //     if(showCover == "true" || showCover == true){
    //         console.log("cover服务区下载")
    //         await downloadFile(fileId, localPathCover, partType, true)
    //         console.log("cover下载完成")

    //         //如果是下载头像 showcover也需要下载一份原图
    //         if(partType == "avatar"){
    //         console.log("服务区下载")
    //             await downloadFile(fileId, localPathDetail, partType, false)
    //         }
    //     }else{
    //         console.log("服务区下载")
    //         await downloadFile(fileId, localPathDetail, partType, false)
    //     }
        
    // }

    console.log("localPathCover:", localPathCover)  
    console.log("localPathDetail:", localPathDetail)


    const fileSuffix = localPathDetail.substring(localPathDetail.lastIndexOf('.') + 1).toLowerCase()

    let contentType = FILE_CONTENT_TYPE[partType] + fileSuffix
    //临时修改逻辑
    if(fileSuffix == "mp4"){
        contentType = "video/mp4"
    }
    res.setHeader("Access-Control-Allow-Origin", "*")
    res.setHeader("Content-Type", contentType)
    //如果是视频 拖动进度条时range不同 请求的部分则不同
    console.log("fileSuffix:", fileSuffix)
    //首先不是視頻
    if(fileSuffix != "mp4" || showCover){
        try {
            let readStream = null
            // 创建可读流
            if(showCover){
                console.log("cover返回")
                readStream = fs.createReadStream(localPathCover);
            }else{
                console.log("detail返回")
                readStream = fs.createReadStream(localPathDetail);
            }
        
            // 监听流的错误事件
            readStream.on('error', (err) => {
                console.error('读取文件时出错:', err);
                res.statusCode = 500;
                res.end('内部服务器错误：无法读取文件');
            });
        
            // 将流的内容发送到响应
            readStream.pipe(res);
        
            } catch (err) {
            console.error('发生错误:', err);
            res.statusCode = 500;
            res.end('内部服务器错误');
            }
    }else{




        let stat = fs.statSync(localPathDetail)
        let fileSize = stat.size
        let range = req.headers.range
        if(range){
            let parts = range.replace(/bytes=/, "").split("-")
            let start = parseInt(parts[0], 10)
            let end = parts[1] ? parseInt(parts[1], 10) : start + 999999
            end = end>fileSize - 1 ? fileSize- 1 : end
            let chunksize = (end - start) + 1
            let stream = fs.createReadStream(localPathDetail, {start, end})
            stream.on('error', (error) => {
                console.error('文件读取错误:', error);
                res.sendStatus(500); // 返回服务器错误
            });
            let head = {
                'Content-Range' : `bytes ${start}-${end}/${fileSize}`,
                'Accept-Ranges': 'bytes',
                'Content-Length': chunksize, 
                'Content-Type': 'video/mp4'
            }

            res.writeHead(206, head) // 206是http状态码 表示部分请求
            stream.pipe(res)
        }else{
            let head = {
                'Content-Length': fileSize,
                'Content-Type': 'video/mp4'
            }
            res.writeHead(200, head) //全部请求
            fs.createReadStream(localPathDetail).pipe(res)
        }
    }
    return
})

//读取文件



//生成压缩图片的函数处理 作用于更换头像时 生成临时头像文件 以便展示在编辑框里
const createCover = async(filePath) => {
    let ffmpegPath = getffmpegPath()
    let avatarPath = await getLocalFilePath("avatar", false, store.getUserId() + "_temp")
    let command = `${ffmpegPath} -i "${filePath}" "${avatarPath}" -y`

    await executeCommand(command)

    //生成压缩图片

    let coverPath = await getLocalFilePath("avatar", true, store.getUserId() + "_temp")

    



    command = `${ffmpegPath} -i "${filePath}" -y -vframes 1 -vf "scale='min(200,iw*min(200/iw,200/ih))':'min(200,ih*min(200/iw,200/ih))':flags=bicubic" "${coverPath}"`
    await executeCommand(command)
    //返回给前端文件流 以便展示
    try {
        

        const avatarStream = fs.readFileSync(avatarPath);
        const coverStream = fs.readFileSync(coverPath);
        return {
            avatarStream,
            coverStream
        }

        // 对文件进行操作
    } catch (err) {
        if (err.code === 'ENOENT') {
            console.error('文件未找到！请检查路径:', filePath);
          // 这里可以用弹窗或者日志记录的方式而不是让程序崩溃
        } else {
          throw err; // 对其他错误重新抛出异常
        }
    }
}

const saveDefautLocalUserAvatar = (fileId) => {
    if (fileId == null) {
        return;
    }
    
    try {
        const userAvatarFolder = store.getData("userAvatarFolder");
        if (!fs.existsSync(userAvatarFolder)) {
            fse.ensureDirSync(userAvatarFolder);
        }
    
        console.log("触发");
        let avatarCoverPath = path.join(userAvatarFolder, fileId + cover_image_suffix);
    
        const coverStream = fs.createWriteStream(avatarCoverPath); // 创建文件流
        let resourecesPath = getResoucesPath();
    
        fs.createReadStream(path.join(resourecesPath, "/assets/user.png"))
            .pipe(coverStream)
            .on('error', (error) => {
                console.error("An error occurred while streaming the file:", error);
            });
    
        coverStream.on('finish', () => {
            console.log("File successfully copied to:", avatarCoverPath);
        });
    } catch (error) {
        console.error("An error occurred:", error);
    }
    
}


const saveMyGroupAvatar2Local = async(avatarStream, coverStream, groupId) => {
    try {
        const avatarBuffer = Buffer.from(avatarStream);
        const coverBuffer = Buffer.from(coverStream);
        //在保存到用户文件夹里面一份
        const avatarCoverPath = await getLocalFilePath("avatar", true, groupId)
        const avatarPath = await getLocalFilePath("avatar", false, groupId)
        fs.writeFileSync(avatarCoverPath, coverBuffer);
        fs.writeFileSync(avatarPath, avatarBuffer);
        getWindow('main').webContents.send("saveGroupAvatarCallBack", groupId)
    } catch (error) {
        console.error('Error saving files:', error);
    }
}



const saveUserAvatar2Local = async(avatarStream, coverStream) => {
    //将压缩照片复制一份到封面文件夹
    const userAvatarFolder = store.getData("userAvatarFolder") 

    if (!fs.existsSync(userAvatarFolder)) {
        fs.mkdirSync(userAvatarFolder); 1
    }
    
    let avatarCoverPath = path.join(userAvatarFolder, store.getUserId() + cover_image_suffix)
    
    let avatarPath =path.join(userAvatarFolder ,store.getUserId() + image_suffix)
    const avatarBuffer = Buffer.from(avatarStream);
    const coverBuffer = Buffer.from(coverStream);

    // 将 Buffer 写入到文件
    try {
        fs.writeFileSync(avatarCoverPath, coverBuffer);
        fs.writeFileSync(avatarPath, avatarBuffer);

        //在保存到用户文件夹里面一份
        avatarCoverPath = await getLocalFilePath("avatar", true, store.getUserId())
        avatarPath = await getLocalFilePath("avatar", false, store.getUserId())
        fs.writeFileSync(avatarCoverPath, coverBuffer);
        fs.writeFileSync(avatarPath, avatarBuffer);
        getWindow('main').webContents.send("saveAvatarCallBack")


    } catch (error) {
        console.error('Error saving files:', error);
        getWindow('main').webContents.send("error2Front","保存头像失败")
    }
}


//下载文件到本地
const saveAs = async({partType, fileId}) => {
    let fileName = ''
    if(partType == 'avatar'){
        fileName = fileId + image_suffix
    }else if(partType == 'chat'){
        let messageInfo = await selectMessage(fileId)
        fileName = messageInfo.fileName
    }

    const localPath = await getLocalFilePath(partType, false, fileId)
    const options = {
        title: '保存文件',
        defaultPath: fileName,
    }

    let result = await dialog.showSaveDialog(options)
    if(result.cancelled || result.filePath == ''){
        return
    }

    const filePath = result.filePath
    try {
        fs.copyFileSync(localPath, filePath);
        console.log('文件复制成功');
    } catch (error) {
        console.error('文件复制失败:', error);
        // 这里可以添加更多的错误处理逻辑，比如记录日志或者提示用户

    }
    getWindow('main').webContents.send("saveSuccess")
}

//处理剪切文件内容
const saveClipBoardFile = async(file) => {
    const fileSuffix = file.name.substring(file.name.lastIndexOf('.'))

    const filePath = await getLocalFilePath('tmp', false, 'tmp' + fileSuffix)
    let byteArray = file.byteArray
    const buffer = Buffer.from(byteArray)
    fs.writeFileSync(filePath, buffer)

    return {
        size: byteArray.length,
        name: file.name,
        path: filePath
    }
}

const openLocalFolder = async() => {
    let settingInfo = await selectUserSettingInfo(store.getUserId())
    const sysSetting = JSON.parse(settingInfo.sysSetting)
    let localFileFolder = sysSetting.localFileFolder
    localFileFolder = utils.formatPath(localFileFolder)
    if(!fs.existsSync(localFileFolder)){
        fse.ensureDirSync(localFileFolder);
     } // 使用 fs-extra 的 ensureDirSync
    shell.openPath(localFileFolder)
}

const changeLocalFolder = async() => {
    let settingInfo = await selectUserSettingInfo(store.getUserId())
    const sysSetting = JSON.parse(settingInfo.sysSetting)
    let localFileFolder = sysSetting.localFileFolder
    
    const options = {
        properties: ['openDirectory'],
        defaultPath: localFileFolder
    }

    let result = await dialog.showOpenDialog(options)
    if(result.canceled){
        return
    }
    const newFilePath = result.filePaths[0]
    const userId = store.getUserId()
    //不一样时将进行copy    
    if(localFileFolder != newFilePath){
        getWindow('main').webContents.send("copyingCallBack")
        console.log("开始copy")
        console.log(path.join(localFileFolder, userId))

        console.log(path.join(result.filePaths[0], userId))

        await fse.copy(path.join(localFileFolder, userId), path.join(result.filePaths[0], userId))
        //删除源文件
        setTimeout(() => {
            fse.remove(path.join(localFileFolder, userId))
            .then(() => {
                    console.log('Directory removed successfully!');
                }).catch(err => {
                console.error('Error removing directory:', err);
            })
        }, 10000)
    }
    sysSetting.localFileFolder = newFilePath
    const sysSettingString = JSON.stringify(sysSetting)
    await updateUserSetting({sysSetting:sysSettingString})
    //store重新设置
    store.setUserData("localFileFolder", path.join(sysSetting.localFileFolder, userId)) 

    getWindow('main').webContents.send("getSysSettingCallBack", sysSettingString)
    
}

//更新方面
const downloadUpdate = async(id ,fileName) => { //id是userId
    let url = `${store.getData("domain")}/api/update/download`
    const token = store.getData("token")
    const fileSuffix = localPathDetail.substring(localPathDetail.lastIndexOf('.') + 1).toLowerCase()
    const fileType = Constants.FILE_TYPE[fileSuffix]
    const config = {
        responseType: "stream",
        headers: {'Content-Type': 'multipart/form-data', 'token': token},
        onDownloadProgress(progress){
            const loaded = progress.loaded
            getWindow("main").webContents.send("downloadCallBack", loaded)
        }
        
    }

    const response = await axios.post(url, {id:id, version:someConfig.version}, config) 
    const localFile = await getLocalFilePath("null", false, fileName)
    const stream = fs.createWriteStream(localFile)
    response.data.pipe(stream)

    stream.on('finish', async() => {
        stream.close()
        //开始安装
        const command = `start ${localFile}`
        executeCommand(command)    
    })
}
export {
    addFlie2Local,
    startLocalServer,
    closeLocalServer,
    createCover,
    saveAs,
    saveClipBoardFile,
    openLocalFolder,
    changeLocalFolder,
    downloadUpdate,
    downloadFile,
    startTempServer,
    saveUserAvatar2Local,
    addAvatar2Local,
    saveDefautLocalUserAvatar,
    getLocalFilePath,
    saveMyGroupAvatar2Local,
    reloadOnline
}