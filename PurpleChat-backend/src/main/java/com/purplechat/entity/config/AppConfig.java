package com.purplechat.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;


@Configuration
@EnableAsync
@Component("appConfig")
public class AppConfig {
    /**
     * websocket端口
     */
    @Value("${ws.port:}")
    private Integer wsPort;
    /**
     * 文件目录
     */
    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.emails:}")
    private String adminEmails;

    @Value("${spring.mail.username:}")
    private String emailUsername;

    public Integer getWsPort() {
        return wsPort;
    }

    public String getEmailUsername() {
        return emailUsername;
    }

    public String getProjectFolder() {
        //如果配置文件没有加/ /
        if(!projectFolder.endsWith("/")){
            projectFolder += "/";
        }
        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }
}
