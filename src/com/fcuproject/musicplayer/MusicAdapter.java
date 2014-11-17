package com.fcuproject.musicplayer;

/*
 * 自訂Adapter用以配對資料欄位並顯示於list view中
 * 
 */

import java.util.ArrayList;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class MusicAdapter extends BaseAdapter {
	/* 宣告區域變數 */
	MainActivity mainActivity;
	ArrayList<MusicInfo> musicInfos;

	MusicAdapter(MainActivity activity, ArrayList<MusicInfo> musicInfos) {
		this.mainActivity = activity;
		this.musicInfos = musicInfos;
	}


	@Override
	public int getCount() {

		return musicInfos.size();
	}

	@Override
	public Object getItem(int position) {

		return null;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// 使用ViewHolder設計方法作為緩衝
		ViewHolder holder;
		View v = convertView;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) parent.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.vlist, parent, false);
			holder = new ViewHolder();
			holder.Title = (TextView) v.findViewById(R.id.title);
			holder.Album = (TextView) v.findViewById(R.id.album);
			holder.sButton = (ImageButton) v.findViewById(R.id.imgBtn_add);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		// 設置holder
		holder.Title.setText(musicInfos.get(position).getTitle());
		holder.Album.setText(musicInfos.get(position).getAlbum());

		// 加入清單按鈕監聽
		if (mainActivity.canAdd()) {
			holder.sButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					mainActivity.addBtnClicked(position);
				}
			});
		} else {
			holder.sButton.setVisibility(View.GONE);
		}

		// 設置歌曲背景變化
		if (musicInfos.get(position).getBackground() == true) {
			v.setBackgroundResource(R.drawable.deepskyblue);
		} else {
			v.setBackground(null);
		}

		return v;
	}

	public final class ViewHolder {
		public TextView Title = null;
		public TextView Album = null;
		public TextView Artist = null;
		public ImageButton sButton = null;
	}

}
