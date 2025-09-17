package com.hebert.hdownloader.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hebert.hdownloader.Storage.MinioService;

@Component
public class YoutubeDownloaderUtil {

    private MinioService minioService;

    private static final Logger logger = LoggerFactory.getLogger(YoutubeDownloaderUtil.class);

    public YoutubeDownloaderUtil(MinioService minioService) {
        this.minioService = minioService;
    }

    public void downloadMusic(String youtubeLink) throws IOException, InterruptedException {
        String link = YoutubeFormatUtil.linkStandardization(youtubeLink);

        String musicCode = YoutubeFormatUtil.linkCodeGetter(link);

        String currentDirectory = System.getProperty("user.dir");

        createDownloadProcess(musicCode, link, currentDirectory);

        File downloadedFile = new File(currentDirectory + "/tmp/music/" + musicCode + ".mp3");

        if (!downloadedFile.exists()) {
            throw new IOException("Music file not found.");
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

    private void createDownloadProcess(String musicCode, String link, String currentDirectory)
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
