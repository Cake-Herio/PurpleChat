import { defineStore } from "pinia";

export const useUpdateByTimeStampStore = defineStore('updateByTimeStamp', {
    state: () => {
        return {
            lastUpdated: 0 // 存储最后更新时间戳
        };
    },
    actions: {
        clearTimeStamp() {
            this.lastUpdated = 0; // 清空时间戳
        },
        updateTimestamp() {
            this.lastUpdated = new Date().getTime(); // 更新为当前时间戳
        },
        getLastTimeStamp() {
            
            return this.lastUpdated
        }
        
    }
});
