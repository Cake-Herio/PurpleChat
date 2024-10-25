import { defineStore } from "pinia";
export const useUserInfoStore = defineStore('userInfo',{
    state:()=>{//用于存储数据，表示应用程序当前状态
        return{
            userInfo:{} 
        }
    },
    actions:{//用于修改状态，这里封装逻辑操作
        setInfo(userInfo){
            this.userInfo = userInfo
            localStorage.setItem("userInfo", JSON.stringify(userInfo))

        },
        getInfo(){
            return this.userInfo
        }
    }
})