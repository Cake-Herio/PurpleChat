<template>

    <div class="image-panel" @click="showImageHandler">

        <el-image
            :class="[data && ((data.sendUserId == userId && status == 0) || (data.sendUserId !== userId && (status != 3))) ? 'dimmed-image' : '']"
            :src="computedServerUrl" fit="scale-down" :width="width" @error="handleImageError" @load="handleImageLoad"
            ref="myImage">
            <template #error>
                <div class="iconfont "></div>
            </template>
        </el-image>



        <!-- 上传过程中显示 -->
        <div class="loading-progress" v-if="(data.sendUserId == userId && data.status == 0)">
            <svg viewBox="0 0 36 36" class="circular-chart">

                <path class="circle-bg" d="M18 2.0845
                        a 15.9155 15.9155 0 0 1 0 31.831
                        
                        a 15.9155 15.9155 0 0 1 0 -31.831" />

                <path class="circle" :stroke-dasharray="displayedPercent + ', 100'" d="M18 2.0845
                        a 15.9155 15.9155 0 0 1 0 31.831
                        a 15.9155 15.9155 0 0 1 0 -31.831" />
            </svg>
            <svg t="1727183515355" class="icon-upload" viewBox="0 0 1024 1024" version="1.1"
                xmlns="http://www.w3.org/2000/svg" p-id="4690" width="200" height="200">
                <path
                    d="M1024 693.248q0 25.6-8.704 48.128t-24.576 40.448-36.864 30.208-45.568 16.384l1.024 1.024-17.408 0-4.096 0-4.096 0-675.84 0q-5.12 1.024-16.384 1.024-39.936 0-74.752-15.36t-60.928-41.472-40.96-60.928-14.848-74.752 14.848-74.752 40.96-60.928 60.928-41.472 74.752-15.36l1.024 0q-1.024-8.192-1.024-15.36l0-16.384q0-72.704 27.648-137.216t75.776-112.128 112.128-75.264 136.704-27.648 137.216 27.648 112.64 75.264 75.776 112.128 27.648 137.216q0 37.888-8.192 74.24t-22.528 69.12q5.12-1.024 10.752-1.536t10.752-0.512q27.648 0 52.736 10.752t43.52 29.696 29.184 44.032 10.752 53.76zM665.6 571.392q20.48 0 26.624-4.608t-8.192-22.016q-14.336-18.432-31.744-48.128t-36.352-60.416-38.4-57.344-37.888-38.912q-18.432-13.312-27.136-14.336t-25.088 12.288q-18.432 15.36-35.84 38.912t-35.328 50.176-35.84 52.224-36.352 45.056q-18.432 18.432-13.312 32.768t25.6 14.336l16.384 0q9.216 0 19.968 0.512t20.992 0.512l17.408 0q14.336 1.024 18.432 9.728t4.096 24.064q0 17.408-0.512 30.72t-0.512 25.6-0.512 25.6-0.512 30.72q0 7.168 1.536 15.36t5.632 15.36 12.288 11.776 21.504 4.608l23.552 0q9.216 0 27.648 1.024 24.576 0 28.16-12.288t3.584-38.912q0-23.552 0.512-42.496t0.512-51.712q0-23.552 4.608-36.352t19.968-12.8q11.264 0 32.256-0.512t32.256-0.512z"
                    p-id="4691" fill="#ad84f0"></path>
            </svg>
        </div>

        <!--客户等待本体上传到服务器 -->
        <div class="loading-progress" v-if="data.sendUserId !== userId && (data.status == 0 || data.status == 1)">
            <svg viewBox="0 0 36 36" class="circular-chart">

                <path class="circle-bg" d="M18 2.0845
                        a 15.9155 15.9155 0 0 1 0 31.831
                        a 15.9155 15.9155 0 0 1 0 -31.831" />
                <path class="wait-circle" :stroke-dasharray="100 + ', 100'" d="M18 2.0845
                        a 15.9155 15.9155 0 0 1 0 31.831
                        a 15.9155 15.9155 0 0 1 0 -31.831" />
            </svg>
            <svg t="1727337828289" v-if="data.status == 1" @click="downloadMedia" class="icon-download"
                viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7358" width="200"
                height="200">
                <path
                    d="M923.3 807.1c-32.1 55.5-91.8 90-155.8 90h-488c-64.1 0-123.8-34.5-155.8-90-8-13.9-3.3-31.6 10.6-39.6 13.9-8.1 31.6-3.3 39.6 10.6 21.7 37.6 62.2 61 105.6 61h488.1c43.4 0 83.9-23.4 105.6-61 8-13.8 25.9-18.6 39.6-10.6 13.8 8 18.6 25.7 10.5 39.6zM503 714.6c2.7 2.7 5.9 4.8 9.5 6.3 3.5 1.5 7.3 2.2 11.1 2.2s7.5-0.8 11.1-2.2c3.6-1.5 6.8-3.6 9.5-6.3l213.7-213.7c11.3-11.3 11.3-29.6 0-41s-29.6-11.3-41 0L552.5 624.2V92.8c0-16-13-29-29-29s-29 13-29 29v531.4L330.3 459.9c-11.3-11.3-29.6-11.3-41 0s-11.3 29.6 0 41L503 714.6z"
                    p-id="7359" fill="#ffffff"></path>
            </svg>
            <svg t="1727336320865" v-else class="icon-wait" viewBox="0 0 1035 1024" version="1.1"
                xmlns="http://www.w3.org/2000/svg" p-id="5418" width="200" height="200">
                <path
                    d="M749.714286 129.406593a21.098901 21.098901 0 1 1 0 42.197803H714.549451v129.941099c0 89.206154-59.313231 164.560176-140.659341 188.764835v37.752967c81.34611 24.204659 140.659341 99.558681 140.659341 188.764835V846.769231h35.164835a21.098901 21.098901 0 1 1 0 42.197802h-464.175824a21.098901 21.098901 0 1 1 0-42.197802H320.703297v-129.941099c0-89.21178 59.318857-164.560176 140.65934-188.770462v-37.741714c-81.340484-24.210286-140.659341-99.558681-140.65934-188.764835V171.604396h-35.164835a21.098901 21.098901 0 0 1 0-42.197803h464.175824z m-176.178638 464.654066a14.065934 14.065934 0 0 0-12.86189 25.020484c20.975121 10.785758 37.432264 23.096264 49.450198 36.858373 11.815385 13.537055 21.790945 33.206857 29.690374 59.065671a14.065934 14.065934 0 1 0 26.905318-8.214506c-8.974066-29.380923-20.705055-52.516571-35.406769-69.356307-14.504791-16.609055-33.797626-31.040703-57.777231-43.373715zM393.78989 295.795341a14.065934 14.065934 0 1 0-27.203516 7.156747c10.616967 40.352352 22.179165 68.517978 35.373011 85.014505 13.492044 16.867868 36.306989 31.209495 68.40545 43.615649a14.065934 14.065934 0 0 0 10.138725-26.241407c-27.873055-10.768879-46.71578-22.618022-56.578813-34.945406-10.155604-12.704352-20.434989-37.741714-30.134857-74.605715z"
                    p-id="5419" fill="#ffffff"></path>
            </svg>
        </div>


        <!-- 下载 -->
        <div class="loading-progress" v-if="data.sendUserId !== userId && data.status == 2">
            <svg viewBox="0 0 36 36" class="circular-chart">

                <path class="circle-bg" d="M18 2.0845
                        a 15.9155 15.9155 0 0 1 0 31.831
                        
                        a 15.9155 15.9155 0 0 1 0 -31.831" />


                <path class="circle" :stroke-dasharray="displayedPercent + ', 100'" d="M18 2.0845
                        a 15.9155 15.9155 0 0 1 0 31.831
                        a 15.9155 15.9155 0 0 1 0 -31.831" />

            </svg>
            <svg t="1727337828289" class="icon-download" viewBox="0 0 1024 1024" version="1.1"
                xmlns="http://www.w3.org/2000/svg" p-id="7358" width="200" height="200">
                <path
                    d="M923.3 807.1c-32.1 55.5-91.8 90-155.8 90h-488c-64.1 0-123.8-34.5-155.8-90-8-13.9-3.3-31.6 10.6-39.6 13.9-8.1 31.6-3.3 39.6 10.6 21.7 37.6 62.2 61 105.6 61h488.1c43.4 0 83.9-23.4 105.6-61 8-13.8 25.9-18.6 39.6-10.6 13.8 8 18.6 25.7 10.5 39.6zM503 714.6c2.7 2.7 5.9 4.8 9.5 6.3 3.5 1.5 7.3 2.2 11.1 2.2s7.5-0.8 11.1-2.2c3.6-1.5 6.8-3.6 9.5-6.3l213.7-213.7c11.3-11.3 11.3-29.6 0-41s-29.6-11.3-41 0L552.5 624.2V92.8c0-16-13-29-29-29s-29 13-29 29v531.4L330.3 459.9c-11.3-11.3-29.6-11.3-41 0s-11.3 29.6 0 41L503 714.6z"
                    p-id="7359" fill="#ad84f0"></path>
            </svg>

        </div>

        <!-- 下载失败显示 -->

        <div class="loading-progress" v-if="data && data.status == -2">


            <svg viewBox="0 0 36 36" class="circular-chart">

                <path class="circle-bg" d="M18 2.0845
                        a 15.9155 15.9155 0 0 1 0 31.831
                        a 15.9155 15.9155 0 0 1 0 -31.831" />
                <path class="error-circle" :stroke-dasharray="100 + ', 100'" d="M18 2.0845
                        a 15.9155 15.9155 0 0 1 0 31.831
                        a 15.9155 15.9155 0 0 1 0 -31.831" />
            </svg>

            <svg v-if="data.sendUserId == userId" t="1727282711352" class="icon-error" viewBox="0 0 1024 1024"
                version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29517" width="200" height="200">
                <path
                    d="M820.096 680.4096 571.4752 259.1296c-26.56-44.3904-92.96-44.3904-119.5264 0l-247.8784 421.28c-26.56 44.3904-8.2624 117.7472 44.864 117.7472l525.5552 0C827.6032 798.1632 846.656 724.8 820.096 680.4096zM516.1088 384.7424c15.6672 0 25.344 18.8992 23.9616 35.9488l-17.5104 198.1632-13.824 0-15.6672-198.1632C492.1408 404.096 501.3568 384.7424 516.1088 384.7424zM517.4848 699.0336c-14.2848 0-25.8048-11.52-25.8048-25.8048 0-14.2784 11.52-25.8048 25.8048-25.8048s25.8048 11.5264 25.8048 25.8048C543.296 687.5136 531.776 699.0336 517.4848 699.0336z"
                    fill="#d81e06" p-id="29518"></path>
            </svg>
            <svg v-if="data.sendUserId != userId" t="1727241997980" class="icon-error" viewBox="0 0 1024 1024"
                version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8323" width="200" height="200">
                <path
                    d="M544 896H166.4c-19.2 0-38.4-19.2-38.4-44.8s19.2-44.8 38.4-44.8H512c6.4 32 19.2 64 32 89.6zM512 128c25.6 0 44.8 19.2 44.8 44.8v364.8L640 448c12.8-12.8 19.2-19.2 32-19.2 25.6 0 51.2 19.2 51.2 44.8 0 12.8-6.4 25.6-12.8 32L704 512c-96 32-160 102.4-185.6 192H512c-12.8 0-25.6-6.4-32-12.8L313.6 512c-6.4-6.4-6.4-19.2-6.4-32 0-25.6 19.2-44.8 44.8-44.8 6.4 0 25.6 6.4 32 12.8l83.2 89.6V172.8c0-25.6 19.2-44.8 44.8-44.8z"
                    fill="#f84b38" p-id="8324"></path>
                <path
                    d="M768 576c108.8 0 192 83.2 192 192s-83.2 192-192 192-192-83.2-192-192 83.2-192 192-192z m83.2 108.8c-12.8-12.8-25.6-12.8-38.4 0l-44.8 44.8-44.8-44.8c-12.8-12.8-25.6-12.8-38.4 0l-6.4 6.4c-6.4 12.8-6.4 25.6 6.4 32l44.8 44.8-44.8 44.8c-12.8 12.8-12.8 25.6 0 38.4l6.4 6.4c12.8 6.4 25.6 6.4 32-6.4l44.8-44.8 44.8 44.8c12.8 12.8 25.6 12.8 38.4 0l6.4-6.4c6.4-12.8 6.4-25.6-6.4-32l-44.8-44.8 44.8-44.8c6.4-12.8 6.4-25.6 0-38.4z"
                    fill="#f84b38" p-id="8325"></path>
            </svg>
        </div>





        <div class="play-panel" v-if="showPlay == true && (status == 3 || (data.sendUserId == userId && status == 1))">

            <span class="iconfont icon-video-play"></span>
        </div>
    </div>

