package com.fpmusicplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;

public class GlobalVariable extends Application {
	private Activity mActivity;
	
	/* 歌曲序列 */
	private ArrayList<MusicInfo> musicInfos;
	private ArrayList<MusicInfo> playList;
	private ArrayList<MusicInfo> albumList;
	private ArrayList<MusicInfo> selectList;
	private ArrayList<MusicInfo> artistList;
	private ArrayList<MusicInfo> tempList;
	private ArrayList<MusicInfo> deleteList;
	
	/* 指標 */
	private int musicCursor = 0;
	public int musicTempTime = 0;
	public int spnPosition = 0;
	
	/* 布林值 */
	public boolean isPlaying = false;
	public boolean isPause = false;
	public boolean isExpand = false;
	
	/** 固定參數 **/
	
	/* 選單狀態 */
	public static final int TAB_AMOUNT = 3;
	public static final int showPlaylist = 0;
	public static final int sortWithName = 1;
	public static final int sortWithAlbum = 2;
	public static final int sortWithArtist = 3;
	public static final int findList = 4;
	public static final long serialVersionUID = 1L;

	/* 音樂狀態 */
	public static final int IDLE = 0;
	public static final int PLAY = 1;
	public static final int PAUSE = 2;
	
	
	GlobalVariable(MainActivity){
		mActivity = MainActivity;
	}
	
	/* Getter */
	public Activity getActivity() {
		return mActivity;
	}
	public int getMusicCursor(){
		return musicCursor;
	}
	
	public int getMusicTempTime(){
		return musicTempTime;
	}
	
	public int getSpnPosition(){
		return spnPosition;
	}

	/* Setter */
	
	public void setMusicCursor(int musicCursor){
		this.musicCursor = musicCursor;
	}
	
	public void setMusicTempTime(int musicTempTime){
		this.musicTempTime = musicTempTime;
	}
	
	public void setSpnPosition(int spnPosition){
		this.spnPosition = spnPosition;
	}

}
