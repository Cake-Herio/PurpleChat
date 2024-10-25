import moment from "moment";
moment.locale('zh-cn');
const isEmpty = (str) => {
  if (str == null || str == "" || str == undefined) {
    return true;
  }
  return false
}

const getAreaInfo = (data) => {
  if (isEmpty(data)) {
    return "-"
  }
  return data.replace(",", " ")
}

const formatSessionDate = (timestamp) => {
  const timestampTime = moment(timestamp)
  const days = moment().startOf('day').diff(timestampTime.startOf('day'), 'days');
  if (days == 0) {
    return moment(timestamp).format("HH:mm")
  }
  else if (days == 1) {
    return "昨天 "}
  else if(days>=2 && days <= 7){
    return timestampTime.format("dddd")
  }else if(days>7){
    return timestampTime.format("YY/MM/DD")
  }
}

const formatChatDate = (timestamp) => {
  const timestampTime = moment(timestamp)
  const days = moment().startOf('day').diff(timestampTime.startOf('day'), 'days');
  if (days == 0) {
    return moment(timestamp).format("HH:mm")
  }
  else if (days == 1) {
    return "昨天 " + moment(timestamp).format("HH:mm")
  }
  else if(days>=2 && days <= 7){
    return timestampTime.format("dddd" + " " + "HH:mm")
  }else if(days>7){
    return timestampTime.format("YY/MM/DD" + " " + "HH:mm")
  }
}

const size2Str = (limit) => {
  var size = "";
  if (limit < 0.1 * 1024) {
      size = limit.toFixed(2) + "B"; // 小于 0.1KB，则转化成B
  } else if (limit < 1024 * 1024) {
      size = (limit / 1024).toFixed(2) + "KB"; // 小于 1MB，则转化成KB
  } else if (limit < 1024 * 1024 * 1024) {
      size = (limit / (1024 * 1024)).toFixed(2) + "MB"; // 小于 1GB，则转化成MB
  } else {
      size = (limit / (1024 * 1024 * 1024)).toFixed(2) + "GB"; // 其他转化成GB
  }
  var sizeStr = size + ""; // 转成字符串
  var index = sizeStr.indexOf("."); // 获取小数点处的索引
  var dou = sizeStr.substring(index + 1, 2); // 获取小数点后两位的值
  if (dou == "00") { 
      return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2); // 判断后两位是否为00，如果是则删除00
  }
  return size;
}








export default {
  isEmpty,
  getAreaInfo,
  formatSessionDate,
  formatChatDate,
  size2Str
}
