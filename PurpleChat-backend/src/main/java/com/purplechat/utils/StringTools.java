package com.purplechat.utils;

import com.purplechat.entity.constants.Constants;
import com.purplechat.enums.UserContactTypeEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class StringTools {

    private final static Logger logger = LoggerFactory.getLogger(StringTools.class);

    public static String getUserId() {
        return UserContactTypeEnum.USER.getPrefix() + getRandomNumber(Constants.LENGTH_11);
    }

    public static String getGroupId() {
        return UserContactTypeEnum.GROUP.getPrefix() + getRandomNumber(Constants.LENGTH_11);
    }

    public static String getRandomNumber (Integer count){
        return RandomStringUtils.random(count, false, true);
    }




    public static String getRandomString( Integer count){
        return RandomStringUtils.random(count, true, true);
    }

    /**
     * 判断字符串是否空
     */

    public static boolean isEmpty (String str){
        if(null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)){
            return true;
        }else if("".equals(str.trim())) {
            return true;//除掉空格后看是否为空
        }
        return false;
    }

    /**
     * md5加密
     */

    public static final String encodeMd5 (String orginString) {
        return StringTools.isEmpty(orginString)?null: DigestUtils.md5Hex(orginString);
    }

    public static String cleanHtmlTag (String content){
        if (isEmpty(content)){
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace(">", "&gt;");
        content = content.replace("\r\n", "<br>");
        content = content.replace("\n", "<br>");
        return content;
    }

    //获取会话id
    public static String getChatSessionId4User(String[] userIds){
        Arrays.sort(userIds);
        return encodeMd5(StringUtils.join(userIds,""));
    }

    public static String getChatSessionId4Group(String groupId){
        return encodeMd5(groupId);
    }

    public static String getFileSuffix(String fileName){
        if (isEmpty(fileName)){
            logger.info("文件类型为空");
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static boolean isNumber(String str){
        String checkNumber = "^[0-9]+$";
        if(null == str){
            return false;
        }
        if(!str.matches(checkNumber)){
            return false;
        }
        return true;
    }

    public static String removeFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            // 没有后缀
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }

    public static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        // 用正则表达式替换连续的多个斜杠为一个斜杠
        String normalizedPath = path.replaceAll("[/\\\\]+", "/");
        // 如果路径不是以斜杠开头，并且是绝对路径，可以根据需要进行处理
        // 比如在Windows系统中，可以使用 \\ 开头表示网络路径
        // 在这里假设路径是相对路径，不进行其他调整

        return normalizedPath;
    }








}
