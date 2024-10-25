<template>
    <div class="file-panel" @click="saveAs"
        :style="{ cursor: data.sendUserId == userId || (data.sendUserId != userId && data.status == 3) ? 'pointer' : 'default' }">
        <div class="suffixLogo" v-html="ICON_MAP[fileSuffix]"></div>

        <div class="file-info" :title="data.fileName">
            <div class="file-name">
                {{ truncatedFileName }}
            </div>
            <div class="file-size">大小：{{ proxy.Utils.size2Str(data.fileSize) }}</div>
            <div class="process" v-if="data.sendUserId === userId">
                <span class="iconfont icon-ok" v-if="data.status > 0"></span>

                <span v-if="data.status == 0">
                    <svg t="1726125472433" class="icon-loading" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="10293" width="200" height="200">
                        <path
                            d="M512 64c247.3984 0 448 200.6016 448 448S759.3984 960 512 960 64 759.3984 64 512 264.6016 64 512 64z m0 396.8a51.2 51.2 0 1 0 0 102.4 51.2 51.2 0 0 0 0-102.4z m-204.8 0a51.2 51.2 0 1 0 0 102.4 51.2 51.2 0 0 0 0-102.4z m409.6 0a51.2 51.2 0 1 0 0 102.4 51.2 51.2 0 0 0 0-102.4z"
                            fill="#FAAD14" p-id="10294"></path>
                    </svg>
                </span>

                <span v-if="data.status < 0">
                    <svg t="1726122930867" class="icon-error" viewBox="0 0 1028 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="2456" width="200" height="200">
                        <path
                            d="M875.086452 153.730058C676.053818-45.302575 353.260522-45.302575 154.128323 153.730058s-199.032634 521.825929 0 720.958129 521.825929 199.032634 720.958129 0 199.032634-521.825929 0-720.958129zM725.836868 725.438604c-9.757478 9.757478-25.488922 9.757478-35.246399 0L514.557604 549.405739 338.624306 725.438604c-9.757478 9.757478-25.488922 9.757478-35.2464 0s-9.757478-25.488922 0-35.2464l176.032865-176.032864-176.032865-175.933299c-9.757478-9.757478-9.757478-25.488922 0-35.246399 9.757478-9.757478 25.488922-9.757478 35.2464 0l176.032864 176.032865 176.032865-176.032865c9.757478-9.757478 25.488922-9.757478 35.246399 0 9.757478 9.757478 9.757478 25.488922 0 35.246399L549.804004 514.15934 725.836868 690.192204c9.657912 9.757478 9.657912 25.488922 0 35.2464z"
                            fill="#F56C6C" p-id="2457"></path>
                    </svg>
                </span>

                <div class="info">{{ info }}</div>
            </div>



            <div class="process" v-if="data.sendUserId !== userId">
                <span v-if="data.status == 1" @click="downloadFile" style="cursor: pointer">
                    <svg t="1726292820831" class="icon-download" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="1747" width="200" height="200">
                        <path
                            d="M512 741.877551c-11.493878 0-20.897959-9.404082-20.897959-20.897959V303.020408c0-11.493878 9.404082-20.897959 20.897959-20.897959s20.897959 9.404082 20.897959 20.897959v417.959184c0 11.493878-9.404082 20.897959-20.897959 20.897959z"
                            fill="#12e683" p-id="1748"></path>
                        <path
                            d="M512 741.877551c-5.22449 0-10.44898-2.089796-14.628571-6.269388-8.359184-8.359184-8.359184-21.420408 0-29.779592l169.273469-169.273469c8.359184-8.359184 21.420408-8.359184 29.779592 0 8.359184 8.359184 8.359184 21.420408 0 29.779592l-169.27347 169.273469c-4.702041 4.179592-9.926531 6.269388-15.15102 6.269388z"
                            fill="#12e683" p-id="1749"></path>
                        <path
                            d="M512 741.877551c-5.22449 0-10.44898-2.089796-14.628571-6.269388l-169.27347-169.273469c-8.359184-8.359184-8.359184-21.420408 0-29.779592 8.359184-8.359184 21.420408-8.359184 29.779592 0l169.273469 169.273469c8.359184 8.359184 8.359184 21.420408 0 29.779592-4.702041 4.179592-9.926531 6.269388-15.15102 6.269388z"
                            fill="#12e683" p-id="1750"></path>
                        <path
                            d="M512 929.959184c-230.4 0-417.959184-187.559184-417.959184-417.959184s187.559184-417.959184 417.959184-417.959184 417.959184 187.559184 417.959184 417.959184-187.559184 417.959184-417.959184 417.959184z m0-794.122449c-207.412245 0-376.163265 168.75102-376.163265 376.163265s168.75102 376.163265 376.163265 376.163265 376.163265-168.75102 376.163265-376.163265-168.75102-376.163265-376.163265-376.163265z"
                            fill="#12e683" p-id="1751"></path>
                    </svg>
                </span>

                <span v-if="data.status == 0">
                    <svg t="1726125472433" class="icon-loading" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="10293" width="200" height="200">
                        <path
                            d="M512 64c247.3984 0 448 200.6016 448 448S759.3984 960 512 960 64 759.3984 64 512 264.6016 64 512 64z m0 396.8a51.2 51.2 0 1 0 0 102.4 51.2 51.2 0 0 0 0-102.4z m-204.8 0a51.2 51.2 0 1 0 0 102.4 51.2 51.2 0 0 0 0-102.4z m409.6 0a51.2 51.2 0 1 0 0 102.4 51.2 51.2 0 0 0 0-102.4z"
                            fill="#FAAD14" p-id="10294"></path>
                    </svg>
                </span>
                <span class="iconfont icon-ok" v-if="data.status == 3"></span>

                <span v-if="data.status == 2">
                    <svg t="1726294516287" class="icon-downloading" viewBox="0 0 1028 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="26966" width="200" height="200">
                        <path
                            d="M354.025043 470.556346l129.143984 129.143985V253.890809h62.102355v345.809522l129.143984-129.143985 43.911364 43.911364-204.136643 204.076408-204.076408-204.076408 43.911364-43.911364zM881.563763 253.890809l60.235067-29.816358A514.046065 514.046065 0 0 0 580.448661 0v66.80069a448.389841 448.389841 0 0 1 301.115102 187.090119zM447.991748 66.80069V0a513.925595 513.925595 0 0 0-361.410404 224.074451l60.235067 29.816358a448.389841 448.389841 0 0 1 301.175337-187.090119z"
                            p-id="26967" fill="#f4ea2a"></path>
                        <path
                            d="M580.448661 1019.598985A514.046065 514.046065 0 0 0 1000.528021 342.797768l-59.933892 29.695888a447.968196 447.968196 0 1 1-852.747849 0l-59.933892-29.695888a513.98583 513.98583 0 0 0 420.07936 676.801217 500.975055 500.975055 0 0 0 132.517148 0z"
                            p-id="26968" fill="#f4ea2a"></path>
                    </svg>
                </span>

                <span v-if="data.status < 0">
                    <svg t="1726122930867" class="icon-error" viewBox="0 0 1028 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="2456" width="200" height="200">
                        <path
                            d="M875.086452 153.730058C676.053818-45.302575 353.260522-45.302575 154.128323 153.730058s-199.032634 521.825929 0 720.958129 521.825929 199.032634 720.958129 0 199.032634-521.825929 0-720.958129zM725.836868 725.438604c-9.757478 9.757478-25.488922 9.757478-35.246399 0L514.557604 549.405739 338.624306 725.438604c-9.757478 9.757478-25.488922 9.757478-35.2464 0s-9.757478-25.488922 0-35.2464l176.032865-176.032864-176.032865-175.933299c-9.757478-9.757478-9.757478-25.488922 0-35.246399 9.757478-9.757478 25.488922-9.757478 35.2464 0l176.032864 176.032865 176.032865-176.032865c9.757478-9.757478 25.488922-9.757478 35.246399 0 9.757478 9.757478 9.757478 25.488922 0 35.246399L549.804004 514.15934 725.836868 690.192204c9.657912 9.757478 9.657912 25.488922 0 35.2464z"
                            fill="#F56C6C" p-id="2457"></path>
                    </svg>

                </span>

                <div class="info">{{ otherInfo }}</div>
            </div>
        </div>

        <template v-if="data.sendUserId == userId">
            
            <div class="linear-progress-bar-my" v-if="data.status != 1">
                <div class="progress-bar-fill" :style="{ width: displayedPercent + '%' }"></div>
            </div>

            <div class="linear-progress-bar-my" v-if="(data.status == 1)">
                <div class="progress-bar-fill" :style="{ width: 100 + '%' }"></div>
            </div>
        </template>

        <template v-if="data.sendUserId != userId">
            <div class="linear-progress-bar-other" v-if="data.status == 2">
                <div class="progress-bar-fill" :style="{ width: displayedPercent + '%' }"></div>
            </div>

            <div class="linear-progress-bar-other" v-if="data.status == 3">
                <div class="progress-bar-fill" :style="{ width: 100 + '%' }"></div>
            </div>
        </template>



    </div>


    <svg class="icon-redownload" v-if="data.sendUserId !== userId && data.status < 0" @click="downloadFile"
        t="1727138318122" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4269"
        width="200" height="200">
        <path
            d="M895.744 525.696v0.128a32 32 0 0 0 31.84 28.64h0.128a32 32 0 0 0 31.872-32v-0.288a31.968 31.968 0 0 0-0.192-3.072v-0.064q-33.664-317.856-309.024-424.128a31.936 31.936 0 0 0-1.696-0.608q-153.44-49.6-281.824-6.496a32 32 0 0 0-21.824 30.336v0.672a32 32 0 0 0 32 31.36h0.192a32 32 0 0 0 10.016-1.696q108.064-36.288 240.896 6.464 238.08 92.32 267.616 370.752z"
            fill="#27B26A" p-id="4270"></path>
        <path
            d="M895.584 243.52v267.584a32 32 0 1 0 64 0V243.488a32 32 0 0 0-64 0zM417.984 878.4q-260.384-101.952-290.176-370.816a32 32 0 0 0-31.808-28.48h-0.288a32 32 0 0 0-31.712 32v0.288a32 32 0 0 0 0.192 3.232q34.144 308.16 332.064 424a31.68 31.68 0 0 0 2.944 0.96q158.208 44.48 280.64-1.856a32 32 0 0 0 20.672-29.952v-0.576a31.936 31.936 0 0 0-32-31.424h-0.288a32.096 32.096 0 0 0-11.008 2.08q-102.208 38.688-239.232 0.544z"
            fill="#27B26A" p-id="4271"></path>
        <path d="M64 511.104v267.616a32 32 0 1 0 64 0v-267.616a32 32 0 0 0-64 0z" fill="#27B26A" p-id="4272">
        </path>
        <path
            d="M511.872 658.784l-105.28-105.152a32 32 0 0 0-54.624 22.624l0.032 0.032a32 32 0 0 0 9.376 22.624l127.872 127.712a32 32 0 0 0 45.216 0l128.128-128a32 32 0 1 0-45.216-45.28l-105.504 105.44z"
            fill="#27B26A" p-id="4273"></path>
        <path
            d="M480 325.344v373.312c0 20.64 14.336 37.344 32 37.344s32-16.704 32-37.344V325.344C544 304.704 529.664 288 512 288s-32 16.704-32 37.344z"
            fill="#27B26A" p-id="4274"></path>
    </svg>