</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue';

const props = defineProps({
    width: {
        type: Number,
        default: 170,
    },
    height: {
        type: Number,
    },
    showCover: {
        type: Boolean,
        default: true,
    },
    fileId: {
        type: [String, Number],
    },
    partType: {
        type: String,
        default: 'avatar',
    },
    forceGet: {
        type: Boolean,
        default: false,
    },
    showPlay: {
        type: Boolean,
        default: false,
    },
    isLogin: {
        type: Boolean,
        default: false,
    },
    status: {
        type: Number,
        default: 3,
    },
    data: {
        type: Object,
        default: () => ({}),
    }
});
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()
import { useDynamicStore } from '../stores/DynamicStore';
const dynamicStore = useDynamicStore();
const myImage = ref(null);
import { useGlobalInfoStore } from '../stores/GlobalInfoStore';
const globalInfoStore = useGlobalInfoStore();
const retryTimes = ref(0);
const emit = defineEmits(['updateStatus']);
import { debounce } from 'lodash';
const computedServerUrl = computed(() => {
    if (!props.fileId) return '';
    let serverPort = globalInfoStore.getInfo('localServerPort');
    const timestamp = new Date().getTime(); // 添加时间戳，确保每次请求不同的 URL
    const randomValue = random(); // 生成随机数防止缓存
    console.log("nowstatus" + props.status)
    console.log('computedServerUrl', `http://localhost:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&showCover=${props.showCover}&forceGet=${props.forceGet}&isLogin=${props.isLogin}&${timestamp / randomValue}`);
    if (props.partType == 'avatar') {
        return `http://localhost:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&showCover=${props.showCover}&forceGet=${props.forceGet}&isLogin=${props.isLogin}&${timestamp / randomValue}`;

    } else {
        if (props.status >= 0 || props.status == -2) {
            return `http://localhost:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&showCover=${props.showCover}&forceGet=${props.forceGet}&isLogin=${props.isLogin}`;
        }
    }

});


