package com.guillaume_hermet.www.grooveairlineradio.models;

/**
 * Created by Guillaume on 10/4/16.
 */
public class ActionButton {

    private int background;
    private int logoLeft;
    private int logoRight;
    private String title;


    public ActionButton(int background, int logoLeft, int logoRight, String title) {
        this.background = background;
        this.logoLeft = logoLeft;
        this.logoRight = logoRight;
        this.title = title;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLogoLeft() {
        return logoLeft;
    }

    public void setLogoLeft(int logo) {
        this.logoLeft = logo;
    }

    public int getLogoRight() {
        return logoRight;
    }

    public void setLogoRight(int logoRight) {
        this.logoRight = logoRight;
    }
}
