package com.purplechat.controller;

import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.config.AppConfig;
import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.MessageSendDto;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.po.ChatMessage;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.enums.DateTimePatternEnum;
import com.purplechat.enums.MessageStatusEnum;
import com.purplechat.enums.MessageTypeEnum;
import com.purplechat.enums.ResponseCodeEnum;
import com.purplechat.exception.BusinessException;
import com.purplechat.service.ChatMessageService;
import com.purplechat.utils.DateUtils;
import com.purplechat.utils.StringTools;
import com.purplechat.websocket.netty.HandlerMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Date;

@RestController
@RequestMapping("/chat")
public class ChatController extends ABaseController {


    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    @Resource
    private ChatMessageService chatMessageService;
    @Resource
    private AppConfig appConfig;
    @Resource
    private HandlerMessage handlerMessage;

    @RequestMapping("/sendMessage")
    @GlobalInterceptor
    public ResponseVO sendMessage(HttpServletRequest request,
                                  @NotEmpty String contactId,
                                  @NotEmpty String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer status,
                                  Integer fileType) throws BusinessException {


        //只对两种消息进行操作
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setMessageType(messageType);
        chatMessage.setFileType(fileType);
        if(status != null) {
            chatMessage.setStatus(status);
        }


        MessageSendDto messageSendDto = chatMessageService.saveMessage(chatMessage, userInfoByToken);
        return getSuccessResponseVO(messageSendDto);
    }

    @RequestMapping("/uploadFile")
    @GlobalInterceptor
    public ResponseVO uploadFile(HttpServletRequest request,
                                 @RequestParam("messageId") Long messageId,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "cover", required = false) MultipartFile cover
    ) throws BusinessException, IOException {
        //只对两种消息进行操作
        logger.info("文件上传");
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
        chatMessageService.saveMessageFile(userInfoByToken.getUserId(), messageId, file, cover);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/uploadCoverFile")
    @GlobalInterceptor
    public ResponseVO uploadCoverFile(HttpServletRequest request,
                                 @RequestParam("messageId") Long messageId,
                                 @RequestParam("file") MultipartFile coverFile
    ) throws BusinessException, IOException {

        if (coverFile == null || coverFile.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }

        //只对两种消息进行操作
        logger.info("cover文件上传");



        String fileSuffix = (StringTools.getFileSuffix(coverFile.getOriginalFilename())).toLowerCase();
        String month = DateUtils.format(new Date((new Date().getTime())), DateTimePatternEnum.YYYYMM.getPattern());

        File targetFileFolder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);
        if(!targetFileFolder.exists()) {
            targetFileFolder.mkdirs();
        }
        String filePath = null;
        if(ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix)){
            filePath = StringTools.removeFileExtension(targetFileFolder.getPath() + "/" + messageId) + Constants.COVER_IMAGE_SUFFIX;
        }else{
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        logger.info("save file to " + filePath);
        coverFile.transferTo(new File(filePath));

        //更改数据库为0
        ChatMessage chatMessage = this.chatMessageService.getChatMessageByMessageId(messageId);
        chatMessage.setStatus(MessageStatusEnum.SENDING.getType());
        this.chatMessageService.updateChatMessageByMessageId(chatMessage, messageId);


        //获取联系人并发送消息
        String contactId = chatMessage.getContactId();

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageId(messageId);
        messageSendDto.setFileName(filePath);
        messageSendDto.setContactId(contactId);
        messageSendDto.setStatus(MessageStatusEnum.SENDING.getType());
        messageSendDto.setMessageType(MessageTypeEnum.COVER_FILE_UPLOAD.getType());
        handlerMessage.sendMessage(messageSendDto);
        return getSuccessResponseVO(null);
    }



    @RequestMapping("/downloadFile")
//    @GlobalInterceptor
    public void downloadFile(HttpServletRequest request,
                             HttpServletResponse response,
                             @NotEmpty String fileId,
                             @NotNull boolean showCover
    ) throws BusinessException, IOException {
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
        OutputStream out = null;
        FileInputStream in = null;
        try {
            String mimeType = "application/octet-stream";
            File file = null;
            //通过是否是纯数字来判断是头像还是其他文件
            if (!StringTools.isNumber(fileId)) {
                //处理拿头像的逻辑
                String avatarFolder = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
                String avatarPath = appConfig.getProjectFolder() + avatarFolder + fileId + Constants.IMAGE_SUFFIX;
                avatarPath = StringTools.normalizePath(avatarPath); //格式化路径
                if (showCover) {
                    avatarPath = StringTools.removeFileExtension(avatarPath) + Constants.COVER_IMAGE_SUFFIX;
                }
                file = new File(avatarPath);
                if (!file.exists()) {
                    logger.error("路径为" + avatarPath + "的文件不存在");
                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }
                mimeType = "image/jpeg";
            } else {
                //处理拿其他文件的逻辑
                try {
                    Long fileIdNumber = Long.parseLong(fileId);
                    file = chatMessageService.downloadFile(userInfoByToken, fileIdNumber, showCover);
                } catch (NumberFormatException e) {
                }

            }

            //如果是图片 返回的contentType应该不同

            String fileExtension = StringTools.getFileSuffix(file.getName()).toLowerCase();
            switch (fileExtension) {
                case ".jpg":
                case ".jpeg":
                    mimeType = "image/jpeg";
                    break;
                case ".png":
                    mimeType = "image/png";
                    break;
                case ".gif":
                    mimeType = "image/gif";
                    break;
                case ".svg":
                    mimeType = "image/svg+xml";
                    break;
                // Add more cases for different file types as needed
                case ".mp4":
                    mimeType = "video/mp4";
                    break;
                default:
                    mimeType = "application/octet-stream";
                    break;
            }


            response.setContentType(mimeType); // 设置正确的 MIME 类型
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\""); // Add filename for download
            response.setContentLengthLong(file.length());

            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) > 0) {
                out.write(byteData, 0, len);
            }
            out.flush();

            logger.info("下載成功" + fileId + showCover);
            //改数据库状态为3 如果下载的是cover就不改
            if(!showCover){
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setStatus(MessageStatusEnum.SEND_DOWNLOADED.getType());

                if(StringTools.isNumber(fileId)) {
                    this.chatMessageService.updateChatMessageByMessageId(chatMessage, Long.valueOf(fileId));
                }
            }

        } catch (Exception e) {
            logger.error("下载文件失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 设置 HTTP 500 状态码
            response.getWriter().write("服务器内部错误，下载失败");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("关闭输出流失败", e);
                }
            }

        }


    }


}
