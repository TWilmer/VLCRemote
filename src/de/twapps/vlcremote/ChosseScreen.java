package de.twapps.vlcremote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class ChosseScreen extends Activity {

	class UDPSearch extends AsyncTask<Void, Void , ArrayList<String>> {

	

		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			ArrayList<String> items=new ArrayList<String>();
			DatagramSocket ds;
			try {
				ds = new DatagramSocket();

				DatagramPacket dp;
				InetAddress broadcast;
				broadcast = InetAddress.getByName("255.255.255.255");

				dp = new DatagramPacket(Constants.Helo.getBytes(),
						Constants.Helo.length(), broadcast, Constants.port);

				 DatagramChannel channel = DatagramChannel.open();
				DatagramSocket s = channel.socket();

				s.setBroadcast(true);
				 InetSocketAddress sa = new InetSocketAddress(Constants.port);
				s.setReuseAddress(true);
				s.bind(sa);
				s.send(dp);
				s.setSoTimeout(1000);
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
						Log.w("TEST",name);
						if(name.startsWith("ME="))
						{
							name=name.replaceAll("ME=", "");
							items.add(name);
						}
					}catch(SocketTimeoutException ex)
					{
						s.close();
						return items;
					}
				}
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
			
		adapter = new StableArrayAdapter(
					mConext, android.R.layout.simple_list_item_1, result);
			listview.setAdapter(adapter);
			
			
		}

	}
	 StableArrayAdapter adapter=null;
	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<Integer, String> mIdMap = new HashMap< Integer,String> ();

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put( i,objects.get(i));
			}
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row, null);
                RadioButton r = (RadioButton)v.findViewById(R.id.radiobutton);
            }
            
           
            RadioButton r = (RadioButton)v.findViewById(R.id.radiobutton);
            r.setChecked(position == selectedPosition);
            r.setTag(position);
            r.setText(mIdMap.get(position));
            r.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPosition = (Integer)view.getTag();
                    notifyDataSetInvalidated();
                }
            });
            return v;
			
		}
		int selectedPosition;

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

	private Context mConext;

	private Handler mHandler;
	 ListView listview ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mConext = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chosse_screen);
		listview = (ListView) findViewById(R.id.listview);
		new  UDPSearch().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chosse_screen, menu);
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
	
	public void doConnect(View v)
	{
		if(adapter!=null)
		{
   		   String screen=	adapter.getItem(adapter.selectedPosition);
	
   		   Util.setName(this, screen);
		}
		setResult(0);
		finish();
	}
	
}
