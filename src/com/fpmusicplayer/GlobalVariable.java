package com.fpmusicplayer;

import java.util.ArrayList;

import android.R.integer;
import android.app.Activity;
import android.app.Application;

public class GlobalVariable extends Application {
	private Activity mActivity;

	/* 歌曲序列 */
	private ArrayList<MusicInfo> allMusicList;
	private ArrayList<MusicInfo> playList;
	private ArrayList<MusicInfo> albumList;
	private ArrayList<MusicInfo> selectList;
	private ArrayList<MusicInfo> artistList;
	private ArrayList<MusicInfo> tempList;
	private ArrayList<MusicInfo> deleteList;

	/* 指標 */
	private int musicCursor 	= 0;
	private int musicTempTime 	= 0;
	private int spnPosition 	= 0;

	/* 布林值 */
	private boolean isPlaying	= false;
	private boolean isPause 	= false;
	private boolean isExpand 	= false;

	/** 固定參數 **/
	/* 音樂狀態 */
	public static final int IDLE 	= 0;
	public static final int PLAY 	= 1;
	public static final int PAUSE 	= 2;
	public static final int PRE 	= 3;
	public static final int NEXT 	= 4;

	/* Getter */
	public int getMusicListSize() {
		return allMusicList.size();
	}
	
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
	
	public void setAllMusicList(ArrayList<MusicInfo> allMusicList) {
		this.allMusicList = allMusicList;
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
	
	public void setIsPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}
	
	public void setIsPause(boolean isPause) {
		this.isPause = isPause;
	}
	
	public void setIsExpand(boolean isExpand) {
		this.isExpand = isExpand;
	}
	
	/* method */
	public void addIntoPlayList(MusicInfo musicInfo) {
		playList.add(musicInfo);
	}
	
	public void clearPlayList() {
		playList.clear();
	}	
	
	public void AddMusicCursor() {
		musicCursor++;
	}
	
	public void resetMusicCursor() {
		musicCursor = 0;
	}
	
	// 得到目前音樂資訊
	public MusicInfo getPlayingNow() {
		return playList.get(musicCursor);
	}
	
	// 是否為list中的最後一首歌
	public boolean isLastOne() {
		if (musicCursor == getMusicListSize() - 1) {
			return true;
		}
		else {
			return false;
		}
	}
}
