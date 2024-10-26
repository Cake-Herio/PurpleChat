const FILE_TYPE= {
    jpeg: 0, jpg:0 , png:0,JPEG:0,JPG:0,PNG:0,
    mp4:1,MP4:1, avi:1, rmvb:1, mkv:1,mov:1,MOV:1
}
const login_width = 280;
const login_height = 370;
const register_height = 550;

const main_width = 850
const main_height = 800

const admin_width = 1235
const admin_height = 800


const maxReConnectTimes = 2


const wsTempUse = 'wss://120.233.26.238:15051/ws'


const robot_UID = 'U52013140711'
export default{
    FILE_TYPE,
    login_height,login_width,register_height,main_width,main_height,admin_width,admin_height,maxReConnectTimes,robot_UID, wsTempUse
}