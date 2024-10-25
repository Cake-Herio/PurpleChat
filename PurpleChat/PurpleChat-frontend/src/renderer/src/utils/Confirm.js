import { ElMessageBox } from 'element-plus';

const confirm = ({ message, okfun, showCancelBtn = true, okText = '确定', cancelFun = null }) => {
  ElMessageBox.confirm(message, '提示', {
    "close-on-click-modal": false,
    confirmButtonText: okText,
    cancelButtonText: '取消',
    showCancelButton: showCancelBtn,
    type: 'info',
  }).then(async () => {
    if (okfun) {
      okfun();
    }
  }).catch(() => {
    // 可选：处理取消的操作
    if (cancelFun) {
      cancelFun(); // 处理取消的操作
    }


  });
};

export default confirm;
