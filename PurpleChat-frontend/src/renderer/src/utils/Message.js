import { ElMessage } from 'element-plus'
import '../assets/base.scss'  // 确保你定义的 CSS 文件已正确引入

const showMessage = (msg, callback, type, customClass = '') => {
    ElMessage({
        type: type,
        message: msg,
        duration: 2000,
        customClass: customClass,  // 使用自定义的 CSS 类
        onClose: () => {
            if (callback) {
                callback();
            }
        }
    })
}

const message = {
    error: (msg, callback) => {
        showMessage(msg, callback, "error", "custom-error");
    },
    success: (msg, callback) => {
        showMessage(msg, callback, "success", "custom-success");
    },
    warning: (msg, callback) => {
        showMessage(msg, callback, "warning", "custom-warning");
    },
}

export default message;
