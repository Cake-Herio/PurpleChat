// stores/dynamicStore.js
import {
    defineStore
} from 'pinia';

export const useDynamicStore = defineStore('dynamic', {
    state: () => ({
    data: {}, // 存储动态数据的对象
    status: null
  }),
  actions: {
    setData(key, value) {
      this.data[key] = value;
    },
    getData(key) {
      return this.data[key];
    },
    deleteData(key) {
      delete this.data[key];
    },
    setStatus(newStatus) {
      this.status = newStatus;
    },
    getStatus() {
      return this.status;
    }
  },
});
