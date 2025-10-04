package com.hebert.hdownloader.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.hebert.hdownloader.Consumer.YoutubeDataApiConsumer;
import com.hebert.hdownloader.Enum.ThumbnailQuality;
import com.hebert.hdownloader.Event.DownloadMusicWebhookEvent;
import com.hebert.hdownloader.Storage.MinioService;

@Component
public class YoutubeDownloaderService {

    private MinioService minioService;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String musicServerWebhookAddress;

    private static final Logger logger = LoggerFactory.getLogger(YoutubeDownloaderService.class);

    public YoutubeDownloaderService(MinioService minioService, @Value("${app.music.server.webhook}") String musicServerWebhookAddress) {
        this.minioService = minioService;
        this.musicServerWebhookAddress = musicServerWebhookAddress;
        
    }

    private void saveMusicDetailsWebhook(String musicCode) throws IOException{
        List<String> musicData = YoutubeDataApiConsumer.getTitleAndDuration(musicCode);

        String musicTitle = musicData.get(0);

        Integer musicDuration =  YoutubeFormatUtil.convertDurationIntoSeconds(musicData.get(1));

        String musicChannelName = musicData.get(2);

        DownloadMusicWebhookEvent music = new DownloadMusicWebhookEvent();

        music.setTitle(musicTitle);
        music.setDuration(musicDuration);
        music.setMusicId(musicCode);
        music.setChannelName(musicChannelName);

        System.out.println("Webhook music details...");

        String url = musicServerWebhookAddress + "/download-event";

        System.out.println(url);

        restTemplate.postForObject(url, music, String.class);
    }

    public void downloadMusic(String youtubeLink) throws IOException, InterruptedException {

        String link = YoutubeFormatUtil.linkStandardization(youtubeLink);

        String musicCode = YoutubeFormatUtil.linkCodeGetter(link);

        downloadMusicFile(musicCode, link);

        saveThumbnail(musicCode);

        saveMusicDetailsWebhook(musicCode);
    }

    private void saveThumbnail(String musicCode){
        YoutubeDataApiConsumer.downloadThumbnail(musicCode);

        String currentDirectory = System.getProperty("user.dir");

        File highThumbnailFile = new File(currentDirectory + "/tmp/thumbnail/high/" + musicCode + ".jpg");
        File mediumThumbnailFile = new File(currentDirectory + "/tmp/thumbnail/medium/" + musicCode + ".jpg");
        File lowThumbnailFile = new File(currentDirectory + "/tmp/thumbnail/low/" + musicCode + ".jpg");

        String filename = musicCode + ".jpg";
        try{
            minioService.uploadThumbnail(currentDirectory + "/tmp/thumbnail/high/" + filename, filename, ThumbnailQuality.HIGH);
            minioService.uploadThumbnail(currentDirectory + "/tmp/thumbnail/medium/" + filename, filename, ThumbnailQuality.MEDIUM);
            minioService.uploadThumbnail(currentDirectory + "/tmp/thumbnail/low/" + filename, filename, ThumbnailQuality.LOW);
        }catch (Exception e){
            System.out.println("Error uploading thumbnail to minio: " + e.getMessage());
        }finally{
            highThumbnailFile.delete();
            mediumThumbnailFile.delete();
            lowThumbnailFile.delete();
        }
        
    }  

    private void downloadMusicFile(String musicCode, String link) throws IOException, InterruptedException{
        if(minioService.fileExists("file/" + musicCode + ".mp3")){
            System.out.println("Music is already installed");
            return;
        }

        String currentDirectory = System.getProperty("user.dir");

        startDownloadProcess(musicCode, link, currentDirectory);

        File downloadedFile = new File(currentDirectory + "/tmp/music/" + musicCode + ".mp3");

        if (!downloadedFile.exists()) {
            throw new IOException("Music file not found. (Download failed)");
        } else {
            String fileName = musicCode + ".mp3";

            try {
                minioService.uploadMusic(currentDirectory + "/tmp/music/" + fileName, fileName);
            } catch (Exception e) {
                logger.error("minio upload failed: {}", e.getMessage());
                return;
            }finally{
                downloadedFile.delete();
            }
        }
    }

    private void startDownloadProcess(String musicCode, String link, String currentDirectory)
            throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "-x", "--audio-format", "mp3", "-o", musicCode, link);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);
        processBuilder.inheritIO();

        File logFile = new File(currentDirectory + "/logs/yt-dlp_output.txt");
        processBuilder.redirectOutput(logFile);

        processBuilder.directory(new File(currentDirectory + "/tmp/music/"));

        Process process = processBuilder.start();

        process.waitFor();
    }
}
