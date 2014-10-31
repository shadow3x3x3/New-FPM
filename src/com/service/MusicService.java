package com.service;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.util.Log;

import com.fpmusicplayer.GlobalVariable;
import com.fpmusicplayer.MainActivity;
import com.fpmusicplayer.MusicDatabase;
import com.fpmusicplayer.MusicInfo;
import com.fpmusicplayer.R;

public class MusicService extends Service {
	/* 引入全域變數 */
	GlobalVariable globalVariable;
	/* 宣告類別物件 */
	private ArrayList<MusicInfo> allMusicList;
	private MediaPlayer mediaPlayer;
	private MusicDatabase mDatabase;
	private String PATH;
	private Notification notification;
	private NotificationManager notificationManager;
	private int musicState;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		globalVariable = (GlobalVariable) getApplicationContext();
		mDatabase = new MusicDatabase();
		try {
			allMusicList = mDatabase.readMusic(globalVariable.getActivity());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		globalVariable.setAllMusicList(allMusicList);
		globalVariable.addIntoPlayList(allMusicList.get(0));
		mediaPlayer = new MediaPlayer();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 媒體方法
		if (intent != null) {
			musicState = intent.getIntExtra("MusicState", GlobalVariable.PLAY);
			// 是播放就得到音樂檔路徑
			if (musicState == GlobalVariable.PLAY) {
				PATH = intent.getStringExtra("PATH");
			}
		}
		Intent intentBroadcast = new Intent();
		switch (musicState) {	
		case GlobalVariable.PLAY:
			if (mediaPlayer != null) {
				intentBroadcast
						.setAction("android.appwidget.action.APPWIDGET_UPDATE");
				sendBroadcast(intentBroadcast);
				sendNotification(globalVariable);
				play(0);
			}
			break;
		case GlobalVariable.PAUSE:
			pause();
			break;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	// 播放方法 (從頭)
	private void play(int position) {
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(PATH);
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new PreparedListener(position));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 暫停方法
	private void pause() {
		Log.d("service", "pause");
		mediaPlayer.pause();
		musicState = GlobalVariable.PAUSE;
	}
	
	private void getCurPlay(){
		globalVariable.getPlayingNow();
	}

	// 準備監聽
	private final class PreparedListener implements OnPreparedListener {
		private int positon;

		public PreparedListener(int positon) {
			this.positon = positon;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			// 開始播放
			musicState = GlobalVariable.PLAY;
			Log.d("service", "play");
			mediaPlayer.start();
			// 判斷是否從頭
			if (positon > 0) {
				mediaPlayer.seekTo(positon);
			}
		}
	}

	// 發送通知
	public void sendNotification(GlobalVariable globalVariable) {
		// 獲取對象
		MusicInfo musicInfo = globalVariable.getPlayingNow();
		notificationManager = (NotificationManager) this
				.getSystemService(Service.NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(this);
		Intent intent = new Intent(this, MainActivity.class);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, 0);

		builder.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("FPMusicPlayer")
				.setContentText(musicInfo.getTitle())
				.setTicker("正在播放 " + musicInfo.getTitle())
				.setContentIntent(pendingIntent).setAutoCancel(false);

		notification = new Notification();
		notification.icon 		= R.drawable.ic_launcher; // 設置圖標，公用圖標
		notification.tickerText = musicInfo.getTitle();
		notification.flags 		= Notification.FLAG_ONGOING_EVENT;

		notificationManager.notify(R.drawable.ic_launcher, builder.build());

	}

	// 取消通知
	public void cancelNotification() {
		notificationManager.cancel(1);
	}

}
