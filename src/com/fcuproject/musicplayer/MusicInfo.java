package com.fcuproject.musicplayer;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2014/7/4.
 */
public class MusicInfo {
	private int id;
	private int track;
	private long duration;
	private long albumID;
	private String time;
	private String title;
	private String artist;
	private String album;
	private String path;
	private Bitmap coverData;
	private boolean background;

	public MusicInfo() {
		id = 0;
		track = 0;
		duration = 0;
		time = null;
		title = null;
		artist = null;
		album = null;
		path = null;
		background = false;
	}

	// 設置
	public void setId(int id) {
		this.id = id;
	}

	public void setTrack(int track) {
		this.track = track;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public void setAlbum(String album) {
		this.album = album;
	}
	
	public void setAlbumID(long albumID) {
		this.albumID = albumID;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public void setCoverData(Bitmap coverData) {
		this.coverData = coverData;
	}
	
	public void setBackground(boolean background) {
		this.background = background;
	}

	// 取得方法
	public int getId() {
		return id;
	}

	public int getTrack() {
		return track;
	}
	
	public long getDuration(){
		return duration;
	}

	public String getTime() {
		return time;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}
	
	public long getAlbumID() {
		return albumID;
	}

	public String getPath() {
		return path;
	}
	
	public Bitmap getCoverData() {
		return coverData;
	}
	
	public boolean getBackground(){
		return background;
	}

}
