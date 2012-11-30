package com.gadgetplus.androiduserguide;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
 
import com.gadgetplus.androiduserguide.R;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.ads.*;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {

	private AdView adView;
	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	private ProgressDialog mProgressDialog;
	//String feedurl = "http://query.yahooapis.com/v1/public/yql?q=select%20title%2Clink%20from%20feed%20where%20url%3D%22https%3A%2F%2Fgdata.youtube.com%2Ffeeds%2Fapi%2Fplaylists%2F9CC79690BA88A69D%3Fv%3D2%22%20%20and%20link.rel%3D%22alternate%22";
	
	String feedurl = "http://query.yahooapis.com/v1/public/yql?q=select%20title%2Clink%20from%20feed%20where%20url%3D%22https%3A%2F%2Fgdata.youtube.com%2Ffeeds%2Fapi%2Fplaylists%2FPLMdlsEUunGVjV9DP6GmxzkVx8jDCM62-E%3Fv%3D2%22%20%20and%20link.rel%3D%22alternate%22";
	//String feedurl = "http://www.youtube.com/playlist?list=PLMdlsEUunGVjV9DP6GmxzkVx8jDCM62-E";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		 
		// load admob
		// Create the adView
		adView = new AdView(this, AdSize.BANNER, "a150b8c4d9a5c05");
		// Lookup your LinearLayout assuming it’s been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
		// Add the adView to it
		layout.addView(adView);
		// Initiate a generic request to load it with an ad
		adView.loadAd(new AdRequest());
 
		// initial database handler
		final DatabaseHandler myDb = new DatabaseHandler(this);
		myDb.getWritableDatabase();
		// check data exist ?
		checkItemExist();

		myDb.close();

	}

	private void checkItemExist() {
		// TODO Auto-generated method stub
		final DatabaseHandler myDb = new DatabaseHandler(this);
		// TODO Auto-generated method stub
		if (myDb.getTotalRow() <= 0) {
			Log.d("DB", "no episode data, should update from internet");
			// asking for update data from internet
			final AlertDialog.Builder dDialog = new AlertDialog.Builder(this);
			dDialog.setTitle("Update");
			dDialog.setMessage("Update data, please connect to Internet.");
			dDialog.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
					Log.d("DB", "update episode data from internet");
					// load episode data and items form internet
					startDownload();
					parseContent();
					loadContent();
				}
			});
			dDialog.show();
		} else {
			loadContent();
		}
		myDb.close();
	}

	private void loadContent() {
		// put data to list
		final DatabaseHandler myDb = new DatabaseHandler(this);

		final String[][] myData = myDb.SelectAllData();

		ListView listItem = (ListView) findViewById(R.id.listItem);

		// Getting adapter by passing xml data ArrayList
		LazyAdapter adapter = new LazyAdapter(this, myData);
		listItem.setAdapter(adapter);

		// OnClick Item
		listItem.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int position, long mylng) {
				String strLocation = myData[position][3];
				// String strLocation =
				// myData.getString(myData.getColumnIndex("location"));
				// Toast.makeText(getApplicationContext(),"Selected  " +
				// strLocation, Toast.LENGTH_SHORT).show();
				// intent to youtube page
				Intent fanPageIntent = new Intent(Intent.ACTION_VIEW);
				fanPageIntent.setType("text/url");
				fanPageIntent.setData(Uri.parse(strLocation));
				startActivity(fanPageIntent);
			}
		});

		myDb.close();

	}

	private void startDownload() {
		new DownloadFileAsync().execute(feedurl);
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... aurl) {
			int count;
			Log.d("DOWNLOAD", feedurl);
			try {

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();
				Log.d("DOWNLOAD", "Lenght of file: " + lenghtOfFile);

				InputStream input = new BufferedInputStream(url.openStream());

				// Get File Name from URL
				// String fileName = URLDownload.substring(
				// URLDownload.lastIndexOf('/')+1, URLDownload.length() );

				OutputStream output = new FileOutputStream(Environment
						.getExternalStorageDirectory().getPath()
						+ "/androiduserguide.xml");

				Log.d("FILE", Environment.getExternalStorageDirectory()
						.getPath() + "/androiduserguide.xml");

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();

			} catch (Exception e) {
				Log.d("DOWNLOAD", "Error download file");
			}

			return null;

		}

		protected void onProgressUpdate(String... progress) {
			// Log.d("DOWNLOAD", progress[0]);
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@SuppressWarnings("deprecation")
		protected void onPostExecute(String unused) {
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
			removeDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

		@SuppressWarnings("deprecation")
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Downloading...");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
		}
	}

	private void parseContent() {
		// TODO Auto-generated method stub
		final DatabaseHandler myDb = new DatabaseHandler(this);
		try {

			File fXmlFile = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/androiduserguide.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			Log.d("XML", doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("entry");

			Log.d("XML", String.valueOf(nList.getLength()));

			// Integer epidId = 0;

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				// Log.d("XML",nNode.getChildNodes().item(0).getTextContent());
				Log.d("XML", nNode.getChildNodes().item(1).getAttributes()
						.item(0).getNodeValue());
				// get title
				String strTitle = nNode.getChildNodes().item(0)
						.getTextContent();
				// get video link
				String strLink = nNode.getChildNodes().item(1).getAttributes()
						.item(0).getNodeValue();
				// get video id = videoid[1]
				String[] fragments = strLink.split("&");
				String[] vedioID = fragments[0].split("=");
				// Log.d("XML", "video id =" + vedioID[1]);

				// save to db
				myDb.InsertItem((temp + 1), strTitle, vedioID[1], strLink);

			}
			myDb.close();

		} catch (Exception e) {

			Log.d("XML", e.getMessage());
		}

	}

	public boolean checkNetworkStatus() {
		final ConnectivityManager connMgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (wifi.isAvailable()) {
			Log.d("Network", "Connect via Wifi");
			return true;
		} else if (mobile.isAvailable()) {
			Log.d("Network", "Connect via Mobile network");
			return true;
		} else {
			Log.d("Network", "No network connection");
			return false;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.menu_share:
			Log.d("MENU", "select menu share");
			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/*");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					getString(R.string.text_share_subject));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					getString(R.string.text_share_body)+getApplicationContext().getPackageName());
			//startActivity(Intent.createChooser(sharingIntent,getString(R.string.menu_share)));
			startActivity(sharingIntent);
			break;
		case R.id.menu_update:
			Log.d("MENU", "select menu update");
			// ask for update
			final DatabaseHandler myDb = new DatabaseHandler(this);
			final AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Update");
			adb.setMessage("Do you want to update data?");
			adb.setPositiveButton("Yes",
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {							
							// check network connection
							if (checkNetworkStatus()) {
								Log.d("Network", "Has network connection");
								// clean table initial new update
								DatabaseHandler myDb = new DatabaseHandler(
										MainActivity.this);
								myDb.deleteTableData();
								// intent update database
								Log.d("DB", "update data from internet");
								// load episode data and items form internet
								startDownload();
								parseContent();
								loadContent();			
							} else {
								final AlertDialog.Builder alertAdb = new AlertDialog.Builder(
										MainActivity.this);
								alertAdb.setTitle("Error");
								alertAdb.setMessage("No internet connection, please connect to internet before update.");
								alertAdb.setNegativeButton("OK",
										null);
								alertAdb.show();
								Log.d("Network", "No network connection");
							}
						}
					});
			adb.setNegativeButton("No", null);
			adb.show();
			myDb.close();
			break;
		}
		return false;
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
