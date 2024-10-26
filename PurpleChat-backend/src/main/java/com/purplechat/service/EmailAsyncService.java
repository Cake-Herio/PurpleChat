package com.purplechat.service;

import com.purplechat.entity.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Service("emailAsyncService")
public class EmailAsyncService {
    private static final Logger logger = LoggerFactory.getLogger(EmailAsyncService.class);

    @Autowired
    private JavaMailSender javaMailSender;
    @Resource
    private AppConfig appConfig;

    @Async
    public void sendEmailAsync(String to, String emailCode) throws  MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
        message.setTo(to);
        message.setFrom(appConfig.getEmailUsername());
        message.setSubject("【PurpleChat】");
        String htmlContent ="<html><body style='text-align: center;'>" +
                "您的ID验证码是: 【" + emailCode + "】, 请尽快验证！<br>" +
                "<img src='cid:logoImage' style='width: 100px; height: auto; margin: 20px auto 0; display: block;'>" +  // 设置图片宽度、上方间距并居中
                "</body></html>";
        message.setText(htmlContent, true);
        message.setSentDate(new Date());

        String logoPath = appConfig.getProjectFolder() + "logo/" + "logo.png";

        // 加载图片资源，注意路径需要是正确的
        FileSystemResource imageResource = new FileSystemResource(logoPath);

        // 添加内联图片，"logoImage" 必须和邮件内容中的 'cid:logoImage' 匹配
        message.addInline("logoImage", imageResource);

        logger.info("发送");
        javaMailSender.send(mimeMessage);


    }



}
