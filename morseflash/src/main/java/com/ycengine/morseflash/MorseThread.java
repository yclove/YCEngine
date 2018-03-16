package com.ycengine.morseflash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;

public class MorseThread implements Runnable {
	private String morseStr;
	private int morseDot = 100;
	private HashMap<String, String> morseCodeMap;
	private boolean morseRun;
	private Handler handler;
	private Context context;

	public MorseThread(String morseStr, Handler handler, Context context) {
		this.morseStr = morseStr;
		this.handler = handler;
		this.context = context;
		morseCodeMap = new HashMap<String, String>();
	}

	@Override
	public void run() {
		String chStr;
		initMorseDb();

		if (morseStr == null || morseStr.length() == 0) {
			Message msg = new Message();
			msg.what = 1;
			handler.sendMessage(msg);
		}

		for (int i = 0; i < morseStr.length(); i++) {
			if (morseRun == false) {
				break;
			}
			chStr = morseStr.charAt(i) + "";
			morseCharater(chStr);

			try {
				Thread.sleep(morseDot * 3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Message msg = new Message();
		msg.what = 0;
		handler.sendMessage(new Message());
	}

	public void setMorseRun() {
		this.morseRun = true;
	}

	public void setMorseStop() {
		this.morseRun = false;
	}

	private void morseCharater(String chStr) {
		String valueStr = morseCodeMap.get(chStr);

        SoundPool soundPool = null;
        int beepShort = 0;

        if (Constants.IS_SOUND) {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            beepShort = soundPool.load(context, R.raw.beepshort, 1);
        }

		if (valueStr != null) {
			for (int i = 0; i < valueStr.length(); i++) {
				int j = valueStr.charAt(i);
				if (j == '0') {

					try {
						MainActivity.turnOn();
                        if (Constants.IS_SOUND) {
                            soundPool.play(beepShort, 1, 1, 0, 0, (float) 1.5);
                        }
						Thread.sleep(morseDot);
                        MainActivity.turnOff();
						Thread.sleep(morseDot);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if (j == '1') {

					try {
                        MainActivity.turnOn();
                        if (Constants.IS_SOUND) {
                            soundPool.play(beepShort, 1, 1, 0, 0, (float) 0.5);
                        }
						Thread.sleep(morseDot * 3);
                        MainActivity.turnOff();
						Thread.sleep(morseDot);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (j == '2') {
					try {
						Thread.sleep(morseDot * 7);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void initMorseDb() {
		AssetManager assetManager = context.getResources().getAssets();

		OutputStream fos = null;
		InputStream fis = null;

		try {
			fis = assetManager.open("morse.db");

			File file = new File("/data/data/com.ycengine.morseflash/databases/");
			file.mkdir();
			fos = new FileOutputStream("/data/data/com.ycengine.morseflash/databases/morse.db");

			byte[] buffer = new byte[1024];
			int readCount = 0;
			while (true) {
				readCount = fis.read(buffer, 0, 1024);

				if (readCount == -1) {
					break;
				}

				if (readCount < 1024) {
					fos.write(buffer, 0, readCount);
					break;
				}

				fos.write(buffer, 0, readCount);
			}
			fos.flush();

			fos.close();
			fis.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase("/data/data/com.ycengine.morseflash/databases/morse.db", null,
				SQLiteDatabase.NO_LOCALIZED_COLLATORS);

		String sqlStr = "select * from morse";
		Cursor cursor = sqlDb.rawQuery(sqlStr, null);

		String ch = null;
		String code = null;
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			ch = cursor.getString(cursor.getColumnIndex("ch"));
			code = cursor.getString(cursor.getColumnIndex("code"));
			morseCodeMap.put(ch, code);
			cursor.moveToNext();
		}

		cursor.close();
		sqlDb.close();
	}
}