</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, computed, onUnmounted, watch } from 'vue';
const { proxy } = getCurrentInstance();
const props = defineProps({
    data: {
        type: Object,
        default: () => ({}),
    },
});
import { useDynamicStore } from '@/stores/DynamicStore';
const dynamicStore = useDynamicStore();
const userId = ref(null)
// 添加缓存当前进度的状态
let loadPecent = ref(0);


let info = computed(() => {
    if (props.data.status == 0) {
        return `上传中...`;
    } else if ((props.data.status == 1)) {
        return `上传成功`;
    } else if (props.data.status < 0) {
        return `上传失败`;
    }
});

let otherInfo = computed(() => {
    if (props.data.status == 0) {
        return `加载中`;
    } else if (props.data.status == 1) {
        return `立即下载`;
    }
    else if (props.data.status == 2) {
        return `下载中`
    } else if (props.data.status == 3) {
        return `下载完成`
    }
    else if (props.data.status == -2 || props.data.status == -1) {
        return `下载失败`;
    }
});



const saveAs = () => {
    if (props.data.sendUserId == userId.value || (props.data.sendUserId !== userId.value && props.data.status == 3)) {
        window.ipcRenderer.send('saveAs', {
            partType: 'chat',
            fileId: props.data.messageId,
        })
    }
}



