package com.hebert.hdownloader.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hebert.hdownloader.Util.YoutubeDownloaderService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
public class DownloadController {
    
    @Autowired
    YoutubeDownloaderService youtubeDownloaderUtil;

    @PostMapping("/download")
    public String download(@RequestBody String url) throws IOException, InterruptedException {
        
        youtubeDownloaderUtil.downloadMusic(url);

        return "started";
    }
    
}
