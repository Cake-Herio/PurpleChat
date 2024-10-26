import { resolve } from 'path';
import { defineConfig, externalizeDepsPlugin } from 'electron-vite';
import vue from '@vitejs/plugin-vue';
import axios from 'axios';

const internalIp = 'http://172.31.69.137:5050';  // 内网地址
const publicIp = 'https://cn-sz-yd-plustmp1.natfrp.cloud:15050';   // 公网地址


getTargetIp = async() => {
  try {
    await axios.get(`${internalIp}/api/account/test`, { timeout: 1000 });  // 超时时间1秒
    return internalIp;
  } catch (error) {
    return publicIp;
  }
}

export default defineConfig(async () => {
  const targetIp = await getTargetIp();  // 动态选择目标IP

  return {
    main: {
      plugins: [externalizeDepsPlugin()]
    },
    preload: {
      plugins: [externalizeDepsPlugin()]
    },
    renderer: {
      resolve: {
        alias: {
          '@': resolve(__dirname, 'src/renderer/src')
        }
      },
      plugins: [vue()],
      optimizeDeps: {
        exclude: ['fsevents'] // 忽略 fsevents 依赖
      },
      // server: {
      //   hmr: true,
      //   port: 5000,
      //   proxy: {
      //     "/api": {
      //       target: targetIp,   // 使用动态的目标IP
      //       changeOrigin: false,
      //       secure: false,
      //       logLevel: 'debug', 
      //       pathRewrite: {
      //         "^/api": "/api"
      //       }
      //     }
      //   }
      // }
    }
  };
});