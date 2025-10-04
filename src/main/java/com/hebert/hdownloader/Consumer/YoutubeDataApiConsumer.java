package com.hebert.hdownloader.Consumer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

public class YoutubeDataApiConsumer {

    private static final String APPLICATION_NAME = "HPlayer";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    
    private static final String YOUTUBE_API_KEY = System.getenv("YOUTUBE_API_KEY"); 

    public static void downloadThumbnail(String youtubeCode){
        String currentDirectory = System.getProperty("user.dir");
        List<String> installationPaths = new ArrayList<>();
        installationPaths.add(currentDirectory + "/tmp/thumbnail/low/");
        installationPaths.add(currentDirectory + "/tmp/thumbnail/medium/");
        installationPaths.add(currentDirectory + "/tmp/thumbnail/high/");

        int i = 0;
        for(String installationPath : installationPaths){
            RestTemplate restTemplate = new RestTemplate();

            try{

                String quality = "default";

                switch(i){
                    case 0:
                        quality = "default";
                        break;
                    case 1:
                        quality = "mqdefault";
                        break;
                    case 2:
                        quality = "hqdefault";
                        break;
                    default:
                        break;
                }

                System.out.println("Requesting thumbnail");
                ResponseEntity<byte[]> response = restTemplate.getForEntity("https://i.ytimg.com/vi/" + youtubeCode + "/" + quality + ".jpg", byte[].class);
                

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
                    try (FileOutputStream fos = new FileOutputStream(installationPath + youtubeCode + ".jpg")) {
                        System.out.println("Writing thumbnail");
                        fos.write(response.getBody());
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            i++;
        }

        

    }

    public static List<String> getTitleAndDuration(String youtubeCode) throws IOException {

        System.out.println(YOUTUBE_API_KEY);
        
        YouTube youtubeService = new YouTube.Builder(new NetHttpTransport(), JSON_FACTORY, (HttpRequestInitializer) null)
                .setApplicationName(APPLICATION_NAME)
                .build();

        
        YouTube.Videos.List request = youtubeService.videos()
                .list("snippet,contentDetails");
        VideoListResponse response = request.setId(youtubeCode)
                .setKey(YOUTUBE_API_KEY)
                .execute();

        
        Video video = response.getItems().get(0);

        List<String> result = new ArrayList<>();

        result.add(video.getSnippet().getTitle());
        result.add(video.getContentDetails().getDuration());
        result.add(video.getSnippet().getChannelTitle());

        return result;

    }
}