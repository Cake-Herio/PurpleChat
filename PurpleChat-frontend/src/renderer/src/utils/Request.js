import axios from 'axios';
import { ElLoading } from 'element-plus';
import Message from '../utils/Message';
import Api from '../utils/Api';
import Confirm from './Confirm';
const contentTypeForm = 'application/x-www-form-urlencoded;charset=UTF-8';
const contentTypeJson = 'application/json';
const responseTypeJson = 'json';
let loading = null;
let baseURL = "http://172.31.69.137:5050/api"; // 初始内网地址
let externalURL = "https://120.233.26.238:15050/api";
let instance = null; // 将axios实例声明为全局变量

// 创建 axios 实例的函数，在确定 baseURL 后再调用
const createAxiosInstance = (baseURL) => {
    return axios.create({
        withCredentials: true,
        baseURL: baseURL,
        timeout: 10 * 1000, // 服务器响应超时时间
    });
};

// 启动时即测试
const initApp = async () => {
    try {
        await testInternalAPI(); // 测试内网地址
        instance = createAxiosInstance(baseURL); // 如果内网可用，使用内网地址创建axios实例
        console.log("内网地址可用，使用内网地址");
    } catch (error) {
        console.error(error);
        instance = createAxiosInstance(externalURL); // 如果内网不可用，切换到公网地址并创建axios实例
        console.log("内网地址不可用，切换到公网地址");
    }
};

// 请求前拦截器
const setupInterceptors = () => {
    instance.interceptors.request.use(
        async (config) => {
            if (config.showLoading) {
                loading = ElLoading.service({
                    lock: true,
                    text: '加载中......',
                    background: 'rgba(0, 0, 0, 0.7)', // 发请求时设置遮罩，防止重复提交
                });
            }
            return config;
        },
        (error) => {
            if (error.config && error.config.showLoading && loading) {
                loading.close();
            }
            return Promise.reject("请求发送失败");
        }
    );

    // 请求后拦截器
    instance.interceptors.response.use(
        (response) => {
            const { showLoading, errorCallback, showError = true, responseType } = response.config;
            if (showLoading && loading) {
                loading.close();  // 关闭遮罩
            }
            const responseData = response.data;
            if (responseType === "arraybuffer" || responseType === "blob") {
                return responseData;
            }

            // 正常请求
            if (responseData.code === 200) {
                return responseData;
            } else if(responseData.code === 505) {
                // 无心跳
                response.data = null;
                Confirm({
                    message: '您已掉线，请重新登录',
                    showCancelBtn: false,
                    okfun: () => {
                        window.ipcRenderer.send('exit');
                    }
                    })
            } else if(responseData.code === 500) {
                // 无心跳 服務器異常
                response.data = null;
                Confirm({
                    message: '登录失败，请重新登录',
                    showCancelBtn: false,
                    okfun: () => {
                        window.ipcRenderer.send('exit');
                    }
                    })
            }
            
            
            else if (responseData.code === 901) {
                // 登录超时
                setTimeout(() => {
                    window.ipcRenderer.send('reLogin');
                }, 2000);
                return Promise.reject({ showError: true, msg: "登录超时" });
            } else {
                // 其他错误
                if (errorCallback) {
                    errorCallback(responseData);
                }
                return Promise.reject({ showError: showError, msg: responseData.info });
            }
        },
        (error) => {
            if (error.config && error.config.showLoading && loading) {
                loading.close();
            }
            return Promise.reject({ showError: true, msg: "网络异常" });
        }
    );
};

// 实际请求函数
const request = (config) => {
    const { url, params, dataType, showLoading = true, responseType = responseTypeJson, showError = true } = config;
    let contentType = contentTypeForm;
    let formData = new FormData(); // 创建form对象

    for (let key in params) {
        formData.append(key, params[key] === undefined ? "" : params[key]);
    }

    if (dataType === 'json') {
        contentType = contentTypeJson;
    }

    const token = localStorage.getItem('token'); // 将token存储在本地
    let headers = {
        'Content-Type': contentType,
        'X-Requested-With': 'XMLHttpRequest',
        "token": token // 使用 token 作为身份验证
    };

    // 发送请求
    return instance.post(url, formData, {
        headers: headers,
        showLoading: showLoading,
        errorCallback: config.errorCallback,
        showError: showError,
        responseType: responseType
    }).then(response => {
        return response;
    }).catch(error => {
        if (error.showError) {
            Message.error(error.msg);
        }

        return null;
    });
};

// 测试内网地址是否可用
const testInternalAPI = () => {
    return new Promise((resolve, reject) => {
        axios.get("http://172.31.69.137:5050/api/account/test", { timeout: 600 })
            .then(response => {
                if (response.status === 200) {
                    resolve(true); // 内网地址可用
                } else {
                    reject(new Error('内网地址不可用'));
                }
            })
            .catch(() => {
                reject(new Error('内网地址不可用'));
            });
    });
};

// 初始化应用
await initApp(); // 在应用启动时立即测试并初始化axios实例
setupInterceptors(); // 在axios实例创建后，设置拦截器

export default request;
