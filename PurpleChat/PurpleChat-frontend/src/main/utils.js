const path = require('path');


const  ensureDoubleBackslashes = (path) => {
    // 使用正则表达式匹配单个反斜杠并将其替换为双反斜杠
    return path.replace(/(?!^)(\\)(?!\\)/g, '\\\\');
}
    

const removeFileExtension = (filename) => {
    const lastDotPosition = filename.lastIndexOf('.');
    
    // 如果没有找到 . 或 . 在第一个字符位置，则返回原始文件名
    if (lastDotPosition === -1 || lastDotPosition === 0) {
        return filename;
    }

    // 返回去掉后缀的文件名
    return filename.substring(0, lastDotPosition);
}

const formatPath = (inputPath) => {
    if (!inputPath) return '';

    // 使用 path.normalize 将路径转换为合适的格式
    let formattedPath = path.normalize(inputPath);

    // 确保路径中的所有分隔符为反斜杠
    formattedPath = formattedPath.replace(/\//g, '\\');

    // 移除路径末尾的多余反斜杠（除了根目录的反斜杠，如 C:\）
    if (formattedPath.length > 1 && formattedPath.endsWith('\\')) {
        formattedPath = formattedPath.replace(/\\+$/, '');
    }

    return formattedPath;
}



// const getChatSessionId4User = (contactId) => {
//     const userIds = [store.getUserId(), contactId]
//     const joinedUserIds = userIds.join("")
//     return crypto.createHash('md5').update(joinedUserIds).digest('hex');
// }

const net = require('net');

const findFreePort = (startPort, endPort, callback) => {
    if (startPort > endPort) {
    return callback(new Error('Start port must be less than or equal to end port.'));
    }

    function checkPort(port) {
    const server = net.createServer();
    server.unref();
    server.once('error', (err) => {
        if (err.code === 'EADDRINUSE') {
        // Port is in use, try next port
        if (port < endPort) {
            checkPort(port + 1);
        } else {
          // No free port found
            callback(new Error('No free port found in the given range.'));
        }
        } else {
        callback(err);
        }
    });
    server.once('listening', () => {
        server.close(() => callback(null, port));
    });
    server.listen(port, '127.0.0.1');
    }

    checkPort(startPort);
}






export default {
    ensureDoubleBackslashes,
    removeFileExtension,
    formatPath,
    findFreePort,
    
}
