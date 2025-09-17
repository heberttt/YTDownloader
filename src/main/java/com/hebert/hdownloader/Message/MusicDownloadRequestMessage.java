package com.hebert.hdownloader.Message;

import lombok.Data;

@Data
public class MusicDownloadRequestMessage {
    private String youtube_url;
}
