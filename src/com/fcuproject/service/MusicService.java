package com.fcuproject.service;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.fcuproject.musicplayer.GlobalVariable;
import com.fcuproject.musicplayer.MainActivity;
import com.fcuproject.musicplayer.MusicDatabase;
import com.fcuproject.musicplayer.MusicInfo;
import com.fcuproject.musicplayer.R;

public class MusicService extends Service {
	/* 引入全域變數 */
	GlobalVariable globalVariable;

	public static MediaPlayer mediaPlayer;
	/* 宣告類別物件 */
	private MusicDatabase mDatabase;
	private String PATH;
	private Notification notification;
	private NotificationManager notificationManager;
	private Intent intent;
	private int musicState;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		globalVariable = (GlobalVariable) getApplicationContext();
		mediaPlayer = new MediaPlayer();
		
		// 媒體方法
		if (intent != null) {
			musicState = intent.getIntExtra("MusicState", GlobalVariable.PLAY);
			// 是播放就得到音樂檔路徑
		}

		switch (musicState) {
		case GlobalVariable.SCAN:
			scanMusic();
			break;
		case GlobalVariable.PLAY:
			if (mediaPlayer != null) {
				playAction();
			}
			break;
		case GlobalVariable.PRE:
			preSong();
			break;
		case GlobalVariable.NEXT:
			nextSong();
			break;
		}
		return super.onStartCommand(intent, Service.START_REDELIVER_INTENT, startId);
	}

	// 讀取音樂方法
	private void scanMusic() {
		Log.d("Service", "SCAN");
		mDatabase = new MusicDatabase();
		try {
			globalVariable.setAllMusicList(mDatabase.readMusic(globalVariable
					.getActivity()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//globalVariable.addIntoPlayList(globalVariable.getMusic(0));
	}

	// 判斷播放及暫停
	public void playAction() {
		// 第一次播放
		if (globalVariable.getPlayState().equals("FirstTimePlay")) {
			// music list 是否還有歌曲
			if (globalVariable.getMusicCursor() <= globalVariable
					.getPlayListSize()) {
				play(0);
				globalVariable.setIsPlaying(true);
			} else {
				Toast.makeText(getApplicationContext(), "沒有音樂檔",
						Toast.LENGTH_SHORT).show();
			}
			// 正在播放 執行暫停動作
		} else if (globalVariable.getPlayState().equals("PlayingNow")) {
			Log.d("service", "PlayingNow");			
			globalVariable.setMusicTempTime(mediaPlayer
					.getCurrentPosition());
			globalVariable.setIsPlaying(false);
			globalVariable.setIsPause(true);
			pause();
			// 正在暫停 執行播放動作
		} else if (globalVariable.getPlayState().equals("PauseNow")) {
			Log.d("service", "PauseNow");
			mediaPlayer.seekTo(globalVariable.getMusicTempTime());
			mediaPlayer.start();
			intent = new Intent("playMusic");
			sendBroadcast(intent);
			globalVariable.setIsPlaying(true);
			globalVariable.setIsPause(false);
		}
	}

	// 播放方法
	private void play(int position) {
		Log.d("service", "play");
		// 得到音樂檔路徑
		PATH = globalVariable.getPlayingNow().getPath();
		Intent intentBroadcast = new Intent();
		// 發送廣播
		intentBroadcast.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		sendBroadcast(intentBroadcast);
		intent = new Intent("playMusic");
		sendBroadcast(intent);
		sendNotification(globalVariable);
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(PATH);
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnPreparedListener(new PreparedListener(position));
			
			// 播放完監聽
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					// 是否為最後一首
					if (globalVariable.isLastOne()) { // 最後一首的情形
						intent = new Intent("theLastSong");
						sendBroadcast(intent);
						Toast.makeText(getApplicationContext(), "已經沒有下一首了"
								, 1000).show();
					} else { // 還沒到最後一首
						nextSong();
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 暫停方法
	private void pause() {
		Log.d("service", "pause");
		// 發送廣播
		intent = new Intent("pauseMusic");
		sendBroadcast(intent);
		musicState = GlobalVariable.PAUSE;
		mediaPlayer.pause();
		

	}

	// 上一首方法
	private void preSong() {
		Log.d("service", "preSong");
		if (globalVariable.getMusicCursor() > 0) { // 還沒到第一首
			globalVariable.minusMusicCursor();
			intent = new Intent("playPreSong");
			sendBroadcast(intent);
			play(0);
		} else {
			Toast.makeText(getApplicationContext(), "沒有上一首", Toast.LENGTH_SHORT)
					.show();
		}

	}

	// 下一首方法
	private void nextSong() {
		Log.d("service", "nextSong");
		if (globalVariable.getMusicCursor() < globalVariable.getPlayListSize() - 1) { // 還沒到最後一首
			Log.d("service", "nextSongOK");
			globalVariable.addMusicCursor();
			intent = new Intent("playNextSong");
			sendBroadcast(intent);
			play(0);
		} else {
			Log.d("service", "nextSongNOPE");
			Toast.makeText(getApplicationContext(), "沒有下一首", Toast.LENGTH_SHORT)
					.show();
		}

		play(0);
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
		notification.icon = R.drawable.ic_launcher; // 設置圖標，公用圖標
		notification.tickerText = musicInfo.getTitle();
		notification.flags = Notification.FLAG_ONGOING_EVENT;

		notificationManager.notify(R.drawable.ic_launcher, builder.build());

	}

	// 取消通知
	public void cancelNotification() {
		notificationManager.cancel(1);
	}

}