const handleImageLoad = () => {
    console.log('handleImageLoad');
    // retryTimes.value = 0; // 重置重试次数
    dynamicStore.setData(props.fileId, new Date().getTime()); // 通知其他组件数据已加载
    setTimeout(() => {
        dynamicStore.setStatus('MEDIA_LOADED')
    }, 100)

};                                                                                 

const handleImageError = debounce((event) => {
    if (props.status == -3) {
        return
    }
    const img = event.target;
    if (!img) {
        return
    }
    console.log('重試' + retryTimes.value)

    if (retryTimes.value < 15) {
        retryTimes.value++; // 增加重试次数
        const timestamp = new Date().getTime(); // 添加时间戳避免缓存
        let serverPort = globalInfoStore.getInfo('localServerPort');
        const randomValue = random();
        if (!serverPort || !props.fileId) {
            return;
        }
        console.log("error:" + `http://localhost:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&showCover=${props.showCover}&forceGet=false&isLogin=${props.isLogin}&_t=${timestamp}/${randomValue}`)
        img.src = `http://localhost:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&showCover=${props.showCover}&forceGet=false&isLogin=${props.isLogin}&_t=${timestamp}/${randomValue}`;
    } else {
        console.error('达到最大重试次数，停止重试');
        if (props.partType == 'avatar') {
            img.src = '../assets/img/avatar.png'
        } else {
            img.src = '@/assets/img/404.png';
        }
    }
}, 100); // 500 毫秒防抖延迟
//随机数函数
const random = () => {
    return Math.floor(Math.random() * (100000) === 0 ? 1 : Math.random() * 100000);
};
const forceRefresh = () => {
    nextTick(() => {
        const imgElement = myImage.value?.$el.querySelector('img');
        let serverPort = globalInfoStore.getInfo('localServerPort');
        if (imgElement) {
            serverPort = globalInfoStore.getInfo('localServerPort');
            const randomValue = random(); // 生成随机数防止缓存
            console.log('forceRefresh', `http://localhost:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&showCover=${props.showCover}&forceGet=${props.forceGet}&isLogin=${props.isLogin}&${new Date().getTime() / randomValue}`);
            imgElement.src = `http://localhost:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&showCover=${props.showCover}&forceGet=${props.forceGet}&isLogin=${props.isLogin}&${new Date().getTime() / randomValue}`;
        }
    });
};

