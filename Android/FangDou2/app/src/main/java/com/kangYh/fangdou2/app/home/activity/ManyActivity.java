package com.kangYh.fangdou2.app.home.activity;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.kangYh.fangdou2.R;
import com.kangYh.fangdou2.widget.MyJzvdStd;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.utils.ColorUtils;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
import com.zlm.libs.widget.RotateLayout;
import com.zml.libs.widget.MusicSeekBar;

import java.io.InputStream;

/**
 * 多行歌词
 */
public class ManyActivity extends AppCompatActivity
{


    /**
     * 多行歌词视图
     */
    private ManyLyricsView mManyLyricsView;
    /**
     * 歌曲播放进度
     */
    private TextView mSongProgressTV;
    /**
     * 歌曲进度条
     */
    private MusicSeekBar mMusicSeekBar;

    /**
     * 歌曲长度
     */
    private TextView mSongDurationTV;
    /**
     * 播放按钮
     */
    private Button mPlayBtn;
    /**
     * 暂停按钮
     */
    private Button mPauseBtn;
    /**
     * 停止按钮
     */
    private Button mStopBtn;
    /**
     * 播放器
     */
    private MediaPlayer mMediaPlayer;

    /**
     * 更新进度
     */
    private final int UPDATE_PROGRESS = 0;

    /**
     * 额外歌词回调
     */
    private final int EXTRALRCALLBACK = 1;

    /**
     * 播放歌曲
     */
    private final int MUSIC_PLAY = 2;

    /**
     * 歌曲暂停
     */
    private final int MUSIC_PAUSE = 3;

    /**
     * 歌曲初始
     */
    private final int MUSIC_INIT = 4;
    /**
     * 歌曲快进
     */
    private final int MUSIC_SEEKTO = 5;
    /**
     * 歌曲停止
     */
    private final int MUSIC_STOP = 6;
    /**
     * 歌曲唤醒
     */
    private final int MUSIC_RESUME = 7;

    private final String TAG = ManyActivity.class.getName();
    private RotateLayout mRotateLayout;
    private MyJzvdStd myJzvdStd;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case UPDATE_PROGRESS:

                    mMusicSeekBar.setEnabled(true);
                    if (mMediaPlayer != null)
                    {
                        if (mMusicSeekBar.getMax() == 0)
                        {
                            mMusicSeekBar.setMax(mMediaPlayer.getDuration());
                            mSongDurationTV.setText(TimeUtils.parseMMSSString(mMediaPlayer.getDuration()));
                        }
                        //
                        mMusicSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                        mSongProgressTV.setText(TimeUtils.parseMMSSString(mMediaPlayer.getCurrentPosition()));
                    }


                    break;

                case EXTRALRCALLBACK:

                    int extraLrcType = mManyLyricsView.getExtraLrcType();


                    break;

