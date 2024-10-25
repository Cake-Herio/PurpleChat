import { defineStore } from "pinia";
export const useMessageCountStore = defineStore('messageCount', {
    state: () => {
        return {
            messageCount: {
                chatCount: 0,
                contactApplyCount: 0,
            }
        }
    },

    actions: {
        
        setCount(key, count, forceUpdate) {
            if(forceUpdate){
                this.messageCount[key] = count
                return
            }
            let curCount = this.messageCount[key]
            this.messageCount[key] = curCount + count 
            if(this.messageCount[key] < 0){
                this.messageCount[key] = 0
            }
        },

        getCount(key) {
            return this.messageCount[key] !== undefined ? this.messageCount[key] : 0;
    
        }
    }

}
)