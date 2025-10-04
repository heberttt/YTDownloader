package com.hebert.hdownloader.Listener;
import com.hebert.hdownloader.Util.YoutubeDownloaderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.hebert.hdownloader.Message.MusicDownloadRequestMessage;

@Component
public class MessageListener {

    private final YoutubeDownloaderService youtubeDownloaderUtil;


    MessageListener(YoutubeDownloaderService youtubeDownloaderUtil) {
        this.youtubeDownloaderUtil = youtubeDownloaderUtil;
    }



    @KafkaListener(topics = "music-download-requests")
    public void listenMusicDownloadServiceGroup(MusicDownloadRequestMessage message) throws InterruptedException{
        System.out.println("Processing music download: " + message.getYoutubeUrl());

        try{
            youtubeDownloaderUtil.downloadMusic(message.getYoutubeUrl());
            System.out.println("Download completed.");
        }catch(Exception e){
            System.out.println("Install error");
            System.out.println(e.getMessage());
        }
    }
}
