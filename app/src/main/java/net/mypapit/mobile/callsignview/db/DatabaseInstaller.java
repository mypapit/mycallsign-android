/*
 * This file is part of MYCallsign
 *
 * Copyright (c) 2015 Mohammad Hafiz bin Ismail <mypapit@gmail.com>
 * MYCallsign is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Last Modified 2/22/15 8:56 PM
 *  Info url :
 *  http://code.google.com/p/mycallsign-android/
 *  http://blog.mypapit.net
 *  http://kirostudio.com
 *  http://mypapit.net
 */

package net.mypapit.mobile.callsignview.db;

/**
 * Created by mypapit on 2/8/15.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//import android.app.ProgressDialog;

public abstract class DatabaseInstaller extends SQLiteOpenHelper {

    private Context ctxt = null;
    private int resource;
    private SQLiteDatabase database;
    private int mOpenedConnections = 0;

    public DatabaseInstaller(Context context, String name,
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

        this.database = db;
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

        SharedPreferences.Editor editor = ctxt.getSharedPreferences("stats", ctxt.MODE_PRIVATE).edit();


        int count9m = db.rawQuery("SELECT _id FROM aa WHERE callsign LIKE ? ", new String[]{"9M%"}).getCount();
        int count9w = db.rawQuery("SELECT _id FROM aa WHERE callsign LIKE ? ", new String[]{"9W%"}).getCount();
        int count9w2 = db.rawQuery("SELECT _id FROM aa WHERE callsign LIKE ? ", new String[]{"9W2%"}).getCount();
        int count9w6 = db.rawQuery("SELECT _id FROM aa WHERE callsign LIKE ? ", new String[]{"9W6%"}).getCount();
        int count9w8 = db.rawQuery("SELECT _id FROM aa WHERE callsign LIKE ? ", new String[]{"9W8%"}).getCount();
        int totalentries = db.rawQuery("SELECT _id FROM aa", null).getCount();

        editor.putInt("totalentries", totalentries);
        editor.putInt("count9m", count9m);
        editor.putInt("count9w", count9w);
        editor.putInt("count9w2", count9w2);
        editor.putInt("count9w6", count9w6);
        editor.putInt("count9w8", count9w8);

        Log.d("Total DB", "Total Entries: " + totalentries + ", Total 9M: " + count9m + ", Total 9W: " + count9w);


        editor.commit();


    }


    public synchronized SQLiteDatabase getReadableDatabase() {
        mOpenedConnections++;
        return super.getReadableDatabase();
    }

    public synchronized void close() {
        mOpenedConnections--;
        if (mOpenedConnections == 0) {
            super.close();
        }
    }


}
