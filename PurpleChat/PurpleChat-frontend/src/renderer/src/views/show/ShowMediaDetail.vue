<template>
    <div class="media-window">
        <div class="win-title drag"></div>
        <div class="media-op no-drag">
            <div :class="['iconfont icon-left', currentIndex == 0 ? 'not-allow' : '']" @dblclick.stop title="上一张"
                @click="next(-1)">
            </div>

            <div :class="['iconfont icon-right', currentIndex >= allFileList.length - 1 ? 'not-allow' : '']"
                @dblclick.stop title="上一张" @click="next(1)">
            </div>

            <template v-if="fileList[0] && fileList[0].fileType == 0">
                <el-divider direction="vertical" />
                <div class="iconfont icon-enlarge" @click.stop="changeSize(0.1)" @dblclick.stop title="放大">
                </div>

                <div class="iconfont icon-narrow" @click.stop="changeSize(-0.1)" @dblclick.stop title="缩小">
                </div>
                <div :class="['iconfont', isOne2One ? 'icon-resize' : 'icon-source-size']" @dblclick.stop
                    @click="resize" :title="isOne2One ? '图片适应窗口大小' : '图片原始大小'">
                </div>

                <div class="iconfont icon-rotate" @dblclick.stop @click="rotate" title="旋转">
                </div>
                <el-divider direction="vertical" />
            </template>

            <div class="iconfont icon-download" @dblclick.stop title="另存为..." @click="saveAs"></div>
        </div>

        <div class="media-panel">
            <viewer :options="options" @inited="inited" :images="fileList"
                v-if="fileList[0] && fileList[0].fileType == 0 && fileList[0].status == 1">
                <img :src="fileList[0].url" />
            </viewer>
            <!-- 展示播放视频 -->
            <div ref="player" id="player" v-show="fileList[0] && fileList[0].fileType == 1 && fileList[0].status == 1"
                style="width: 100%; height: 100%;" @dblclick.stop="toggleMaximize"></div>

            <div class="loading" v-if="fileList[0] && fileList[0].status == 0">加载中...</div>

        </div>

        <winOp @closeCallback="closeWin" ref="winOpRef"></winOp>


    </div>
</template>






<script setup>
import 'viewerjs/dist/viewer.css'
import DPlayer from 'dplayer';
import { component as Viewer } from 'v-viewer';
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
const winOpRef = ref(null)
const currentIndex = ref(0)
const allFileList = ref([])
const fileList = ref([{ fileType: 0, status: 0 }])
const currentFileId = ref(null)
const serverPort = ref(null)




const getCurrentFile = () => {

    if (dPlayer.value) {
        dPlayer.value.pause()
    }

    const curFile = allFileList.value[currentIndex.value]
    const url = getUrl(curFile)
    console.log(url)
    //将信息塞到list的第一个索引处
    fileList.value.splice(0, 1, {
        url: url,
        status: 1,
        fileType: curFile.fileType,
        fileName: curFile.fileName,
        fileSize: curFile.fileSize,
        fileId: curFile.fileId
    })

    if (curFile.fileType == 1) {
        dPlayer.value.switchVideo({
            url: url
        })
    }


}
const getUrl = (file) => {
    return `http://localhost:${serverPort.value}/file?fileId=${file.fileId}&partType=${file.partType}&showCover=false&forceGet=${file.forceGet}${new Date().getTime()}` //? 为了避免缓存问题 添加时间戳

}

const next = (index) => {
    if (currentIndex.value + index < 0 || currentIndex.value + index >= allFileList.value.length) {
        return
    }
    currentIndex.value += index
    getCurrentFile()
}

const saveAs = () => {
    const curFile = allFileList.value[currentIndex.value]
    console.log("触发")
    window.ipcRenderer.send('saveAs', {
        partType: curFile.partType,
        fileId: curFile.fileId
    })
}

