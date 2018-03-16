/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ycengine.yclibrary.widget;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.ycengine.yclibrary.util.LogUtil;

import java.io.IOException;

/**
 * Created by Phillip Hsu on 9/5/2016.
 *
 * A MediaPlayer configured to play a ringtone in a loop.
 */
public final class RingtoneOnce {

    private final Context mContext;
    private final AudioManager mAudioManager;
    private final Uri mUri;
    private int mVolume = 0; // volume은 0~15 사이어야 함.
    private MediaPlayer mMediaPlayer;

    public int getmVolume() {
        return mVolume;
    }

    public void setmVolume(int mVolume) {
        this.mVolume = mVolume;
    }

    public RingtoneOnce(Context context, Uri uri) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mUri = uri;
    }

    public void play() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mContext, mUri);
            //if (mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
                if (mVolume != 0) {
                    if (mVolume > 15) mVolume = 15;
                    LogUtil.e("RingTone Volume :::: " + mVolume);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mVolume, AudioManager.FLAG_PLAY_SOUND);
                }
                // "Must call this method before prepare() or prepareAsync() in order
                // for the target stream type to become effective thereafter."
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mMediaPlayer.setLooping(false);
                // There is prepare() and prepareAsync().
                // "For files, it is OK to call prepare(), which blocks until
                // MediaPlayer is ready for playback."
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            //}
        } catch (SecurityException | IOException e) {
            destroyLocalPlayer();
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            destroyLocalPlayer();
        }
    }

    private void destroyLocalPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

}