import { FILE_SUFFIX, ICON_MAP, CHAT_MAX_FILENAME_LENGTH_VIEW } from '@/utils/Constants';
import { useUserInfoStore } from "@/stores/UserInfoStore"
const userInfoStore = useUserInfoStore()
const truncatedFileName = computed(() => {
    const fileName = props.data.fileName || '';
    const ext = fileName.split('.').pop(); // 获取文件后缀

    //

    // 如果文件名长度超过最大值，截断文件名并保留后缀
    if (fileName.length > CHAT_MAX_FILENAME_LENGTH_VIEW) {
        const nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.')); // 获取不带后缀的文件名
        const truncatedName = nameWithoutExt.slice(0, CHAT_MAX_FILENAME_LENGTH_VIEW - ext.length - 3); // 截断文件名，留出后缀和 "..."
        return `${truncatedName}...${ext}`; // 返回截断后的文件名加后缀
    } else {
        return fileName;
    }
});

let displayedPercent = ref(0);



let interval = null;

const smoothIncrement = (target) => {
    const targetValue = parseFloat(target);
    if (isNaN(targetValue)) {
        console.warn(`Invalid target value: ${target}`);
        return;
    }

    // 确保之前的定时器被清除
    if (interval) {
        clearInterval(interval);
    }

    let currentValue = parseFloat(displayedPercent.value) ?? 0;
    if (currentValue < targetValue) {
        const step = (targetValue - currentValue) / 20;
        interval = setInterval(() => {

            currentValue = Math.min(currentValue + step, targetValue);
            displayedPercent.value = currentValue.toFixed(2);
            dynamicStore.setData(props.data.messageId, displayedPercent.value);
            if (currentValue >= targetValue) {
                clearInterval(interval);                        
                interval = null; // 清除定时器
            }

            if (displayedPercent.value == 100) {
                console.log("now clear:" + displayedPercent.value + "此时status为  " + props.data.status)
                clearInterval(interval);
                interval = null; // 清除定时器
                if (props.data.status == 0) {
                    console.log("now clear:" + displayedPercent.value + "更改状态")
                    setTimeout(() => {
                        props.data.status = 1
                    }, 800)
                } else if (props.data.status == 2) {
                    console.log("now clear:" + displayedPercent.value + "更改状态")
                    setTimeout(() => {
                        props.data.status = 3
                    }, 800)
                }
            }
        }, 50);
    } else {
        displayedPercent.value = targetValue.toFixed(2);
    }
};


