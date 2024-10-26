const Store = require('electron-store');
const store = new Store();

let userId = null;

const initUserId = (id) => {
    userId = id;
}

const getUserId = () => {
    return userId;
}

const getData = (key) => {
    return store.get(key);
}
const setData = (key, value) => {
    store.set(key, value);
}

const getUserData = (key) => {
    return store.get(userId + key);
}

const setUserData = (key, value) => {
    store.set(userId + key, value);
}

const deleteUserData = (key) => {
    store.delete(userId + key);
}

export default {
    initUserId,
    getUserId,
    getData,
    setData,
    getUserData,
    setUserData,
    deleteUserData
}

        