const forceReloadOnline = () => {

    console.log('发送reload')
    displayedPercent.value = 0;
    window.ipcRenderer.send('reloadOnline', props.fileId);
};



watch(
    () => dynamicStore.getData('refresh' + props.fileId),
    (newVal, oldVal) => {
        if (newVal !== oldVal) {
            console.log('refresh' + props.fileId, newVal);
            forceRefresh(newVal); // 数据变化时强制刷新图片
        }
    },
    { immediate: true },

);


const debouncedForceReloadOnline = debounce((newVal) => {
    console.log('Debounced reloadOnline', newVal);
    forceReloadOnline(newVal); // 数据变化时强制刷新图片
}, 500); // 300ms 的防抖延迟

watch(
    () => dynamicStore.getData('reloadOnline' + props.fileId),
    (newVal, oldVal) => {
        if (newVal !== oldVal) {
            debouncedForceReloadOnline(newVal);
        }
    },
    { immediate: true }
);










//进度条相关

let displayedPercent = ref(0);
const downloadMedia = () => {
    if (props.data.sendUserId !== userId.value && props.status == 1) {
        emit('updateStatus', 2)
        displayedPercent.value = 0;
        window.ipcRenderer.send('downloadFile', { fileId: props.fileId });
    }
}


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
            dynamicStore.setData(props.fileId, displayedPercent.value);
            if (currentValue >= targetValue) {
                clearInterval(interval);
                interval = null; // 清除定时器
            }

            if (displayedPercent.value == 100) {
                console.log("now clear:" + displayedPercent.value + "此时status为  " + props.data.status)


                clearInterval(interval);
                interval = null; // 清除定时器
                if (props.data.sendUserId == userId.value && props.status == 0) {
                    setTimeout(() => {
                        emit('updateStatus', 1)
                    }, 800)
                } else {
                    setTimeout(() => {
                        emit('updateStatus', 3)
                    }, 800)
                }
                setTimeout(() => {
                    displayedPercent.value = 0;
                    dynamicStore.setData(props.fileId, 0);
                }, 5000)

            }
        }, 50);
    } else {
        displayedPercent.value = targetValue.toFixed(2);
    }
};





