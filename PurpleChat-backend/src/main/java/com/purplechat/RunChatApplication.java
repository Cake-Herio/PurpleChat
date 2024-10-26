package com.purplechat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //允许启动定时调度
@EnableAsync //启动异步执行 对标注了异步的方法执行用另一个线程调度 防止阻塞
@MapperScan(basePackages = {"com.purplechat.mappers"})
public class RunChatApplication {
	public static void main(String[] args) { SpringApplication.run(RunChatApplication.class,args); }
}
