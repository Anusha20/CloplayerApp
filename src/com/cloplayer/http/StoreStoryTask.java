package com.cloplayer.http;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.StringTokenizer;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.cloplayer.CloplayerService;
import com.cloplayer.MaryConnector;
import com.cloplayer.PlayItem;
import com.cloplayer.utils.ServerConstants;

public class StoreStoryTask extends AsyncTask<String, String, String> {

	String textToRead;

	public StoreStoryTask(String textToRead) {
		this.textToRead = textToRead;
	}

	@Override
	protected String doInBackground(String... data) {

		StringTokenizer st = new StringTokenizer(textToRead.replace("\"", ""), ".");

		CloplayerService.getInstace().sendIntMessageToUI(CloplayerService.MSG_UPDATE_PROGRESS_MAX, st.countTokens());

		SharedPreferences globalSettings = CloplayerService.getInstace().getSharedPreferences(ServerConstants.CLOPLAYER_GLOBAL_PREFS, 0);
		SharedPreferences.Editor editor = globalSettings.edit();
		editor.putInt("nowPlayingProgressMax", st.countTokens());
		editor.commit();

		int currentLine = 0;

		while (st.hasMoreTokens()) {
			String text = st.nextToken().trim();
			Log.e("Test", "Downloading voice for : " + text);
			byte[] byteArray = MaryConnector.getAudio(text);
			PlayItem playItem = new PlayItem(text, byteArray);

			synchronized (CloplayerService.getInstace().currentStory) {

				try {
					editor.putString("nowPlayingPlayItem." + currentLine, PlayItem.toString(playItem));
					editor.commit();
				} catch (IOException e) {
					e.printStackTrace();
				}

				CloplayerService.getInstace().currentStory.notifyAll();
			}

			currentLine++;

			CloplayerService.getInstace().sendIntMessageToUI(CloplayerService.MSG_UPDATE_DOWNLOAD_PROGRESS, currentLine);

			editor.putInt("nowPlayingDownloadProgress", currentLine);
			editor.commit();
		}

		return "";

	}

	@Override
	protected void onPostExecute(String result) {

	}

}
