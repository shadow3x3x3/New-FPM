package com.fpmusicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.fpmusicplayer.ScreenObserver.ScreenStateListener;
import com.service.MusicService;

//TODO Begin
@SuppressLint({ "InflateParams", "DefaultLocale" })
public class MainActivity extends FragmentActivity implements MainActivity.MainFragment.OnHeadlineSelectedListener{
	/* 引入全域變數 */
	GlobalVariable globalVariable;
	/* 宣告物件變數 */	
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private TextView songTitle, songAlbum, songArtist;
	private TextView songTime;
	private ImageButton imgBtn_play;
	private SeekBar songTimeBar;
	private View vLayout;
	private BitmapDrawable background;
	private ListView mlistView;
	private ArrayList<MusicInfo> musicInfos;
	private ArrayList<MusicInfo> playList;
	private ArrayList<MusicInfo> albumList;
	private ArrayList<MusicInfo> selectList;
	private ArrayList<MusicInfo> artistList;
	private ArrayList<MusicInfo> tempList;
	private ArrayList<MusicInfo> deleteList;
	private Handler handler = new Handler();
	private int musicTempTime = 0;
	private int spnPosition = 0;
	private boolean isPlaying = false;
	private boolean isPause = false;
	private boolean isExpand = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 設置窗口無標題
		Log.d("Activity", "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		globalVariable = (GlobalVariable) getApplicationContext();
		globalVariable.setMainActivity(this);

		viewPager_initial();

		Intent startIntent = new Intent(this, MusicService.class);
		startService(startIntent);

		playList = new ArrayList<MusicInfo>();
		albumList = new ArrayList<MusicInfo>();
		artistList = new ArrayList<MusicInfo>();
		selectList = new ArrayList<MusicInfo>();
		tempList = new ArrayList<MusicInfo>();
		deleteList = new ArrayList<MusicInfo>();

	}

