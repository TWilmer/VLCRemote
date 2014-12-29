package de.twapps.vlcremote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;

import de.twapps.vlcremote.ChooseTrack.Item;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ControlActivity extends Activity {
	public  boolean isTracking;
	class UDPCommand extends AsyncTask<String, Void , ArrayList<String>> {
		long position;
		long length;
		int chapter;
		int track;
		String title;
		boolean dataOK;
		

		@Override
		protected ArrayList<String> doInBackground(String... params) {
			ArrayList<String> items=new ArrayList<String>();
			DatagramSocket ds;
			dataOK=false;
			try {
				ds = new DatagramSocket();

				DatagramPacket dp;
				InetAddress broadcast;
				broadcast = InetAddress.getByName("255.255.255.255");

				String cmd=params[0];
				
				dp = new DatagramPacket(cmd.getBytes(),
						cmd.length(), broadcast, Constants.port);

				 DatagramChannel channel = DatagramChannel.open();
				DatagramSocket s = channel.socket();

				s.setBroadcast(true);
				 InetSocketAddress sa = new InetSocketAddress(Constants.port);
				s.setReuseAddress(true);
				s.bind(sa);
				s.send(dp);
				if(cmd.contains("Get"))
				{
				s.setSoTimeout(100);
				while(true)
				{
					try{
						byte[] buffer=new byte[1024];
						dp=new DatagramPacket(buffer, buffer.length);
						
						s.receive(dp);
						StringBuilder r=new StringBuilder();
						buffer=dp.getData();
						for(int i=0;i<dp.getLength();i++)
						{
							byte v=buffer[i];
							if(v!=0)
							r.append((char)v);
						}
						String name=r.toString();
				
						if(name.startsWith(ownName+"$GetPosition"))
						{
							name=name.substring(ownName.length()+1);
							String[] vars=name.split("\\|");
							dataOK=true;
							for(String v: vars)
							{
								if(v.startsWith("Title="))
								{
								   v=v.replaceFirst("Title=", "");
								   title=v;
								}else{
								String[] pair=v.split("=");
								if(pair[0].equals("GetPosition"))
								{
									position=Long.parseLong(pair[1]);
								}
								if(pair[0].equals("Length"))
								{
									length=Long.parseLong(pair[1]);
								}
								if(pair[0].equals("Track"))
								{
									track=Integer.parseInt(pair[1]);
								}
								if(pair[0].equals("Chapter"))
								{
									chapter=Integer.parseInt(pair[1]);
								}
								}
							}
							return null;
						}
					}catch(SocketTimeoutException ex)
					{
						s.close();
						return items;
					}
				}
				}
				s.close();
				return null;
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return items;

			
		}
		@Override
		protected void onPostExecute(ArrayList<String> result) {
			if(dataOK && !isTracking)
			{
				seekbar.setMax((int) (length/1000000));
				seekbar.setIndeterminate(false);
				seekbar.setProgress((int) (position/1000000));
				ControlActivity.this.title.setText(title);
			}
		}
	}

	SeekBar seekbar;
	TextView screenName;
	TextView title;

	
	String ownName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		ownName=Util.getName(this);
		screenName=(TextView)findViewById(R.id.screenName);
		title=(TextView)findViewById(R.id.title);
		isTracking=false;
		
		seekbar=(SeekBar)findViewById(R.id.seekbar);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				isTracking=false;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				isTracking=true;
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser)
				{
					 StringBuilder cmd=new StringBuilder();
					  cmd.append(ownName);

					  cmd.append('@');
					  long p=progress;
					  cmd.append("SetPosition="+Long.toString(p*1000000));
					  String c=cmd.toString();
					 
					  new UDPCommand().execute(c);
				}
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.control, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	  static  final int ACT_SCEEN=0;
	  static  final  int ACT_AUDIO=1;
	  static  final int  ACT_SUBTITLE=2;
	  
	  
	public void chooseScreen(View v)
	{
		Intent i=new Intent(this,  ChosseScreen.class);
		startActivityForResult(i,  ACT_SCEEN);
	}
	public void  doAction(View v)
	{
		StringBuilder cmd=new StringBuilder();
		cmd.append(Util.getName(this));
		
			cmd.append('@');
			cmd.append(
			getResources().getResourceEntryName(v.getId()));
			String c=cmd.toString();
			Log.w("TEST",c);
		new UDPCommand().execute(c);
	}

	boolean isRunning;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		screenName.setText(Util.getName(this));
		ownName=Util.getName(this);
		isRunning=true;
		Thread t = new Thread() {

			  @Override
			  public void run() {
				  try {
					  while (!isInterrupted() && isRunning) {
						  Thread.sleep(1000);
						  runOnUiThread(new Runnable() {
							  @Override
							  public void run() {
								  StringBuilder cmd=new StringBuilder();
								  cmd.append(ownName);

								  cmd.append('@');
								  cmd.append("GetPosition");
								  String c=cmd.toString();
								  Log.w("TEST",c);
								  new UDPCommand().execute(c);
			          }
			        });
			      }
			    } catch (InterruptedException e) {
			    }
			  }
			};

			t.start();
	}
	@Override
	protected void onPause() {
		isRunning=false;
		super.onPause();
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == ACT_SCEEN) {
	    	screenName.setText(Util.getName(this));
	    }
	}
	public void chooseAudio(View v)
	{
		Intent i=new Intent(this,  ChooseTrack.class);
		i.putExtra("WHAT", "Audio");
		
		startActivityForResult(i,  ACT_AUDIO);
		
	}
	public void chooseSub(View v)
	{
		Intent i=new Intent(this,  ChooseTrack.class);
		i.putExtra("WHAT", "Subtitle");
		startActivityForResult(i,  ACT_SUBTITLE);
	}
}
