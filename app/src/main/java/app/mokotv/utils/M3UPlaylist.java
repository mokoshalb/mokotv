package app.mokotv.utils;

import java.util.List;

public class M3UPlaylist {
    private List<M3UItem> playlistItems;
    public  List<M3UItem> getPlaylistItems() {
        return playlistItems;
    }
    void setPlaylistItems(List<M3UItem> playlistItems) {
        this.playlistItems = playlistItems;
    }
}