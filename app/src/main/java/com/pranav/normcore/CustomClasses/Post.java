package com.pranav.normcore.CustomClasses;

public class Post {
    public String influncerName, mediaUrl;
    public long time;
    public boolean isImage;

    public Post(String influencerName, String mediaUrl, long time, boolean isImage){
        this.influncerName = influencerName;
        this.mediaUrl = mediaUrl;
        this.time = time;
        this.isImage = isImage;
    }
}
