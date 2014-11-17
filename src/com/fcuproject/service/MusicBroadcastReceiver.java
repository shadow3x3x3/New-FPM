package com.fcuproject.service;
//package com.service;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//
//import com.fpmusicplayer.MainActivity;
//
//public class MusicBroadcastReceiver extends BroadcastReceiver {
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		Log.d("BroadcastReceiver", "onReceive");
//		if (!intent.getAction().equals(
//				"android.intent.action.NEW_OUTGOING_CALL")) {
//			Log.d("BroadcastReceiver", "PhoneIn");
//			if (!MainActivity.ContextFragment.phoneQuiet) {
//				TelephonyManager telephonymanager = (TelephonyManager) context
//						.getSystemService(Context.TELEPHONY_SERVICE);
//				switch (telephonymanager.getCallState()) {
//				// 響鈴狀態
//				case TelephonyManager.CALL_STATE_RINGING:
//					MainActivity.playBtnAction(MainActivity.setActivity());
//					break;
//				// 掛斷狀態
//				case TelephonyManager.CALL_STATE_OFFHOOK:
//					MainActivity.playBtnAction(MainActivity.setActivity());
//					break;
//				default:
//					break;
//				}
//			}
//		} else {
//			MainActivity.playBtnAction(MainActivity.setActivity());
//		}
//
//	}
//
//}
