package com.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.util.Log;

import com.fpmusicplayer.MainActivity;
import com.fpmusicplayer.MusicInfo;
import com.fpmusicplayer.R;

public class MusicService extends Service implements MusicState {

	/* 宣告類別物件 */
	public static MediaPlayer mediaPlayer = new MediaPlayer();
	private String PATH;
	private int musicState = IDLE;
	private Notification notification; // 通知
	private NotificationManager notificationManager; // 通知系統服務

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// 媒體方法
		if (intent != null) {
			musicState = intent.getIntExtra("MusicState", 1);
			// 是播放就得到音樂檔路徑
			if (musicState == PLAY) {
				PATH = intent.getStringExtra("PATH");
			}
		}
		Intent intentBroadcast = new Intent();
		switch (musicState) {
		case PLAY:
			if (mediaPlayer != null) {
				intentBroadcast.setAction("android.appwidget.action.APPWIDGET_UPDATE");
				sendBroadcast(intentBroadcast);
				sendNotification();
				play(0);
			}
			break;
		case PAUSE:
			pause();
			break;
		case EXIT:
			cancelNotification();
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
		musicState = PAUSE;

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
			musicState = PLAY;
			Log.d("service", "play");
			mediaPlayer.start();
			// 判斷是否從頭
			if (positon > 0) {
				mediaPlayer.seekTo(positon);
			}
		}
	}


	// 發送通知
	@SuppressWarnings("deprecation")
	public void sendNotification() {
		// 獲取對象
		MusicInfo musicInfo = MainActivity.getPlayingNow();
		notificationManager = (NotificationManager) this
				.getSystemService(Service.NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.icon = R.drawable.ic_launcher; // 設置圖標，公用圖標
		notification.tickerText = musicInfo.getTitle();
		notification.when = System.currentTimeMillis();
		notification.flags = Notification.FLAG_ONGOING_EVENT;

		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
				0);
		notification.setLatestEventInfo(this, musicInfo.getTitle(),
				musicInfo.getArtist(), pi);
		notificationManager.notify(1, notification);

	}
	
	// 取消通知
	public void cancelNotification() {
		notificationManager.cancel(1);
	}

}
