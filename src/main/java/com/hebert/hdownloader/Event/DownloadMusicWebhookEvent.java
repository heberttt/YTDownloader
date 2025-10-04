package com.hebert.hdownloader.Event;

import lombok.Data;

@Data
public class DownloadMusicWebhookEvent {
    private String musicId;
    private String title;
    private String channelName;
    private Integer duration;
}
