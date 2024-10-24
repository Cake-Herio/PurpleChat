import {
  app,
  shell,
  BrowserWindow,
  Menu,
  Tray
} from 'electron';
import path, {
  join
} from 'path';
import {
  electronApp,
  optimizer,
  is
} from '@electron-toolkit/utils';
import {
  onLoginOrRegister,
  onLoginSuccess,
  winTitleOp,
  onSetLocalStore,
  onGetLocalStore,
  onLoadChatSession,
  onDelChatSession,
  onTopChatSession,
  onloadChatMessageData,
  onUpdateMessageStatus,
  onAddLocalMessage,
  onFoceGetChatImage,
  onSetSessionSelect,
  onCreateCover,
  onOpenNewWindow,
  onSaveAs,
  onSaveClipBoardFile,
  onLoadContactApply,
  onUpdateContactNoReadCount,
  onRelogin,
  onExit,
  onOpenLocalFile,
  onGetSysSetting,
  onChangeLocalFolder,
  onReloadSession,
  onOpenUrl,
  onDownloadUpdate,
  onDownloadFile,
  onLoadLocalUser,
  openWindow,
  onStartTempLocalServer,
  onSaveAvatar,
  onReconnectTrue,
  onRemoveContact,
  onAddAvatar2Local,
  onClearCurrentChatSession,
  onTransferStation,
  onForceGetAvatar,
  onDeleteUserSetting,
  onReloadOnLine,
  onUpdateUserSetting

} from './ipc';
import icon from '../../resources/icon.png?asset';
import {
  init
} from './db/ADB';
import {
  saveWindow
} from './windowProxy';


import Constants from './Constants';
import { on } from 'events';
const NODE_ENV = process.env.NODE_ENV;
process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

const {
  session
} = require('electron');




let tray = null;



const createWindow = () => {
  const mainWindow = new BrowserWindow({
    icon: icon,
    width: Constants.login_width,
    height: Constants.login_height,
    show: false,
    autoHideMenuBar: true,
    titleBarStyle: 'hidden',
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      sandbox: false,
      contextIsolation: false,
      webSecurity: false,
    }
  });

  mainWindow.setResizable(false);

  if (NODE_ENV === 'development') {
    mainWindow.webContents.openDevTools();
  }

  mainWindow.on('ready-to-show', () => {
    mainWindow.show();
    mainWindow.setTitle("PurpleChat");
  });

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url);
    return {
      action: 'deny'
    };
  });

  if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL']);
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'));
  }

  saveWindow('main', mainWindow);

  // 设置托盘图标
  tray = new Tray(icon);
  let contextMenu = Menu.buildFromTemplate([{
    label: '退出PurpleChat',
    click: () => {
      app.exit();
    }
  }]);
  tray.setToolTip('PurpleChat');
  tray.setContextMenu(contextMenu);

  tray.on('click', () => {
    mainWindow.setSkipTaskbar(false); // 显示任务栏图标
    mainWindow.show();
  });

  // 监听渲染进程事件--------------------------------------------------------------------

  onLoadLocalUser();

  onSetLocalStore();

  onStartTempLocalServer()

  onDeleteUserSetting();

  onExit()


  onLoginOrRegister((isLogin) => {
    mainWindow.setResizable(true);
    if (isLogin) {
      mainWindow.setSize(Constants.login_width, Constants.login_height);
    } else {
      mainWindow.setSize(Constants.login_width, Constants.register_height);
    }
    mainWindow.setResizable(false);
  });
  let adminMenuItem = null;





  onLoginSuccess((config) => {
    mainWindow.setResizable(true);
    mainWindow.setSize(Constants.main_width, Constants.main_height);
    mainWindow.center();
    mainWindow.setMaximizable(true);
    mainWindow.setMinimumSize(800, 600);
    //管理员额外加窗口 托盘方面
    // 更新托盘菜单   






    if (config.admin) {

      adminMenuItem = {
        label: '后台管理',
        click: function () {
          openWindow({
            windowId: 'admin',
            title: '后台管理',
            path: '/admin',
            width: Constants.admin_width,
            heigh: Constants.admin_height,
            data: {
              token: config.token
            }
          })
        }
      };
    }
    let newContextMenuTemplate = [{
      label: `${config.nickName}的chat`,
      click: () => {
        mainWindow.setSkipTaskbar(false); // 显示任务栏图标
        mainWindow.show();
      }
    }, {
      label: '退出PurpleChat',
      click: () => {
        app.exit();
      }
    }, ...(adminMenuItem ? [adminMenuItem] : [])];

    contextMenu = Menu.buildFromTemplate(newContextMenuTemplate);
    tray.setContextMenu(contextMenu);
    onGetLocalStore();
    onLoadChatSession();
    onDelChatSession();
    onTopChatSession();
    onloadChatMessageData();
    onAddLocalMessage();
    onSetSessionSelect();
    onCreateCover()
    onOpenNewWindow();
    onUpdateMessageStatus();
    onSaveAs();
    onSaveClipBoardFile();
    onLoadContactApply();
    onUpdateContactNoReadCount();
    onOpenLocalFile();
    onGetSysSetting();
    onChangeLocalFolder();
    onReloadSession();
    onOpenUrl()
    onDownloadUpdate(),
    onDownloadFile()
    onSaveAvatar()
    onReconnectTrue();
    onRemoveContact();
    onAddAvatar2Local()
    onClearCurrentChatSession()
    onTransferStation();
    onForceGetAvatar();
    onFoceGetChatImage();
    onReloadOnLine();
    onUpdateUserSetting();
    onRelogin(() => {
      mainWindow.setResizable(true);
      mainWindow.setMinimumSize(Constants.login_width, Constants.login_height);
      mainWindow.setSize(Constants.login_width, Constants.login_height);
      mainWindow.center()
      mainWindow.setResizable(false); //设置窗口不可改变大小
    });
    // 监听渲染进程事件--------------------------------------------------------------------


  });
  winTitleOp((e, {
    action,
    data
  }) => {
    const webContents = e.sender;
    const win = BrowserWindow.fromWebContents(webContents);
    switch (action) {
      case 'setTop':
        win.setAlwaysOnTop(data);
        break;
      case 'close':
        if (data.closeType === 0) win.close();
        else {
          win.setSkipTaskbar(true);
          win.hide();
        }
        break;
      case 'minimize':
        win.minimize();
        break;
      case 'maximize':
        console.log("maximize");
        win.maximize();
        break;
      case 'unmaximize':
        console.log("unmaximize");
        win.unmaximize();
        break;
      default:
        break;
    }
  });
}
app.on('certificate-error', (event, webContents, url, error, certificate, callback) => {
  event.preventDefault();
  callback(true); // 忽略证书错误
});
app.whenReady().then(() => {
  init();
  createWindow();
  const cacheDir = path.join(app.getPath('userData'), 'cache');
  app.commandLine.appendSwitch('disk-cache-dir', cacheDir);

  electronApp.setAppUserModelId('com.electron');
  app.on('browser-window-created', (_, window) => {
    optimizer.watchWindowShortcuts(window);
  });

});

app.on('activate', function () {
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.commandLine.appendSwitch('ignore-certificate-errors', 'true');


