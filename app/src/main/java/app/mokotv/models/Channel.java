package app.mokotv.models;

import java.io.Serializable;

public class Channel implements Serializable {

    public int id;

    public String category_name;

    public String channel_id;
    public String channel_name;
    public String channel_image;
    public String channel_url;
    public String channel_description;
    public String channel_type;
    public String video_id;
    private boolean isTv = true;

    public Channel() {
    }

    public Channel(String channel_id) {
        this.channel_id = channel_id;
    }

    public Channel(String category_name, String channel_id, String channel_name, String channel_image, String channel_url, String channel_description, String channel_type, String video_id) {
        this.category_name = category_name;
        this.channel_id = channel_id;
        this.channel_image = channel_image;
        this.channel_name = channel_name;
        this.channel_url = channel_url;
        this.channel_description = channel_description;
        this.channel_type = channel_type;
        this.video_id = video_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getChannel_image() {
        return channel_image;
    }

    public void setChannel_image(String channel_image) {
        this.channel_image = channel_image;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getChannel_url() {
        return channel_url;
    }

    public void setChannel_url(String channel_url) {
        this.channel_url = channel_url;
    }

    public String getChannel_description() {
        return channel_description;
    }

    public void setChannel_description(String channel_description) {
        this.channel_description = channel_description;
    }

    public boolean isTv() {
        return isTv;
    }

    public void setIsTv(boolean flag) {
        this.isTv = flag;
    }

    public String getChannel_type() {
        return channel_type;
    }

    public void setChannel_type(String channel_type) {
        this.channel_type = channel_type;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }
}
