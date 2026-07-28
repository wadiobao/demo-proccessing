package com.example.demo.config;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @org.springframework.beans.factory.annotation.Value("${app.async.core-pool-size:5}")
    private int corePoolSize;

    @org.springframework.beans.factory.annotation.Value("${app.async.max-pool-size:10}")
    private int maxPoolSize;

    @org.springframework.beans.factory.annotation.Value("${app.async.queue-capacity:25}")
    private int queueCapacity;

    @Bean(name = "pdfTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("pdf-task-exe-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
            LoggerFactory.getLogger(AsyncConfig.class)
                .error("[ASYNC-ERROR] Method: {}, Params: {}, Error: {}",
                    method.getName(), Arrays.toString(params), ex.getMessage(), ex);
    }
}
