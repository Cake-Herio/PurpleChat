package com.purplechat.service;

import com.purplechat.exception.BusinessException;

import javax.mail.MessagingException;
import java.util.Map;

public interface EmailService {
    Map<String, String> sendEmail(String to, Boolean isLoginByEmail) throws MessagingException, BusinessException;

}
