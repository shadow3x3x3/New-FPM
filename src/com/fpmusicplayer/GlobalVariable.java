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
	/* 音樂狀態 */
	public static final int IDLE = 0;
	public static final int PLAY = 1;
	public static final int PAUSE = 2;

	/* Getter */
	public Activity getActivity() {
		return mActivity;
	}

	public int getMusicCursor() {
		return musicCursor;
	}

	public int getMusicTempTime() {
		return musicTempTime;
	}

	public int getSpnPosition() {
		return spnPosition;
	}

	/* Setter */
	public void setMainActivity(MainActivity mainActivity) {
		this.mActivity = mainActivity;
	}

	public void setMusicCursor(int musicCursor) {
		this.musicCursor = musicCursor;
	}

	public void setMusicTempTime(int musicTempTime) {
		this.musicTempTime = musicTempTime;
	}

	public void setSpnPosition(int spnPosition) {
		this.spnPosition = spnPosition;
	}
	
	

}
