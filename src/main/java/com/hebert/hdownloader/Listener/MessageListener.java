package com.hebert.hdownloader.Listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @KafkaListener(topics = "music-download-requests")
    public void listenMusicDownloadServiceGroup(String message) throws InterruptedException{
        
    }
}