const userId = ref(null)
onMounted(() => {
    console.log("data" + JSON.stringify(props.data))
    console.log("Nowstatus:" + props.status)
    console.log("fileID:" + props.fileId)
    console.log("send" + props.data.sendUserId)
    console.log("userId" + userId.value)
    if (props.status < 3) {
        userId.value = userInfoStore.getInfo().userId;
        if (dynamicStore.getData(props.fileId)) {
            displayedPercent.value = dynamicStore.getData(props.fileId);
        } else {
            displayedPercent.value = 0;
        }
        if (props.data.sendUserId == userId.value && props.status < 1) {
            console.log("触发")
            window.ipcRenderer.on('uploadProgress', (event, data) => {
                console.log("触发" + props.fileId + "status为" + props.status)

                if (data.progress && data.messageId == props.fileId) {
                    console.log("接收uploadProgress" + data.progress)
                    smoothIncrement(data.progress);
                }
            });
        };
        if (props.data.sendUserId != userId.value) {
            window.ipcRenderer.on('downloadProgress', (event, data) => {
                console.log("触发")
                if (data.messageId == props.fileId && props.status == 2) {
                    console.log("接收" + data.progress)
                    smoothIncrement(data.progress);
                }
            });
        }

    }


});


onUnmounted(() => {
    if (interval) {
        clearInterval(interval);
    }
    window.ipcRenderer.removeAllListeners('uploadProgress');
    window.ipcRenderer.removeAllListeners('downloadProgress');
});











