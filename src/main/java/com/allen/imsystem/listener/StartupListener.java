package com.allen.imsystem.listener;

import com.allen.imsystem.common.EmailServiceMessageConsumer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        EmailServiceMessageConsumer emailConsumer = new EmailServiceMessageConsumer();
        Thread emailConsumerThread = new Thread(emailConsumer,"emailConsumer1");
        emailConsumerThread.run();
    }
}
