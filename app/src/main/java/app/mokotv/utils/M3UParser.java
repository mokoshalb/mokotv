package app.mokotv.utils;

import android.util.Log;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.List;

public class M3UParser {

    private static final String EXT_INF = "#EXTINF:";
    private static final String EXT_URL = "http";

    private String convertStreamToString(InputStream is) {
        try {
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public M3UPlaylist parseFile(InputStream inputStream) {
        M3UPlaylist m3UPlaylist = new M3UPlaylist();
        List<M3UItem> playlistItems = new ArrayList<>();
        String stream = convertStreamToString(inputStream);
        String[] linesArray = stream.split(EXT_INF);
        for (int i = 1; i < linesArray.length; i++) {
            String currLine = linesArray[i];
            M3UItem playlistItem = new M3UItem();
            String[] dataArray = currLine.split(",");
            try {
                String url = dataArray[1].substring(dataArray[1].indexOf(EXT_URL)).replace("\n", "").replace("\r", "");
                String name = dataArray[1].substring(0, dataArray[1].indexOf(EXT_URL)).replace("\n", "");
                playlistItem.setItemName(name.trim());
                playlistItem.setItemUrl(url.trim());
            } catch (Exception e) {
                Log.e("Parse Error", "Error: " + e.fillInStackTrace());
            }
            playlistItems.add(playlistItem);
        }
        m3UPlaylist.setPlaylistItems(playlistItems);
        return m3UPlaylist;
    }
}