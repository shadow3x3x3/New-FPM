package com.fpmusicplayer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AlbumAdapter extends BaseAdapter {
	ArrayList<MusicInfo> musicInfos;

	AlbumAdapter(ArrayList<MusicInfo> musicInfos) {
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// 使用ViewHolder設計方法作為緩衝
		ViewHolder holder;
		View v = convertView;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) parent.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.albumlist, parent, false);
			holder = new ViewHolder();
			holder.Album = (TextView) v.findViewById(R.id.album);
			
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		// 設置holder
		holder.Album.setText(musicInfos.get(position).getAlbum());

		return v;
	}
	
	public final class ViewHolder {
		public TextView Title = null;
		public TextView Album = null;
		public TextView Artist = null;
	}
}
