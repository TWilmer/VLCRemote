package de.twapps.vlcremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Util {
	static void setName(Context c,String name)
	{
		SharedPreferences pf=		c.getSharedPreferences("de.twapps.vlcremote", c.MODE_PRIVATE);
		Editor e = pf.edit();
		e.putString("screen", name);
		e.apply();
		

	}
	static String getName(Context c)
	{
		SharedPreferences pf=		c.getSharedPreferences("de.twapps.vlcremote", c.MODE_PRIVATE);
		return pf.getString("screen", "PI");
	}
}