const fileSuffix = ref('');

const downloadFile = () => {
    if (props.data.status == 1 || props.data.status < 0) {
        //初始化进度
        displayedPercent.value = 0;
        //开始下载文件
        props.data.status = 2;
        //无需存数据库
        window.ipcRenderer.send('updateMessageStatus', { messageId: props.data.messageId, status: 2 })
        window.ipcRenderer.send('downloadFile', { fileId: props.data.messageId, messageId: props.data.messageId, fileName: props.data.fileName });
    }
};

watch(
    () => props.data.status, // 监听的对象是 props.status
    (newStatus, oldStatus) => {
        console.log(`status 发生变化: 从 ${oldStatus} 到 ${newStatus}`);

        // 根据新的 status 执行不同的逻辑
        if (newStatus == -2) {
            smoothIncrement(0)
        } 
    },
    { immediate: true } // 确保在初始渲染时立即触发
);


onMounted(() => {
    userId.value = userInfoStore.getInfo().userId;
    if (dynamicStore.getData(props.data.messageId)) {
        displayedPercent.value = dynamicStore.getData(props.data.messageId);
    } else {
        displayedPercent.value = 0;
    }
    
    let isMine = props.data.sendUserId === userId.value;
    if ((props.data.status == 0 && isMine == true) || (props.data.status == 1 || props.data.status == 0) && isMine == false) {
        window.ipcRenderer.on('downloadProgress', (event, data) => {

            if(props.data.status != 2){
                return
            }

            if (data.messageId == props.data.messageId) {
                loadPecent.value = data.progress
                smoothIncrement(loadPecent.value);
            }
        });

        window.ipcRenderer.on('uploadProgress', (event, data) => {
            console.log("上传数据接收 此时status为" + props.data.status)
            if(props.data.status != 0 ){
                return
            }

            if (data.messageId == props.data.messageId) {
                console.log("上传数据接收" + data.progress)
                loadPecent.value = data.progress
                smoothIncrement(loadPecent.value);
            }



        });
    }
    nextTick(() => {
        fileSuffix.value = FILE_SUFFIX[props.data.fileName.split('.').pop()] || 'default';
    });
});

