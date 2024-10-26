const regs = {
  email: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/,
  number: /^\+[1-9][0-9]*$/,
  password: /^(?=.*\d)(?=.*[a-zA-Z])[\da-zA-Z~!@#$^&*_]{8,18}$/,
  version: /^[0-9\.]+$/ 
}

const verfiy = (rule, value, reg, callback) => { //reg表示我需要进行正则匹配的表达式
  if (value) {
    if (reg.test(value)) {
      callback()
    } else {
      callback(new Error(rule.message))
    }
  } else {
    callback()
  }
}

const checkPassword = (value) => {
  return regs.password.test(value) //test调用进行正则表达式匹配
}

const checkEmail = (value) => {
  return regs.email.test(value)
}

const password = (rule, value, callback) => {
  return verfiy(rule, value, regs.password, callback)
}

const number = (rule, value, callback) => {
  return verfiy(rule, value, regs.number, callback)
}

const checkId = (Id) => {
  const idRegex = /^[GU]\d{11}$/;
  return idRegex.test(Id);
}

export default {
  checkPassword,
  checkEmail,
  password,
  number,
  checkId
} //默认导出  其他文件import的时候无需解构导入 import { createApp } from 'vue'
