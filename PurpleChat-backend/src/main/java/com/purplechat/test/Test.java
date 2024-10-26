package com.purplechat.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Test {

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
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> data = new HashMap<>();
        data.put("createTime", "2024-09-13 01:56:56");

        // 序列化为 JSON 字符串
        String jsonResult = mapper.writeValueAsString(data);
        System.out.println(jsonResult);
    }
}