</script>

<style lang="scss" scoped>
.file-panel {
    display: flex;
    padding: 10px 15px;
    background: #fff;
    align-items: center;
    height: 95px;

    .file-info {
        width: 150px;
        display: flex;
        flex-direction: column;
        max-width: 100%;

        .file-name {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: normal;
        }

        .file-size {
            margin-top: 2px;
            font-size: 12px;
        }

        .process {
            display: flex;
            align-items: center;

            .loading {
                width: 16px;
            }

            .info {
                margin-top: 5px;
                margin-left: 5px;
                font-size: 12px;
                color: #545454;
            }

            .loadPercent {
                transition: width 0.5s ease;
                color: #cba5f0;
                font-size: 13px;
                font-weight: bold;
                margin-left: 7px;
                margin-top: 2px;
            }

            .icon-ok {
                margin-top: 5px;
                color: #bb87eb;
                font-size: 16px;
                width: 16px;
            }

            .icon-error {
                margin-top: 10px;
                width: 16px;
                height: 16px;
                fill: #f56c6c;
            }

            .icon-download {
                margin-top: 10px;
                width: 16px;
                height: 16px;
                fill: #FAAD14;
            }

            .icon-downloading {
                margin-top: 10px;
                width: 16px;
                height: 16px;
                fill: #FAAD14;
            }

            .icon-loading {

                margin-top: 10px;
                width: 16px;
                height: 16px;
                fill: #fff;
            }


        }
    }

    .iconfont {
        width: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 40px;
        color: #6e6e6e;
    }
}

.suffixLogo {
    display: flex;
    width: 50px;
    height: auto;
    margin-right: 8px;

    svg {
        width: 100%;
        height: auto;
    }
}

.icon-redownload {
    margin-top: 10px;
    width: 16px;
    height: 16px;
    fill: #f56c6c;
    position: absolute; // 使用绝对定位
    right: 26px; // 靠右
    top: 40%; // 垂直居中
    transform: translateY(-40%); // 将按钮的中间与父元素的中间对齐
    pointer: cursor;
}

.linear-progress-bar-my {
    position: absolute;
    bottom: 0px;
    right: 0;
    height: 1.5px;
    background-color: #f0f0f0;
    width: 237px;
    overflow: hidden;

}

.linear-progress-bar-other {
    position: absolute;
    bottom: 0px;
    left: 0;
    height: 1.5px;
    background-color: #f0f0f0;
    width: 237px;
    overflow: hidden;

}

.progress-bar-fill {
    height: 100%;
    background-color: #cba5f0;
    width: 0;
    transition: width 0.5s ease;
}
</style>