//初始化图片组件
const options = ref({
    inline: true,
    toolbar: false,
    navbar: false,
    button: false,
    title: false,
    zoomRatio: 0.1,
    zoomOnWheel: false,
})
const viewerMy = ref(null)
const inited = (e) => {
    viewerMy.value = e
}
const changeSize = (zoomRatio) => {
    if (viewerMy.value == null) {
        return
    }
    viewerMy.value.zoom(zoomRatio, true)
}
const rotate = () => {
    viewerMy.value.rotate(90, true)
}
const isOne2One = ref(false)
const resize = () => {
    isOne2One.value = !isOne2One.value
    if (!isOne2One.value) {
        viewerMy.value.zoomTo(viewerMy.value.initialImageData.ratio, true)//图片原始大小
    } else {
        viewerMy.value.zoomTo(1, true)//图片适应窗口大小
    }
}



//初始化视频播放器组件
const player = ref()
const dPlayer = ref()
const initPlayer = () => {
    dPlayer.value = new DPlayer({
        element: player.value,
        theme: "#cfb0f7",
        screenshot: true,
        video: {
            url: ''
        }
    });

    dPlayer.value.on('fullscreen', () => {4
        console.log('DPlayer 进入全屏');
        winOpRef.value.setMax()
    });

    dPlayer.value.on('fullscreen_cancel', () => {
        console.log('DPlayer 退出全屏');
        winOpRef.value.setMax() // 通知主进程退出全屏
    });
    

}


//关闭视频组件
const closeWin = () => {
    dPlayer.value.pause()


    allFileList.value = []
    fileList.value = []
    currentFileId.value = null
    console.log("close")
}

//重写滚轮事件
const onWheel = (e) => {
    if (fileList.value[0].fileType !== 0) {
        return
    }
    if (e.deltaY > 0) {
        changeSize(-0.1)
    } else {
        changeSize(0.1)
    }
}



onMounted(async () => {

    await nextTick(); // 等待 DOM 更新
    initPlayer();
    window.ipcRenderer.on('pageInitData', (event, data) => {

        allFileList.value = data.fileList
        serverPort.value = data.serverPort
        currentFileId.value = data.currentFileId

        //获取文件索引
        let index = 0
        if (data.currentFileId) {
            index = allFileList.value.findIndex(item => item.fileId == currentFileId.value)
            index = index == -1 ? 0 : index
        }
        currentIndex.value = index
        console.log("get")

        getCurrentFile()
    })


    //添加滚轮监听
    window.addEventListener('wheel', onWheel)
})

onUnmounted(() => {
    console.log("remove")
    window.removeEventListener('wheel', onWheel)
    window.ipcRenderer.removeAllListeners('pageInitData')
})

</script>


















<style lang="scss" scoped>
.media-window {
    padding: 0px;
    height: calc(100vh);
    border: 1px solid #ddd;
    background: #fff;
    position: relative;
    overflow: hidden;

    .win-title {
        height: 37px;
    }

    .media-op {
        position: absolute;
        left: 0px;
        top: 0px;
        height: 35px;
        line-height: 35px;
        display: flex;
        align-items: center;

        .iconfont {
            font-size: 18px;
            padding: 0px 10px;

            &:hover {
                background: #f3f3f3;
                cursor: pointer;
            }
        }

        .not-allow {
            cursor: not-allowed;
            color: #ddd;
            text-decoration: none;

            &:hover {
                color: #ddd;
                cursor: not-allowed;
                background: none;
            }
        }
    }



    .media-panel {
        height: calc(100vh - 37px);
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;

        :deep(.viewer-backdrop) {
            background: #f5f5f5;
        }

        .file-panel {
            .file-item {
                margin-top: 5px;
            }

            .download {
                margin-top: 20px;
                text-align: center;
            }
        }
    }
}

.dplayer {
    background-color: #b02626;
}

.dplayer-bar-wrap .dplayer-bar {
    background-color: rgb(146, 30, 181);
}
</style>
