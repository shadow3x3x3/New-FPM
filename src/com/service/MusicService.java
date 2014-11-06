package com.service;

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

import com.fpmusicplayer.GlobalVariable;
import com.fpmusicplayer.MainActivity;
import com.fpmusicplayer.MusicDatabase;
import com.fpmusicplayer.MusicInfo;
import com.fpmusicplayer.R;

public class MusicService extends Service {
	/* 引入全域變數 */
	GlobalVariable globalVariable;

	public static MediaPlayer mediaPlayer;
	/* 宣告類別物件 */
	private ArrayList<MusicInfo> allMusicList;
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
		}
		
		switch (musicState) {
		case GlobalVariable.PLAY:
			if (mediaPlayer != null) {
				
				play(0);
			}
			break;
		case GlobalVariable.PAUSE:
			pause();
			break;
		case GlobalVariable.PRE:
			preSong();
			break;
		case GlobalVariable.NEXT:
			nextSong();
			break;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	// 播放方法
	private void play(int position) {
		// 得到音樂檔路徑
		PATH = globalVariable.getPlayingNow().getPath();
		Intent intentBroadcast = new Intent();
		intentBroadcast.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		sendBroadcast(intentBroadcast);
		sendNotification(globalVariable);
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(PATH);
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new PreparedListener(position));
			// 播放完監聽
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					// 是否為最後一首
					if (globalVariable.isLastOne()) { // 最後一首的情形
						intent = new Intent("theLastSong");
						sendBroadcast(intent);
						Toast.makeText(getApplicationContext(), "已經沒有下一首了", 1000).show();
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
		mediaPlayer.pause();
		musicState = GlobalVariable.PAUSE;
	}

	private void preSong() {
		if (globalVariable.getMusicCursor() > 0) { // 還沒到第一首
			globalVariable.setMusicCursor(globalVariable.getMusicCursor()-1);
			intent = new Intent("playPreSong");
			sendBroadcast(intent);
			play(0);
		} else {
			Toast.makeText(getApplicationContext(), "沒有上一首",
					Toast.LENGTH_SHORT).show();
		}
		
	}

	private void nextSong() {
		if (globalVariable.getMusicCursor() < globalVariable.getMusicListSize() - 1) { // 還沒到最後一首
			globalVariable.setMusicCursor(globalVariable.getMusicCursor()+1);
			intent = new Intent("playNextSong");  
	        sendBroadcast(intent);
		} else {
			Toast.makeText(getApplicationContext(), "沒有下一首",
					Toast.LENGTH_SHORT).show();
		}
		
        play(0);
	}

	private void getCurPlay() {
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
