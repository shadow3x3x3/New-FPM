package com.fpmusicplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;

public class GlobalVariable extends Application {
	private Activity mActivity;
	private MainActivity mainActivity;

	/* 歌曲序列 */
	private ArrayList<MusicInfo> allMusicList;
	private ArrayList<MusicInfo> playList;

	/* 指標 */
	private int musicCursor 	= 0;
	private int spnPosition 	= 0;
	
	/* 暫存 */
	private int musicTempTime 	= 0;

	/* 布林值 */
	private boolean isPlaying	= false;
	private boolean isPause 	= false;

	/** 固定參數 **/
	/* 音樂狀態 */
	public static final int IDLE 	= 0;
	public static final int PLAY 	= 1;
	public static final int PAUSE 	= 2;
	public static final int PRE 	= 3;
	public static final int NEXT 	= 4;

	/* Getter */
	
	public MainActivity getMainActivity() {
		return mainActivity;
	}
	
	public Activity getActivity() {
		return mActivity;
	}
	
	public ArrayList<MusicInfo> getMusic() {
		return allMusicList;
	}
	
	public MusicInfo getMusic(int pos) {
		return allMusicList.get(pos);
	}
	
	public ArrayList<MusicInfo> getPlayList() {
		return playList;
	}
	
	public int getMusicListSize() {
		return allMusicList.size();
	}
	
	public int getPlayListSize(){
		return playList.size();
	}

	public int getMusicCursor() {
		return musicCursor;
	}

	public int getSpnPosition() {
		return spnPosition;
	}
	
	public int getMusicTempTime() {
		return musicTempTime;
	}
	
	public String getPlayState(){
		if (isPlaying == false && isPause == false ) {
			return "FirstTimePlay";
		} else if (isPlaying) {
			return "PlayingNow";
		} else if (isPause) {
			return "PauseNow";
		}
		
		return "error";
	}

	/* Setter */
	public void setMainActivity(MainActivity mainActivity) {
		this.mActivity = mainActivity;
		this.mainActivity = mainActivity;
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
	
	
	/* method */
	public void addIntoPlayList(MusicInfo musicInfo) {
		playList.add(musicInfo);
	}
	
	public void clearPlayList() {
		playList.clear();
	}	
	
	public void addMusicCursor() {
		musicCursor++;
	}
	
	public void minusMusicCursor(){
		musicCursor--;
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
