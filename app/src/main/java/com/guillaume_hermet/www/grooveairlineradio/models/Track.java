package com.guillaume_hermet.www.grooveairlineradio.models;

/**
 * Created by Guillaume on 10/9/16.
 */
public class Track {
    public final String title;
    private final String artist;
    private final String cover;

    public int getCallmeback() {
        return callmeback;
    }

    public String getCover() {
        return cover;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    private final int callmeback;

    public Track(String title, String artist, String cover, int callmeback) {
        this.title = title;
        this.artist = artist;
        this.cover = cover;
        this.callmeback = callmeback;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", cover='" + cover + '\'' +
                ", callmeback='" + callmeback + '\'' +
                '}';
    }
}