	// Activity被強制關閉時結束通知
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO cancelNotification
		android.os.Process.killProcess(android.os.Process.myPid());

	}

	// Activity 關閉時保存資料
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d("Activity", "onSave");
		super.onSaveInstanceState(outState);
		outState.putInt("Cursor", globalVariable.getMusicCursor());
		outState.putBoolean("isPlaying", isPlaying);
		outState.putBoolean("ispause", isPause);

	}

	// 執行序處理進度條
	Runnable updateThread = new Runnable() {
		public void run() {
			// 獲得歌曲現在播放位置並設置成播放進度條的值
			songTimeBar.setProgress(MusicService.mediaPlayer
					.getCurrentPosition());
			// 獲得播放時間並更改text view
			musicTempTime = MusicService.mediaPlayer.getCurrentPosition();
			songTime.setText(MusicDatabase
					.formatTime((long) MusicService.mediaPlayer
							.getCurrentPosition()));
			handler.postDelayed(updateThread, 100);
		}
	};

	// ViewPager 初始化
	private void viewPager_initial() {
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(1);

	}

	public ViewPager getViewPager() {
		return mViewPager;
	}

	// 創造選項選單
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// 頁面
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		private static final int TAB_AMOUNT = 3;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		// 頁面選項
		@Override
		public Fragment getItem(int position) {
			final byte contex_fragment = 0;
			final byte main_fragment = 1;
			final byte scanner_fragment = 2;

			switch (position) {
			case contex_fragment:
				return new ContextFragment();
			case main_fragment:
				return new MainFragment();
			case scanner_fragment:
				return new ScannerFragment();

			}
			return null;
		}

		@Override
		public int getCount() {
			return TAB_AMOUNT;
		}

	}

	/**
	 * 各頁面函式
	 */

	// fragment_main - 播放頁面
	// TODO main
	public class MainFragment extends Fragment {
		ImageButton imgBtn_next, imgBtn_pre;
		OnHeadlineSelectedListener mCallback; 

		public MainFragment() {
		}
		
		public interface OnHeadlineSelectedListener {  
	        public void onArticleSelected(int position);  
	    }
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_main, null);

			MainFragment_initail(view);

			setMainState(0);

			// 播放條監聽
			songTimeBar
					.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							// 改變音樂進度
							if (fromUser == true) {
								MusicService.mediaPlayer.seekTo(progress);
							}
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
						}
					});

			return view;
		}
		
		@Override   
	    public void onAttach(Activity activity) {   
	        super.onAttach(activity);   
	           
	        // This makes sure that the container activity has implemented   
	        // the callback interface. If not, it throws an exception   
	        try {   
	            mCallback = (OnHeadlineSelectedListener) activity;   
	        } catch (ClassCastException e) {   
	            throw new ClassCastException(activity.toString()   
	                    + " must implement OnHeadlineSelectedListener");   
	        }   
	    }

		// MainFragment 初始化
		public void MainFragment_initail(View view) {
			/* Build */
			songTitle = (TextView) view.findViewById(R.id.songTitle);
			songAlbum = (TextView) view.findViewById(R.id.songAlbum);
			songArtist = (TextView) view.findViewById(R.id.songArtist);
			songTime = (TextView) view.findViewById(R.id.songTime);
			imgBtn_play = (ImageButton) view.findViewById(R.id.imgBtn_add);
			imgBtn_next = (ImageButton) view.findViewById(R.id.imgBtn_next);
			imgBtn_pre = (ImageButton) view.findViewById(R.id.imgBtn_pre);

			songTimeBar = (SeekBar) view.findViewById(R.id.songTimeBar);
			vLayout = view.findViewById(R.id.MusicLayout);
			/* Execution */
			imgBtn_play.setOnClickListener(btnMusicListener);
			imgBtn_next.setOnClickListener(btnMusicListener);
			imgBtn_pre.setOnClickListener(btnMusicListener);
			/* 設置頁面歌曲 */
			playList.add(0, musicInfos.get(0));
			songTimeBar.setEnabled(false);
			songTimeBar.setMax((int) playList.get(0).getDuration());

			// 播放完成監聽
			MusicService.mediaPlayer
					.setOnCompletionListener(new OnCompletionListener() {

						int musicCursor = globalVariable.getMusicCursor();

						@Override
						public void onCompletion(MediaPlayer mp) {
							// 是否為最後一首
							if (musicCursor < playList.size() - 1) { // 還沒到最後一首
								musicCursor++;
								playMusic(getActivity());

							} else if (musicCursor == playList.size() - 1) { // 最後一首的情形
								imgBtn_play
										.setImageResource(R.drawable.player_play);
								handler.removeCallbacks(updateThread);
								songTimeBar.setProgress(0);
								songTimeBar.setEnabled(false);
								isPause = false;
								isPlaying = false;

							}

						}

					});

		}

		// 基本控制
		private Button.OnClickListener btnMusicListener = new Button.OnClickListener() {
			int musicCursor = globalVariable.getMusicCursor();

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				// 播放暫停鍵
				case R.id.imgBtn_add: {
					playBtnAction(getActivity());
					break;
				}
				case R.id.imgBtn_pre: {
					if (musicCursor > 0) { // 還沒到第一首
						musicCursor--;
						playMusic(getActivity());
					} else {
						Toast.makeText(getActivity(), "沒有上一首",
								Toast.LENGTH_SHORT).show();
					}
					break;
				}
				case R.id.imgBtn_next: {
					if (musicCursor < playList.size() - 1) { // 還沒到最後一首
						musicCursor++;
						playMusic(getActivity());
					} else {
						Toast.makeText(getActivity(), "沒有下一首",
								Toast.LENGTH_SHORT).show();
					}
					break;
				}

				}
			}

		};

	}

	// fragment_scanner - 音樂選取頁面
	// TODO scanner
	public class ScannerFragment extends Fragment {
		private MusicAdapter mAdapter;
		private MusicAdapter selectAdapter;
		private MusicAdapter findAdapter;
		private Spinner spnPrefer;
		private EditText searchEdit;
		private int selectPosition = 0;
		private int countA = 0, countB = 0, flag = 0;
		private int Pos = 0;
		private static final int showPlaylist = 0;
		private static final int sortWithName = 1;
		private static final int sortWithAlbum = 2;
		private static final int sortWithArtist = 3;
		private static final int findList = 4;
		ModeCallback mCallback = new ModeCallback();

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_scanner, null);

			// 使用自定Adapter顯示歌曲清單
			mlistView = (ListView) view.findViewById(R.id.musiclist);
			mAdapter = new MusicAdapter(playList);
			mlistView.setAdapter(mAdapter);

			// 下拉式清單
			spnPrefer = (Spinner) view.findViewById(R.id.sortlist);

			ArrayAdapter<CharSequence> adapterSort = ArrayAdapter
					.createFromResource(getActivity(), R.array.Sort,
							android.R.layout.simple_spinner_item);

			adapterSort
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spnPrefer.setAdapter(adapterSort);

			spnPrefer.setOnItemSelectedListener(spnPreferListener);

			searchEdit = (EditText) view.findViewById(R.id.find_edit);
			searchEdit.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

					if (searchEdit.getText().length() != 0) {
						searchHandler.post(searchThread);
						spnPosition = findList;
					}

				}

				@Override
				public void afterTextChanged(Editable s) {

				}
			});

			// 點擊list view項目的動作
			mlistView.setOnItemClickListener(new OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (musicInfos != null) {
						// 抓取當前位置的資訊
						MusicInfo musicInfo = musicInfos.get(position);
						// 判斷用哪個list來抓取位置
						if (spnPosition == showPlaylist) { // 顯示播放清單時
							globalVariable.setMusicCursor(position);
							((MainActivity) getActivity()).getViewPager()
									.setCurrentItem(1);
							playMusic(getActivity());
						} else if (spnPosition == sortWithName) { // 根據名字排列時
							musicInfo = musicInfos.get(position);

							// 設置main Fragment 狀態與外觀
							playList.clear();
							playList.add(0, musicInfo);
							imgBtn_play
									.setImageResource(R.drawable.player_pause);
							isPlaying = true;
							songTimeBar.setMax((int) playList.get(0)
									.getDuration());
							setMainState(0);
							((MainActivity) getActivity()).getViewPager()
									.setCurrentItem(1);
							playMusic(getActivity());

						} else if (spnPosition == sortWithAlbum) { // 根據專輯排列時
							Log.d("isExpand", String.valueOf(isExpand));
							// 沒有展開的情況下 將選到的專輯裡的所有歌存到selectList
							if (isExpand == false) {
								Log.d("isExpand False",
										String.valueOf(isExpand));
								selectList.clear();
								for (countA = 0; countA < musicInfos.size(); countA++) {
									if (musicInfos
											.get(countA)
											.getAlbum()
											.equals(albumList.get(position)
													.getAlbum()) == true) {
										selectList.add(musicInfos.get(countA));
									}
								}
								// 顯示選到的album
								selectAdapter = new MusicAdapter(selectList);
								mlistView.setAdapter(selectAdapter);
								isExpand = true;
								Log.d("isExpand True", String.valueOf(isExpand));
							} else { // 展開的情況下 依據選取的位置加到播放清單
								Log.d("isExpand True", String.valueOf(isExpand));
								musicInfo = selectList.get(position);
								globalVariable.setMusicCursor(position);
								// 設置播放清單
								playList.clear();
								for (int listPointer = 0; listPointer < selectList
										.size(); listPointer++) {
									musicInfo = selectList.get(listPointer);
									playList.add(musicInfo);

								}
								// 設置main Fragment 狀態與外觀
								imgBtn_play
										.setImageResource(R.drawable.player_pause);
								songTimeBar.setMax((int) playList.get(position)
										.getDuration());
								((MainActivity) getActivity()).getViewPager()
										.setCurrentItem(1);
								playMusic(getActivity());

							}

						} else if (spnPosition == sortWithArtist) { // 根據歌手排列時
							// 沒有展開的情況下 依據選取的位置加到播放清單
							if (isExpand == false) {
								selectList.clear();
								for (countA = 0; countA < musicInfos.size(); countA++) {
									if (musicInfos
											.get(countA)
											.getArtist()
											.equals(artistList.get(position)
													.getArtist()) == true) {
										selectList.add(musicInfos.get(countA));
									}
								}
								// 顯示選到的artist
								selectAdapter = new MusicAdapter(selectList);
								mlistView.setAdapter(selectAdapter);
								isExpand = true;
							} else { // 展開的情況下 將選到的歌手裡的所有歌存到selectList
								musicInfo = selectList.get(position);
								// 設置main Fragment 狀態與外觀
								playList.clear();
								playList.add(0, musicInfo);
								imgBtn_play
										.setImageResource(R.drawable.player_pause);
								songTimeBar.setMax((int) playList.get(0)
										.getDuration());
								((MainActivity) getActivity()).getViewPager()
										.setCurrentItem(1);
								playMusic(getActivity());

							}

						} else if (spnPosition == findList) { // 根據搜尋排列時
							musicInfo = selectList.get(position);
							// 設置main Fragment 狀態與外觀
							playList.clear();
							playList.add(0, musicInfo);
							imgBtn_play
									.setImageResource(R.drawable.player_pause);
							songTimeBar.setMax((int) playList.get(0)
									.getDuration());
							setMainState(0);
							((MainActivity) getActivity()).getViewPager()
									.setCurrentItem(1);
							playMusic(getActivity());
						}

					}
				}
			});

			mlistView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			mlistView.setMultiChoiceModeListener(mCallback);
			return view;

		}
		
		

		private class ModeCallback implements ListView.MultiChoiceModeListener {

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// TODO Auto-generated method stub
				switch (item.getItemId()) {
				case R.id.action_add:
					for (countA = 0; countA < tempList.size(); countA++) {
						tempList.get(countA).setBackground(false);
						playList.add(tempList.get(countA));
					}
					tempList.clear();
					mode.finish();
					return true;
				case R.id.action_del:
					for (countA = 0; countA < deleteList.size(); countA++) {
						deleteList.get(countA).setBackground(false);
						playList.remove(deleteList.get(countA));
					}
					deleteList.clear();
					mode.finish();
					return true;
				case R.id.action_allselect:
					tempList.clear();
					deleteList.clear();
					if (spnPosition == sortWithName) {
						for (countA = 0; countA < musicInfos.size(); countA++) {
							tempList.add(musicInfos.get(countA));
							musicInfos.get(countA).setBackground(true);
						}
						mAdapter.notifyDataSetChanged();
					} else if (isExpand == true) {
						for (countA = 0; countA < selectList.size(); countA++) {
							tempList.add(selectList.get(countA));
							selectList.get(countA).setBackground(true);
						}
						selectAdapter.notifyDataSetChanged();
					} else if (spnPosition == showPlaylist) {
						for (countA = 0; countA < playList.size(); countA++) {
							deleteList.add(playList.get(countA));
							playList.get(countA).setBackground(true);
						}
						mAdapter.notifyDataSetChanged();
					} else if (spnPosition == findList) {
						for (countA = 0; countA < selectList.size(); countA++) {
							tempList.add(selectList.get(countA));
							selectList.get(countA).setBackground(true);
						}
						findAdapter.notifyDataSetChanged();
					}
					return true;
				case R.id.action_allcancle:
					if (spnPosition == sortWithName) {
						for (countA = 0; countA < musicInfos.size(); countA++) {
							musicInfos.get(countA).setBackground(false);
						}
						mAdapter.notifyDataSetChanged();
					} else if (isExpand == true) {
						for (countA = 0; countA < selectList.size(); countA++) {
							selectList.get(countA).setBackground(false);
						}
						selectAdapter.notifyDataSetChanged();
					} else if (spnPosition == showPlaylist) {
						for (countA = 0; countA < playList.size(); countA++) {
							playList.get(countA).setBackground(false);
						}
						mAdapter.notifyDataSetChanged();
					} else if (spnPosition == findList) {
						for (countA = 0; countA < selectList.size(); countA++) {
							selectList.get(countA).setBackground(false);
						}
						findAdapter.notifyDataSetChanged();
					}
					tempList.clear();
					deleteList.clear();
					return true;
				default:
					return false;
				}

			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				if (spnPosition == sortWithName || spnPosition == findList
						|| isExpand == true) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.action_mode_addmenu, menu);

				} else if (spnPosition == showPlaylist) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.action_mode_delmenu, menu);
				} else {
					return false;
				}

				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
				Log.d("sss", "ondestroyActionMode");
				if (spnPosition == sortWithName) {
					for (countA = 0; countA < musicInfos.size(); countA++) {
						musicInfos.get(countA).setBackground(false);
					}
					mAdapter.notifyDataSetChanged();
				} else if (isExpand == true) {
					for (countA = 0; countA < selectList.size(); countA++) {
						selectList.get(countA).setBackground(false);
					}
					selectAdapter.notifyDataSetChanged();
				} else if (spnPosition == showPlaylist) {
					for (countA = 0; countA < playList.size(); countA++) {
						playList.get(countA).setBackground(false);
					}
					mAdapter.notifyDataSetChanged();
				} else if (spnPosition == findList) {
					for (countA = 0; countA < selectList.size(); countA++) {
						selectList.get(countA).setBackground(false);
					}
					findAdapter.notifyDataSetChanged();
				}
				tempList.clear();
				deleteList.clear();
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				Log.d("sss", "onPrepareActionMode");
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				// TODO Auto-generated method stub

				// mlistView.setSelection(position);
				Log.d("sss", "position=" + position);
				Log.d("sss", "check=" + checked);
				if (checked == true) {
					if (spnPosition == sortWithName) {
						tempList.add(musicInfos.get(position));
						musicInfos.get(position).setBackground(checked);
						mAdapter.notifyDataSetChanged();
					} else if (isExpand == true) {
						tempList.add(selectList.get(position));
						selectList.get(position).setBackground(checked);
						selectAdapter.notifyDataSetChanged();
					} else if (spnPosition == showPlaylist) {
						deleteList.add(playList.get(position));
						playList.get(position).setBackground(checked);
						mAdapter.notifyDataSetChanged();
					} else if (spnPosition == findList) {
						tempList.add(selectList.get(position));
						selectList.get(position).setBackground(checked);
						findAdapter.notifyDataSetChanged();
					}
				} else {
					if (spnPosition == sortWithName) {
						tempList.remove(musicInfos.get(position));
						musicInfos.get(position).setBackground(checked);
						mAdapter.notifyDataSetChanged();
					} else if (isExpand == true) {
						tempList.remove(selectList.get(position));
						selectList.get(position).setBackground(checked);
						selectAdapter.notifyDataSetChanged();
					} else if (spnPosition == showPlaylist) {
						deleteList.remove(playList.get(position));
						playList.get(position).setBackground(checked);
						mAdapter.notifyDataSetChanged();
					} else if (spnPosition == findList) {
						tempList.remove(selectList.get(position));
						selectList.get(position).setBackground(checked);
						findAdapter.notifyDataSetChanged();
					}
				}

			}

		}

		// 根據選單項目做動作
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			switch (item.getItemId()) {

			case 1:
				if (spnPosition == sortWithName) {
					tempList.add(musicInfos.get(Pos));
				} else {
					tempList.add(selectList.get(Pos));
				}
				break;
			case 2:
				for (countA = 0; countA < tempList.size(); countA++) {
					playList.add(tempList.get(countA));
				}
				tempList.clear();
				break;

			default:
				break;
			}

			return super.onContextItemSelected(item);
		}

		// 點擊下拉式清單的動作
		private Spinner.OnItemSelectedListener spnPreferListener = new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				spnPosition = position;
				switch (position) {
				case showPlaylist:
					mAdapter = new MusicAdapter(playList);
					mlistView.setAdapter(mAdapter);
					isExpand = false;
					break;

				case sortWithName:
					mAdapter = new MusicAdapter(musicInfos);
					mlistView.setAdapter(mAdapter);
					isExpand = false;
					break;

				case sortWithAlbum:
					// 依專輯排序
					sortalbumHandler.post(sortalbumThread);
					isExpand = false;
					break;

				case sortWithArtist:
					// 依歌手排序
					sortartistHandler.post(sortartistThread);
					isExpand = false;
					break;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		};

		// 執行序處理進度條
		Handler handler = new Handler();
		Runnable updateThread = new Runnable() {
			public void run() {
				// 獲得歌曲現在播放位置並設置成播放進度條的值
				songTimeBar.setProgress(MusicService.mediaPlayer
						.getCurrentPosition());
				// 獲得播放時間並更改text view
				musicTempTime = MusicService.mediaPlayer.getCurrentPosition();
				songTime.setText(MusicDatabase
						.formatTime((long) MusicService.mediaPlayer
								.getCurrentPosition()));
				handler.postDelayed(updateThread, 100);
			}
		};

		// 執行緒處理搜尋
		Handler searchHandler = new Handler();
		Runnable searchThread = new Runnable() {
			public void run() {
				Log.d("searchthread", "thread");
				selectList.clear();
				for (countA = 0; countA < musicInfos.size(); countA++) {
					// 如果歌名有搜尋到
					if (musicInfos
							.get(countA)
							.getTitle()
							.toLowerCase()
							.indexOf(
									searchEdit.getText().toString()
											.toLowerCase()) >= 0) {
						Log.d("search", "歌名"
								+ musicInfos.get(countA).getTitle());
						selectList.add(musicInfos.get(countA));
					}

				}

				for (countA = 0; countA < musicInfos.size(); countA++) {
					// 如果專輯有搜尋到
					if (musicInfos
							.get(countA)
							.getAlbum()
							.toLowerCase()
							.indexOf(
									searchEdit.getText().toString()
											.toLowerCase()) >= 0) {
						flag = 0;// 判斷是否已經在list裡面
						for (countB = 0; countB < selectList.size(); countB++) {
							if (musicInfos.get(countA).getId() == selectList
									.get(countB).getId()) {
								flag = 1;
							}
						}
						if (flag == 0) {
							Log.d("search", "歌手名稱"
									+ musicInfos.get(countA).getTitle());
							Log.d("search", "歌手"
									+ musicInfos.get(countA).getArtist());
							selectList.add(musicInfos.get(countA));
						}
					}

				}

				for (countA = 0; countA < musicInfos.size(); countA++) {
					// 如果專輯有搜尋到
					if (musicInfos
							.get(countA)
							.getArtist()
							.toLowerCase()
							.indexOf(
									searchEdit.getText().toString()
											.toLowerCase()) >= 0) {
						flag = 0;// 判斷是否已經在list裡面
						for (countB = 0; countB < selectList.size(); countB++) {
							if (musicInfos.get(countA).getId() == selectList
									.get(countB).getId()) {
								flag = 1;
							}
						}
						if (flag == 0) {
							Log.d("search", "歌手名稱"
									+ musicInfos.get(countA).getTitle());
							Log.d("search", "歌手"
									+ musicInfos.get(countA).getArtist());
							selectList.add(musicInfos.get(countA));
						}
					}
				}

				MusicAdapter findAdapter = new MusicAdapter(selectList);
				mlistView.setAdapter(findAdapter);
			}
		};
		Handler sortalbumHandler = new Handler();
		Runnable sortalbumThread = new Runnable() {
			public void run() {
				// 依專輯排序
				Collections.sort(musicInfos, new Comparator<MusicInfo>() {
					public int compare(MusicInfo o1, MusicInfo o2) {
						return o1.getAlbum().compareTo(o2.getAlbum());
					}
				});
				// 將專輯清單不重複顯示出來
				for (countA = 0; countA < musicInfos.size(); countA++) {
					flag = 0;
					for (countB = 0; countB < albumList.size(); countB++) {
						if (musicInfos.get(countA).getAlbum()
								.equals(albumList.get(countB).getAlbum()) == true) {
							flag = 1;
						}
					}

					if (flag == 0) {
						albumList.add(musicInfos.get(countA));
					}
				}

				// 顯示所有專輯
				AlbumAdapter alAdapter = new AlbumAdapter(albumList);
				mlistView.setAdapter(alAdapter);
			}
		};

		Handler sortartistHandler = new Handler();
		Runnable sortartistThread = new Runnable() {
			public void run() {
				// 依歌手排序
				Collections.sort(musicInfos, new Comparator<MusicInfo>() {
					public int compare(MusicInfo o1, MusicInfo o2) {
						return o1.getArtist().compareTo(o2.getArtist());
					}
				});
				// 將歌手清單不重複顯示出來
				for (countA = 0; countA < musicInfos.size(); countA++) {
					flag = 0;
					for (countB = 0; countB < artistList.size(); countB++) {
						if (musicInfos.get(countA).getArtist()
								.equals(artistList.get(countB).getArtist()) == true) {
							flag = 1;
						}
					}

					if (flag == 0) {
						artistList.add(musicInfos.get(countA));
					}
				}

				// 顯示所有歌手
				ArtistAdapter arAdapter = new ArtistAdapter(artistList);
				mlistView.setAdapter(arAdapter);
			}
		};

		Handler selectHandler = new Handler();
		Runnable selectThread = new Runnable() {
			public void run() {
				switch (spnPosition) {

				case sortWithAlbum:
					selectList.clear();
					for (countA = 0; countA < musicInfos.size(); countA++) {
						if (musicInfos
								.get(countA)
								.getAlbum()
								.equals(albumList.get(selectPosition)
										.getAlbum()) == true) {
							selectList.add(musicInfos.get(countA));
						}
					}
					// 顯示選到的album
					MusicAdapter selectalbumAdapter = new MusicAdapter(
							selectList);
					mlistView.setAdapter(selectalbumAdapter);
					break;

				case sortWithArtist:
					selectList.clear();
					for (countA = 0; countA < musicInfos.size(); countA++) {
						if (musicInfos
								.get(countA)
								.getArtist()
								.equals(artistList.get(selectPosition)
										.getArtist()) == true) {
							selectList.add(musicInfos.get(countA));
						}
					}
					// 顯示選到的artist
					MusicAdapter selectartistAdapter = new MusicAdapter(
							selectList);
					mlistView.setAdapter(selectartistAdapter);

					break;
				}
			}
		};

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Log.d("back", "3");
			// 如果是展開的情況下返回
			if (isExpand == true) {
				switch (spnPosition) {

				case MainActivity.ScannerFragment.sortWithAlbum:
					AlbumAdapter alAdapter = new AlbumAdapter(albumList);
					mlistView.setAdapter(alAdapter);
					isExpand = false;
					break;

				case MainActivity.ScannerFragment.sortWithArtist:
					ArtistAdapter arAdapter = new ArtistAdapter(artistList);
					mlistView.setAdapter(arAdapter);
					isExpand = false;
					break;

				}
				Log.d("back", String.valueOf(isExpand));
			}
			// 其他情況 變成背景模式
			else {
				moveTaskToBack(true);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// fragment_context - 情境模式頁面
	// TODO context
	public class ContextFragment extends Fragment {
		private ListView cListView;
		private LinearLayout setLayout;
		private View quietView;
		private View commuterView;
		private View longHaulView;
		private View customView;
		private SensorManager cSensorManager;
		private Sensor cSensor;
		private SensorEventListener sListener;
		private Switch mediaOnlySw;
		private Switch sensorCtrSw;
		private Switch earPrtSw;
		private EditText prtTimEditText;
		private EditText sTimeEditText;
		private Button crtSuiteBtn;
		private AudioManager audioManager;
		private ScreenObserver mScreenObserver;
		private float gravityX = 0;
		private float gravityY = 0;
		private float gravityZ = 0;
		private int prtVolume;
		private int curMode;
		private boolean isIdleMode = false;
		public boolean phoneQuiet = false;
		private static final int noneMode = 0;
		private static final int quietMode = 1;
		private static final int commuterMode = 2;
		private static final int longHaulMode = 3;
		private static final int customMode = 4;

		public ContextFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_context, null);

			ContextFragment_initail(inflater, view);

			// layout
			cListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					switch (position) {
					case noneMode:
						curMode = noneMode;
						setLayout.removeAllViews();
						listenTurn(curMode);
						break;

					case quietMode:
						if (curMode != quietMode) {
							sensorCtrSw.setChecked(false);
							mediaOnlySw.setChecked(false);
						}
						curMode = quietMode;
						setLayout.removeAllViews();
						setLayout.addView(quietView);
						listenTurn(curMode);
						break;

					case commuterMode:
						curMode = commuterMode;
						setLayout.removeAllViews();
						setLayout.addView(commuterView);
						listenTurn(curMode);
						break;

					case longHaulMode:
						curMode = longHaulMode;
						setLayout.removeAllViews();
						setLayout.addView(longHaulView);
						listenTurn(curMode);
						break;

					case customMode:
						curMode = customMode;
						setLayout.removeAllViews();
						setLayout.addView(customView);
						listenTurn(curMode);
						break;
					}
				}
			});

			// Sensor 監聽
			sListener = new SensorEventListener() {
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
				}

				@SuppressWarnings("deprecation")
				public void onSensorChanged(SensorEvent event) {
					gravityX = event.values[SensorManager.DATA_X];
					gravityY = event.values[SensorManager.DATA_Y];
					gravityZ = event.values[SensorManager.DATA_Z];

					doWithGravityChange(gravityX, gravityY, gravityZ);
				}
			};

			return view;
		}

		private void ContextFragment_initail(LayoutInflater inflater, View view) {
			setLayout = (LinearLayout) view.findViewById(R.id.modeSetLayout);
			quietView = inflater.inflate(R.layout.quiet_layout, null);
			commuterView = inflater.inflate(R.layout.commuter_layout, null);
			longHaulView = inflater.inflate(R.layout.longhaul_layout, null);
			customView = inflater.inflate(R.layout.custom_layout, null);

			// 服務類build
			audioManager = (AudioManager) getActivity().getSystemService(
					AUDIO_SERVICE);
			cSensorManager = (SensorManager) getActivity().getSystemService(
					SENSOR_SERVICE);
			if (cSensorManager == null) {
				Log.d("Context", "No Sensor");
			}
			cSensor = cSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			/* 元件build */// TODO build in fragment
			// quite 頁面
			mediaOnlySw = (Switch) quietView.findViewById(R.id.music_only_sw);
			sensorCtrSw = (Switch) quietView.findViewById(R.id.sensor_control);

			// commuter 頁面
			sTimeEditText = (EditText) commuterView
					.findViewById(R.id.editText_suiteTime);
			crtSuiteBtn = (Button) commuterView
					.findViewById(R.id.btn_creatSuite);

			// longHaul 頁面
			earPrtSw = (Switch) longHaulView.findViewById(R.id.sw_earPrtd);
			prtTimEditText = (EditText) longHaulView
					.findViewById(R.id.editText_prtdTime);
			prtTimEditText.setEnabled(false);
			try {
				mScreenObserver = new ScreenObserver(getActivity());
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// list view
			String[] contextMode = new String[] { "無情境模式", "勿擾模式", "通勤模式",
					"長途模式", "使用者自訂" };

			cListView = (ListView) view.findViewById(R.id.contextListView);
			cListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

			ArrayAdapter<String> vArrayData = new ArrayAdapter<String>(
					this.getActivity(),
					android.R.layout.simple_list_item_single_choice,
					contextMode);
			cListView.setAdapter(vArrayData);

		}

		// 按鈕是否監聽 // TODO listenTurn
		private void listenTurn(int curMode) {
			// quiet Mode 下的監聽判斷
			if (curMode == quietMode) {
				mediaOnlySw
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean switchON) {
								if (switchON) {
									changeAudio(switchON);
									phoneQuiet = true;
								} else {
									changeAudio(switchON);
									phoneQuiet = false;
								}
							}
						});
				sensorCtrSw
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean switchON) {
								if (switchON) {
									cSensorManager.registerListener(sListener,
											cSensor,
											SensorManager.SENSOR_DELAY_NORMAL);
								} else {
									cSensorManager
											.unregisterListener(sListener);
								}
							}
						});
			} else {
				mediaOnlySw.setOnCheckedChangeListener(null);
				sensorCtrSw.setOnCheckedChangeListener(null);
				changeAudio(false);
				cSensorManager.unregisterListener(sListener);
			}

			// commuter Mode 下的監聽判斷
			if (curMode == commuterMode) {
				crtSuiteBtn.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (Integer.valueOf(sTimeEditText.getText().toString()) >= 5) {
							randomSuite(Long.valueOf(sTimeEditText.getText()
									.toString()));
						} else {
							Toast.makeText(getActivity(), "請至少設定五分鐘或以上！",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
			} else {

			}
			// longHaul Mode 下的監聽判斷
			if (curMode == longHaulMode) {
				// 啟動開關
				earPrtSw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean switchON) {
						if (switchON) {
							prtTimEditText.setEnabled(true);
							isIdleMode = true;
							Toast.makeText(getActivity(), "已經設定在待機時降低音量為 2",
									Toast.LENGTH_SHORT).show();

						} else {
							prtTimEditText.setEnabled(false);

						}
					}
				});

				// 時間編輯
				prtTimEditText
						.setOnEditorActionListener(new OnEditorActionListener() {
							@Override
							public boolean onEditorAction(TextView v,
									int actionId, KeyEvent event) {
								prtVolume = Integer.valueOf(prtTimEditText
										.getText().toString());
								if (prtVolume < 5) {
									Toast.makeText(
											getActivity(),
											"已經設定在待機時降低音量為 "
													+ Long.toString(prtVolume),
											Toast.LENGTH_SHORT).show();
									isIdleMode = true;

								} else {
									Toast.makeText(getActivity(),
											"無法在降低音量至5以上", Toast.LENGTH_SHORT)
											.show();
								}
								return false;
							}

						});

				// 螢幕判斷
				mScreenObserver
						.requestScreenStateUpdate(new ScreenStateListener() {
							@Override
							public void onScreenOn() {
							}

							@Override
							public void onScreenOff() {
								if (isIdleMode) {
									audioManager.setStreamVolume(
											AudioManager.STREAM_MUSIC,
											prtVolume, 0);
								}
							}
						});

			} else {
				earPrtSw.setOnClickListener(null);
				prtTimEditText.addTextChangedListener(null);
				mScreenObserver.requestScreenStateUpdate(null);

			}
		} // listenTurn End

		// 改變音量函式
		private void changeAudio(Boolean musicOnly) {
			int ringVolume, ntfVolume, sysVolume;

			ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
			ntfVolume = audioManager
					.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
			sysVolume = audioManager
					.getStreamVolume(AudioManager.STREAM_SYSTEM);

			if (musicOnly) {
				audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION,
						false);
				audioManager.setStreamMute(AudioManager.STREAM_RING, false);
				audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

			} else {
				;
				audioManager.setStreamVolume(AudioManager.STREAM_RING,
						ringVolume, 0);
				audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
						ntfVolume, 0);
				audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
						sysVolume, 0);

			}
		}

		// 重力感應改變後的動作
		private void doWithGravityChange(float gravityX, float gravityY,
				float gravityZ) {
			int musicCursor = globalVariable.getMusicCursor();
			// 暫停與播放
			if (gravityY > 4) {
				Log.d("Context", "Up");
				if (isPlaying == true) {
					playBtnAction(getActivity());
				}

			} else {
				Log.d("Context", "Lie");
				if (isPause == true) {
					playBtnAction(getActivity());
				}

			}

			// 換首
			if (gravityX > 3 && gravityZ > 0) {
				Log.d("Context", "Left");
				if (globalVariable.getMusicCursor() > 0) { // 還沒到第一首
					musicCursor--;
					playMusic(getActivity());

				}

			} else if (gravityX < -3 && gravityZ > 0) {
				Log.d("Context", "Right");
				if (musicCursor < playList.size() - 1) { // 還沒到最後一首
					musicCursor++;
					playMusic(getActivity());
				}
			}
		}

		// 自動組曲函式
		private void randomSuite(long maxTime) {
			maxTime = maxTime * 60 * 1000;
			List<Integer> randomAll = new ArrayList<>();
			int count = 0;
			long tempTime = 0;

			// 初始化一個array list 將所有可以取得的數放入
			while (count < musicInfos.size()) {
				randomAll.add(count);
				count++;
			}

			// 在時間還沒超過前
			while (tempTime < maxTime) {
				Random random = new Random();
				int randomTemp = random.nextInt(musicInfos.size());
				if (randomAll.contains(randomTemp)) {
					randomAll.remove(randomTemp);
					tempTime = tempTime
							+ musicInfos.get(randomTemp).getDuration();
					// 加入播放清單
					playList.clear();
					playList.add(musicInfos.get(randomTemp));
					playMusic(getActivity());
					Log.d("randomSuite", Long.toString(randomTemp));
				} else {
					continue;
				}

			}
		}

	} // Context End

	/* 全域方法 */// TODO 全域方法
	// 播放及暫停方法
	public void playBtnAction(Activity activity) {
		// 第一次播放
		if (isPlaying == false && isPause == false) {
			// music list 是否還有歌曲
			if (globalVariable.getMusicCursor() <= playList.size()) {
				playMusic(activity);
			} else {
				Toast.makeText(activity, "沒有音樂檔", Toast.LENGTH_SHORT).show();
			}
			// 正在播放 執行暫停動作
		} else if (isPlaying == true) {
			Log.d("MAIN", "PAUSE");
			imgBtn_play.setImageResource(R.drawable.player_play);
			Intent intent = new Intent();
			intent.putExtra("MusicState", GlobalVariable.PAUSE);
			intent.setClass(activity, MusicService.class);
			activity.startService(intent);
			musicTempTime = MusicService.mediaPlayer.getCurrentPosition();
			isPlaying = false;
			isPause = true;
			// 正在暫停 執行播放動作
		} else if (isPause == true) {
			MusicService.mediaPlayer.seekTo(musicTempTime);
			MusicService.mediaPlayer.start();
			imgBtn_play.setImageResource(R.drawable.player_pause);
			isPlaying = true;
			isPause = false;
		}
	}

	// 純播放方法
	public void playMusic(Activity activity) {
		imgBtn_play.setImageResource(R.drawable.player_pause);
		Intent intent = new Intent();
		intent.putExtra("PATH", playList.get(globalVariable.getMusicCursor())
				.getPath());
		intent.putExtra("MusicState", GlobalVariable.PLAY);
		intent.setClass(activity, MusicService.class);
		activity.startService(intent);
		// 設置main Fragment 狀態與外觀
		setMainState(globalVariable.getMusicCursor());
		handler.post(updateThread);
		songTimeBar.setEnabled(true);
		isPlaying = true;
	}

	// 設置Main元件狀態
	@SuppressWarnings("deprecation")
	public void setMainState(int musicCursor) {
		songTitle.setText(playList.get(musicCursor).getTitle());
		songArtist.setText(playList.get(musicCursor).getArtist());
		songAlbum.setText(playList.get(musicCursor).getAlbum());
		songTime.setText(playList.get(musicCursor).getTime());
		background = new BitmapDrawable(playList.get(musicCursor)
				.getCoverData());
		vLayout.setBackground(background);
		vLayout.getBackground().setAlpha(100);
	}

	/* 外部使用方法 */
	// 得到目前音樂資訊
	public MusicInfo getPlayingNow() {
		MusicInfo musicInfo;
		musicInfo = playList.get(globalVariable.getMusicCursor());
		return musicInfo;
	}

	// 得到目前得到目前是否能加入清單
	public static Boolean canAdd() {
		if (spnPosition == showPlaylist) {
			return false;
		} else {
			return true;
		}
	}

	// 加入清單按鈕
	public static void buttonClicked(View view, int position) {
		Log.d("click", "MainActivity" + Integer.toString(position));
		if (spnPosition == sortWithName) {
			playList.add(musicInfos.get(position));
		} else {
			playList.add(selectList.get(position));
		}
		Toast.makeText(view.getContext(), "已加入播放清單", Toast.LENGTH_SHORT).show();
	}

	// 得到這個Activity
	public static MainActivity setActivity() {
		return main;
	}

} // Activity End
