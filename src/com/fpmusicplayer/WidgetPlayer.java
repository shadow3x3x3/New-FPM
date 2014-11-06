package com.fpmusicplayer;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetPlayer extends AppWidgetProvider{
	GlobalVariable globalVariable;
	private MusicInfo musicInfo;
	

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.d("Widget", "onUpdate Start");
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent textPendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);

		RemoteViews widgetViews = new RemoteViews(context.getPackageName(),
				R.layout.player_widget);

		ComponentName widgetPlayer = new ComponentName(context,
				WidgetPlayer.class);
		musicInfo = globalVariable.getPlayingNow();
		widgetViews.setTextViewText(R.id.W_songTitle, musicInfo.getTitle());
		widgetViews.setTextViewText(R.id.W_songAlbum, musicInfo.getAlbum());
		widgetViews.setOnClickPendingIntent(R.id.W_songTitle, textPendingIntent);
		widgetViews.setOnClickPendingIntent(R.id.W_songAlbum, textPendingIntent);

		appWidgetManager.updateAppWidget(widgetPlayer, widgetViews);
		Log.d("Widget", "onUpdate End");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.d("Widget", "onReceive");
		String action = intent.getAction();
		if (action.equals("android.appwidget.action.APPWIDGET_UPDATE")) {
			updateWidget(context);
			Log.d("Widget", "onReUpdate");
		}
	}

	private void updateWidget(Context context) {
		Log.d("Widget", "updateWidget Start");
		RemoteViews widgetViews = new RemoteViews(context.getPackageName(),
				R.layout.player_widget);
		musicInfo = globalVariable.getPlayingNow();
		widgetViews.setTextViewText(R.id.W_songTitle, musicInfo.getTitle());
		widgetViews.setTextViewText(R.id.W_songAlbum, musicInfo.getAlbum());
		Log.d("Widget", "Title:" + musicInfo.getTitle());
		AppWidgetManager am = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = am.getAppWidgetIds(new ComponentName(context,
				WidgetPlayer.class));
		am.updateAppWidget(appWidgetIds, widgetViews);
		Log.d("Widget", "updateWidget End");
	}
}
