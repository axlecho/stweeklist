package com.songtaste.weeklist;

/**
 * Created by axlecho on 2015/2/15.
 */
public interface PlayerInterface {

    public static final int PLAY_STATUS = 0;
    public static final int PAUSE_STATUS = 1;

    public void setSongName(String songName);

    public void setPlayStatus(int status);

    public void setProgress(int progress);
}
