import { defineStore } from "pinia";
export const useAvatarInfoStore = defineStore('avatarInfo', {
    state: () => {
        return {
            avatarMap: {

            }
        }
    },
    actions: {
        setForceReload(uid, forceReload) {
            this.avatarMap[uid] = forceReload
        },
        getForceReload(uid){
            return this.avatarMap[uid]
        }
    }
})