</script>

<style lang="scss" scoped>
.dimmed-image {
    filter: brightness(0.7);
    /* 调整值使图像变暗，0.7代表70%的亮度 */
    transition: filter 0.3s ease;
    /* 添加平滑过渡效果 */
}



.image-panel {


    position: relative;
    display: flex;
    overflow: hidden;
    max-width: 170px;
    max-height: 170px;
    background: transparent;
    cursor: pointer;

    .icon-image-error {
        margin: 0 auto;
        font-size: 30px;
        background: transparent;
    }

    .play-panel {
        z-index: 2;
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;


        .icon-video-play {
            font-size: 35px;
            color: #c590fd;
        }

        &:hover {
            opacity: 0.8;
        }
    }

    .loading-progress {
        z-index: 2;
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;


        .icon-video-play {
            font-size: 35px;
            color: #b60c0c;
        }

        &:hover {
            opacity: 0.8;
        }
    }

}

.circular-progress {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 2;
}

.circular-chart {
    width: 35px;
    height: 50px;
}

.circle-bg {
    fill: none;
    stroke: #eee;
    stroke-width: 1.5;
}

.circle {
    fill: none;
    stroke-width: 1.5;
    stroke: #c590fd;
    stroke-linecap: round;
    transition: stroke-dasharray 0.35s;
    transform: rotate(-90deg);
    transform-origin: 50% 50%;
}

.error-circle {
    fill: none;
    stroke-width: 1.5;
    stroke: #ff5757;
    stroke-linecap: round;
    transition: stroke-dasharray 0.35s;
    transform: rotate(-90deg);
    transform-origin: 50% 50%;
}

.wait-circle {
    fill: none;
    stroke-width: 1.5;
    stroke: #ffffff;
    stroke-linecap: round;
    transition: stroke-dasharray 0.35s;
    transform: rotate(-90deg);
    transform-origin: 50% 50%;
}

.icon-upload {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 20px;
    /* 调整大小 */
    transform: translate(-50%, -50%);
}

.icon-download {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 20px;
    /* 调整大小 */
    transform: translate(-50%, -50%);
}

.icon-error {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 25px;
    /* 调整大小 */
    transform: translate(-50%, -50%);
}

.icon-wait {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 30px;
    /* 调整大小 */
    transform: translate(-50%, -50%);
}
</style>
