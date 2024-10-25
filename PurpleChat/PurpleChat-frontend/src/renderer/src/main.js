import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import App from './App.vue'
import router from '@/router'
import * as Pinia from 'pinia'
import 'element-plus/dist/index.css'
import '@/assets/cust-elementplus.scss'
import '@/assets/icon/iconfont.css'
import '@/assets/base.scss'
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
import ContextMenu from '@imengyu/vue3-context-menu'


import Utils from '@/utils/Utils.js'
import Verify from '@/utils/Verify.js'
import Request from '@/utils/Request.js'
import Message from '@/utils/Message.js'
import Api from '@/utils/Api.js'
import Confirm from '@/utils/Confirm.js'


import Layout from '@/components/Layout.vue'
import WinOp from '@/components/WinOp.vue'
import ContentPanel from '@/components/ContentPanel.vue'
import ShowLocalImage from '@/components/ShowLocalImage.vue'
import UserBaseInfo from '@/components/UserBaseInfo.vue'
import Dialog from '@/components/Dialog.vue'
import Avatar from '@/components/Avatar.vue'
import AvatarBase from '@/components/AvatarBase.vue'
import AvatarUpload from '@/components/AvatarUpload.vue'
import Badge from './components/Badge.vue'
import Table from './components/Table.vue'
import ErrorInternet from './components/ErrorInternet.vue'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(Pinia.createPinia())   
app.use(ContextMenu)     

app.component('Layout', Layout)
app.component('WinOp', WinOp)
app.component('ContentPanel', ContentPanel)
app.component('ShowLocalImage', ShowLocalImage)
app.component('UserBaseInfo', UserBaseInfo)
app.component('Dialog', Dialog)
app.component('Avatar', Avatar)
app.component('AvatarBase', AvatarBase)
app.component('AvatarUpload', AvatarUpload)
app.component('Badge', Badge)
app.component('Table', Table)
app.component('ErrorInternet', ErrorInternet)

app.config.globalProperties.Utils = Utils //配成全局可以让其他文件无需导入就可以直接使用
app.config.globalProperties.Verify = Verify
app.config.globalProperties.Request = Request
app.config.globalProperties.Message = Message
app.config.globalProperties.Api = Api
app.config.globalProperties.Confirm = Confirm

app.mount('#app')

