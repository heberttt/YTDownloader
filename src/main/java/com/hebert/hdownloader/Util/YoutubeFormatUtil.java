package com.hebert.hdownloader.Util;


import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class YoutubeFormatUtil {

    public static String linkStandardization(String link){
        String linkCode = linkCodeGetter(link);

        return "https://www.youtube.com/watch?v=" + linkCode;
    }


    public static void downloadThumbnail(String youtubeCode){
        String currentDirectory = System.getProperty("user.dir");
        List<String> installationPaths = new ArrayList<>();
        installationPaths.add(currentDirectory + "/assets/thumbnails/low/");
        installationPaths.add(currentDirectory + "/assets/thumbnails/medium/");
        installationPaths.add(currentDirectory + "/assets/thumbnails/high/");

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


    public static String linkCodeGetter(String link) {
        //https://www.youtube.com/watch?v=1f81qXxggo8
        //https://youtu.be/1f81qXxggo8?feature=shared
        //https://www.youtube.com/watch?v=u5heWZ9occg&list=PLbLnVwFkWyIi-4rcBuuMPT5tVZuI3ba0z


        String[] splittedLink = link.split("/");
        
        String dirtyCode;
        
        if (link.contains("https://")){
            dirtyCode = splittedLink[3];
        }else{
            dirtyCode = splittedLink[1];
        }


        if (dirtyCode.contains("watch?v=")){
            dirtyCode = dirtyCode.replace("watch?v=", "");
        }

        String cleanCode = "";
        
        int dirtyCodeLength = dirtyCode.length();
        
        for (int i = 0; i < dirtyCodeLength; i++){
            char currentChar = dirtyCode.charAt(i);

            if (currentChar == '?' || currentChar == '&'){
                break;
            }

            cleanCode += currentChar;
        }



        return cleanCode;
    }

    public static byte[] convertFileToByteArray(File file) throws IOException {
        byte[] fileContent = new byte[(int) file.length()];
        
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileContent);
        }
        
        return fileContent;
    }


    public static Integer convertDurationIntoSeconds(String duration){
        duration = duration.replace("PT", "");

        int durationCharLength = duration.length();

        String numberTracker = "";

        Integer totalDuration = 0;

        for (int i = 0; i < durationCharLength; i++){
            char currentChar = duration.charAt(i);

            if (currentChar == 'H'){
                totalDuration += Integer.parseInt(numberTracker) * 3600;
                numberTracker = "";
                continue;
            }else if(currentChar == 'M'){
                totalDuration += Integer.parseInt(numberTracker) * 60;
                numberTracker = "";
                continue;
            }else if (currentChar == 'S'){
                totalDuration += Integer.parseInt(numberTracker);
                numberTracker = "";
                continue;
            }

            numberTracker += currentChar;
        }


        return totalDuration;
    }
}

