import axios from 'axios';
const internalApiDomain = 'http://172.31.69.137:5050'; // 替换为你的内网API地址
const externalApiDomain = 'https://cn-sz-yd-plustmp1.natfrp.cloud:15050'; // 替换为你的公网API地址
const internalWsDomain = 'wss://172.31.69.137:443/ws'; // 替换为你的内网WebSocket地址
const externalWsDomain = 'wss://120.233.26.238:15051/ws'; // 替换为你的公网WebSocket地址


const getDomainIp = async () => {
  try {
    await axios.get(`${internalApiDomain}/api/account/test`, {
      timeout: 500,
    });
    return {
      apiDomain: internalApiDomain,
      wsDomain: internalWsDomain,
    };
  } catch (error) {
    // 返回公网 IP 作为备选方案
    console.error('内网 API 地址不可用，尝试使用公网 API 地址');
    return {
      apiDomain: externalApiDomain,
      wsDomain: externalWsDomain,
    };
  }
}




let api = {
  prodDomain: 'https://cn-sz-yd-plustmp1.natfrp.cloud:15050',
  devDomain: 'https://cn-sz-yd-plustmp1.natfrp.cloud:15050',
  prodWsDomain: 'wss://120.233.26.238:15051/ws',
  devWsDomain: 'wss://120.233.26.238:15051/ws',


  getEmailCode: "/account/getEmailCode", //获取邮箱验证码
  checkCode: "/account/checkCode", //验证码
  login: "/account/login", //登录
  loginDirect: "/account/loginDirect", //立即登录
  loginForget: "/account/loginForget", //忘记密码登录
  register: "/account/register", //注册
  getSysSetting: "/account/getSysSetting",
  loadMyGroup: "/group/loadMyGroup", //获取我创建的群聊
  saveGroup: "/group/saveGroup", //保存群聊
  getGroupInfo: "/group/getGroupInfo", //获取群聊信息
  getGroupInfo4Chat: "/group/getGroupInfo4Chat", //获取群聊群详细信息
  dissolutionGroup: "/group/dissolutionGroup", //解散群聊
  leaveGroup: "/group/leaveGroup", //退出群聊
  addOrRemoveGroupUser: "/group/addOrRemoveGroupUser", //添加或者删除群成员
  search: "/contact/search", //搜索好友
  applyAdd: "/contact/applyAdd", //申请加入
  loadApply: "/contact/loadApply", //获取申请列表
  dealWithApply: "/contact/dealWithApply", //处理申请
  loadContact: "/contact/loadContact", //获取联系人列表
  getContactUserInfo: "/contact/getContactUserInfo", //获取联系人信息(一定是好友)
  addContact2BlackList: "/contact/addContact2BlackList", //拉黑联系人
  delContact: "/contact/delContact", //删除联系人
  removeContact: "/contact/removeContact", //彻底移除联系人
  getContactInfo: "/contact/getContactInfo", //获取联系人信息（不一定是好友）
  saveUserInfo: "/userInfo/saveUserInfo", //保存用户信息
  getUserInfo: "/userInfo/getUserInfo", //获取用户信息
  updatePassword: "/userInfo/updatePassword",
  forgetPassword: "/userInfo/forgetPassword",
  logout: "/userInfo/logout",
  sendMessage: "/chat/sendMessage", //发送消息
  uploadFile: "/chat/uploadFile", //上传文件地址
  loadAdminAccount: "/admin/loadUser", //后台获取用户列表
  updateUserStatus: "/admin/updateUserStatus", //后台更新用户状态
  forceOffLine: "/admin/forceOffLine", //强制下线
  loadGroup: "/admin/loadGroup", //群聊列表
  adminDissolutionGroup: "/admin/dissolutionGroup", //解散群聊
  saveSysSetting: "/admin/saveSysSetting", //保存系统设置
  getSysSetting4Admin: "/admin/getSysSetting", //获取系统设置
  loadUpdateDataList: '/admin/loadUpdateList', //获取更新列表
  delUpdate: '/admin/delUpdate', //删除更新
  saveUpdate: "/admin/saveUpdate", //保存更新
  postUpdate: "/admin/postUpdate", //发布更新
  loadBeautyAccount: "/admin/loadBeautyAccountList", //靓号列表
  saveBeautyAccount: "/admin/saveBeautyAccount", //保存靓号
  delBeautyAccount: "/admin/delBeautyAccount", //删除靓号
  checkVersion: "/update/checkVersion", //更新检测
}


getDomainIp().then(({
  apiDomain,
  wsDomain
}) => {
  api.prodDomain = apiDomain;
  api.devDomain = apiDomain;
  api.prodWsDomain = wsDomain;
  api.devWsDomain = wsDomain;
  window.ipcRenderer.send('setLocalStore', {
    key: "prodDomain",
    value: api.prodDomain
  });
  window.ipcRenderer.send('setLocalStore', {
    key: "devDomain",
    value: api.devDomain
  });
  window.ipcRenderer.send('setLocalStore', {
    key: "prodWsDomain",
    value: api.prodWsDomain
  });
  window.ipcRenderer.send('setLocalStore', {
    key: "devWsDomain",
    value: api.devWsDomain
  });

});



export default api;
