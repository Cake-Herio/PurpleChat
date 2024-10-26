import {defineStore} from 'pinia'
export const useSysSettingStore = defineStore('sysSetting', {
    state: () => {
        return {
            sysSetting: {},
        }
    },
    actions: {
        setSysSetting(sysSetting) {
            this.sysSetting = sysSetting
        },
        getSysSetting() {
            return this.sysSetting
        },
    },
})  