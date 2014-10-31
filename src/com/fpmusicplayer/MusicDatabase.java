package com.fpmusicplayer;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

/**
 * Created by Administrator on 2014/7/4.
 */
public class MusicDatabase {
	private ArrayList<MusicInfo> musicInfos;
	private static final Uri albumCoverUri = Uri
			.parse("content://media/external/audio/albumart");
	
	public MusicDatabase() {
		musicInfos = new ArrayList<MusicInfo>();
	}

	// 取得音樂檔案資訊
	public ArrayList<MusicInfo> readMusic(Activity mainActivity)
			throws FileNotFoundException {
		int index = 0;
		/* 解析音樂檔 */
		Cursor musicCursor = mainActivity.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.TITLE, 		// 取得音樂檔名稱
						       MediaStore.Audio.Media.ARTIST, 		// 取得演出者
						       MediaStore.Audio.Media.TRACK, 		// 取得音樂音軌
						       MediaStore.Audio.Media.ALBUM, 		// 取得專輯名稱
						       MediaStore.Audio.Media.ALBUM_ID, 	// 取得專輯ID
						       MediaStore.Audio.Media._ID, 			// 取得音樂id
						       MediaStore.Audio.Media.DATA, 		// 取得音樂路徑
						       MediaStore.Audio.Media.DURATION, 	// 取得音樂檔長度
						       MediaStore.Audio.Media.DISPLAY_NAME, // 取得表示名稱
				}, null, null, null);

		// 移動指標
		musicCursor.moveToFirst();
		// 讀取每一個音樂檔
		while (musicCursor.moveToNext()) {
			// 放入MusicInfo
			MusicInfo temp = new MusicInfo();
			temp.setTitle		(musicCursor.getString(0));
			temp.setArtist		(musicCursor.getString(1));
			temp.setTrack		(musicCursor.getInt(2));
			temp.setAlbum		(musicCursor.getString(3));
			temp.setAlbumID		(musicCursor.getLong(4));
			temp.setId			(musicCursor.getInt(5));
			temp.setPath		(musicCursor.getString(6));
			temp.setDuration	(musicCursor.getLong(7));
			temp.setTime		(formatTime(musicCursor.getLong(7)));
			temp.setCoverData	(getAlbumCover(temp.getAlbumID(), mainActivity));
			musicInfos.add(index, temp);
			index++;
		}
		musicCursor.close();
		return musicInfos;
	}// readMusic end

	// 轉換毫秒
	public String formatTime(long time) {
		String min = time / (1000 * 60) + "";
		String sec = time % (1000 * 60) + "";
		if (min.length() < 2) {
			min = "0" + time / (1000 * 60) + "";
		} else {
			min = time / (1000 * 60) + "";
		}
		if (sec.length() == 4) {
			sec = "0" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 3) {
			sec = "00" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 2) {
			sec = "000" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 1) {
			sec = "0000" + (time % (1000 * 60)) + "";
		}
		return min + ":" + sec.trim().substring(0, 2);
	}

	// 藉由專輯id 找尋 專輯封面
	public Bitmap getAlbumCover(long albumID, Activity mainActivity) {
		/* 定義區域變數 */
		Bitmap bm = null;
		FileDescriptor fd = null;

		Uri uri = ContentUris.withAppendedId(albumCoverUri, albumID);

		// 讀取專輯封面
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			ParcelFileDescriptor pfd = mainActivity.getContentResolver()
					.openFileDescriptor(uri, "r");
			if (pfd != null) {
				fd = pfd.getFileDescriptor();
			}

			// 改善圖片資料已防止OOM
			options.inSampleSize = 1;
			// 只抓取圖片寬高
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, options);

			if (options.outHeight > 400 || options.outWidth > 400) {
				// 得到縮放
				int height = options.outHeight * 400 / options.outWidth;
				options.outWidth = 400;
				options.outHeight = height;
				options.inSampleSize = options.outWidth / 200;
				// 在傳回真正的圖片
				options.inJustDecodeBounds = false;
			}
			bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bm;
	}

}
