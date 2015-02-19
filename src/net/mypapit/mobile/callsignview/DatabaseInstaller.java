package net.mypapit.mobile.callsignview;

/*
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * MYCallsign 1.7.0 for Android <mypapit@gmail.com> (9w2wtf)
 * Copyright 2012-2015 Mohammad Hafiz bin Ismail. All rights reserved.
 *
 * Info url :
 * http://code.google.com/p/mycallsign-android/
 * http://kirostudio.com
 * http://m.ashamradio.com/
 * 
 * 
 * DatabaseInstaller.java
 * Class for importing and handling AA database 
 * Mobile Malaysian Callsign Search Application
 *
 *
 * Callsign data are taken from SKMM (MCMC) website http://www.skmm.gov.my/registers1/aa.asp?aa=AARadio
 * MYCallsign logo was created by piju (http://9m2pju.blogspot.com)
 * 
 * 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class DatabaseInstaller extends SQLiteOpenHelper implements
		Runnable {

	private CallsignViewActivity ctxt = null;
	private int resource;
	SQLiteDatabase database;

	public DatabaseInstaller(CallsignViewActivity context, String name,
			SQLiteDatabase.CursorFactory factory, int version, int resource) {
		super(context, name, factory, version);

		this.resource = resource;

		this.ctxt = context;

	}

	public void onCreate(SQLiteDatabase db) {

		// pd = ProgressDialog.show(ctxt, "Please wait...",
		// "Importing database",false,true);

		// Thread thread = new Thread(this);
		// thread.start();

		database = db;
		try {
			InputStream stream = ctxt.getResources().openRawResource(resource);
			InputStreamReader is = new InputStreamReader(stream);
			BufferedReader in = new BufferedReader(is);
			String str;
			// ctxt.showProgress();
			while ((str = in.readLine()) != null) {
				 //Log.d("SQL DatabaseInstaller",str);
				database.execSQL(str);

			}

			in.close();
			// ctxt.hideProgress();
			// pd.dismiss();

		} catch (IOException e) {
			Log.d(this.toString(), "error while installing callsign db: " + e,
					e);
			// pd.dismiss();
		} finally {

		}

	}

	public void run() {

	}

}