                case MUSIC_PLAY:
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying() && mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY)
                    {
                        mManyLyricsView.play(mMediaPlayer.getCurrentPosition());
                    }
                    break;

                case MUSIC_PAUSE:

                    if (mMediaPlayer != null && mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC)
                    {
                        mManyLyricsView.pause();
                    }
                    break;

                case MUSIC_INIT:
                    mManyLyricsView.initLrcData();
                    //加载中
                    mManyLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
                    break;

                case MUSIC_SEEKTO:
                    if (mMediaPlayer != null && mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC)
                    {
                        mManyLyricsView.seekto(mMediaPlayer.getCurrentPosition());
                    }

                    break;

                case MUSIC_STOP:

                    mManyLyricsView.initLrcData();
                    //
                    mMusicSeekBar.setProgress(0);
                    mMusicSeekBar.setMax(0);
                    mMusicSeekBar.setEnabled(false);
                    //
                    mSongDurationTV.setText(TimeUtils.parseMMSSString(0));
                    mSongProgressTV.setText(TimeUtils.parseMMSSString(0));

                    break;

                case MUSIC_RESUME:

                    if (mMediaPlayer != null && mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC)
                    {
                        mManyLyricsView.resume();
                    }

                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_many);

        mRotateLayout = findViewById(R.id.rotateLayout);
        mRotateLayout.setContentView(R.layout.activity_many);
        mRotateLayout.setRotateLayoutListener(new RotateLayout.RotateLayoutListener()
        {
            @Override
            public void finishActivity()
            {
                if (mMediaPlayer != null)
                {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                if (myJzvdStd != null)
                {
                    MyJzvdStd.releaseAllVideos();

                }
                finish();
                overridePendingTransition(0, 0);
            }
        });
        mRotateLayout.addIgnoreView(findViewById(R.id.menu));
        mRotateLayout.addIgnoreView(findViewById(R.id.lrcseekbar));
        mRotateLayout.setDragType(0);
        //
        mManyLyricsView = findViewById(R.id.manyLyricsView);
        //默认颜色
        int[] paintColors = new int[]{
                ColorUtils.parserColor("#ffffff"),
                ColorUtils.parserColor("#ffffff")
        };
        mManyLyricsView.setPaintColor(paintColors, false);
        mManyLyricsView.setSize(80, 80, true);
        mManyLyricsView.setIndicatorFontSize(80);
        mManyLyricsView.setDefText("等待播放");
        //高亮颜色
        int[] paintHLColors = new int[]{
                ColorUtils.parserColor("#fada83"),
                ColorUtils.parserColor("#fada83")
        };
        mManyLyricsView.setPaintHLColor(paintHLColors, false);

        mManyLyricsView.setExtraLyricsListener(new AbstractLrcView.ExtraLyricsListener()
        {
            @Override
            public void extraLrcCallback()
            {
                mHandler.sendEmptyMessage(EXTRALRCALLBACK);
            }
        });

        //
        mManyLyricsView.setSearchLyricsListener(new AbstractLrcView.SearchLyricsListener()
        {
            @Override
            public void goToSearchLrc()
            {

            }
        });
        mManyLyricsView.setOnLrcClickListener(new ManyLyricsView.OnLrcClickListener()
        {
            @Override
            public void onLrcPlayClicked(int progress)
            {
                if (mMediaPlayer != null)
                {
                    if (progress <= mMediaPlayer.getDuration())
                    {
                        mMediaPlayer.seekTo(progress);
                    }
                }

            }
        });
        //
        mSongProgressTV = findViewById(R.id.songProgress);
        mMusicSeekBar = findViewById(R.id.lrcseekbar);
        mMusicSeekBar.setOnMusicListener(new MusicSeekBar.OnMusicListener()
        {
            @Override
            public String getTimeText()
            {
                return TimeUtils.parseMMSSString(mMusicSeekBar.getProgress());
            }

            @Override
            public String getLrcText()
            {
                return null;
            }

            @Override
            public void onProgressChanged(MusicSeekBar musicSeekBar)
            {

            }

            @Override
            public void onTrackingTouchFinish(MusicSeekBar musicSeekBar)
            {
                if (mMediaPlayer != null)
                {
                    mMediaPlayer.seekTo(mMusicSeekBar.getProgress());
                }
            }
        });


        mSongDurationTV = findViewById(R.id.songDuration);

        //
        mPlayBtn = findViewById(R.id.play);
        mPlayBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (mMediaPlayer == null)
                {

                    mHandler.sendEmptyMessage(MUSIC_INIT);

                    //
                    mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.aiqinghai);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                    {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer)
                        {
                            mMediaPlayer.release();
                            mMediaPlayer = null;

                            mHandler.sendEmptyMessage(MUSIC_STOP);
                        }
                    });

                    //快进事件
                    mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener()
                    {
                        @Override
                        public void onSeekComplete(MediaPlayer mediaPlayer)
                        {
                            mHandler.sendEmptyMessage(MUSIC_SEEKTO);
                        }
                    });


                    //异步加载歌词文件
                    loadLrcFile();

                    mMediaPlayer.start();
                    mHandler.sendEmptyMessage(MUSIC_PLAY);
                    mHandler.postDelayed(mPlayRunnable, 0);

                    return;
                }

                if (mMediaPlayer.isPlaying())
                {
                    return;
                }

                mMediaPlayer.start();
                mHandler.sendEmptyMessage(MUSIC_RESUME);
                mHandler.postDelayed(mPlayRunnable, 0);
            }
        });

        mPauseBtn = findViewById(R.id.pause);
        mPauseBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mMediaPlayer.pause();
                mHandler.sendEmptyMessage(MUSIC_PAUSE);
            }
        });

        mStopBtn = findViewById(R.id.stop);
        mStopBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mMediaPlayer != null)
                {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }

                mHandler.sendEmptyMessage(MUSIC_STOP);

            }
        });


        myJzvdStd = findViewById(R.id.jz_video);
        myJzvdStd.setUp("http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4"
                , "    测试视频");
        Glide.with(this).load("http://jzvd-pic.nathen.cn/jzvd-pic/1bb2ebbe-140d-4e2e-abd2-9e7e564f71ac.png").into(myJzvdStd.thumbImageView);
        myJzvdStd.startButton.performClick();
        myJzvdStd.startVideo();

    }

    /**
     * 加载歌词文件
     */
    @SuppressLint("StaticFieldLeak")
    private void loadLrcFile()
    {

        new AsyncTask<String, Integer, String>()
        {

            @Override
            protected String doInBackground(String... strings)
            {
                InputStream inputStream = getResources().openRawResource(R.raw.aiqinghai_lrc);
                try
                {
                    //延迟看一下加载效果
                    Thread.sleep(500);

                    LyricsReader lyricsReader = new LyricsReader();
                    byte[] data = new byte[inputStream.available()];
                    inputStream.read(data);
                    lyricsReader.loadLrc(data, null, "aiqinghai_lrc.lrc");
                    mManyLyricsView.setLyricsReader(lyricsReader);
                    //
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying() && mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY)
                    {
                        mManyLyricsView.play(mMediaPlayer.getCurrentPosition());
                    }

                    inputStream.close();
                } catch (Exception e)
                {

                    mManyLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_ERROR);
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
                inputStream = null;

                return null;
            }
        }.execute("");
    }

    private Runnable mPlayRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying())
            {
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);

                mHandler.postDelayed(mPlayRunnable, 1000);
            }
        }
    };

    @Override
    public void onBackPressed()
    {
        if (mMediaPlayer != null)
        {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mRotateLayout.closeView();
        mManyLyricsView.release();
        if (myJzvdStd != null)
        {
            MyJzvdStd.releaseAllVideos();
        }
    }
}
