import { defineStore } from "pinia";
export const useContactStateStore = defineStore('contactStateInfo',{
    state:()=>{//用于存储数据，表示应用程序当前状态
        return{
            contactReload: null,
            deleleContactId: null,
        }
    },
    actions:{//用于修改状态，这里封装逻辑操作
        setContactReload(contactReload){
            this.contactReload = contactReload
        },
        deleteContactId(deleleContactId){
            this.deleleContactId = deleleContactId
        }
    }
})  