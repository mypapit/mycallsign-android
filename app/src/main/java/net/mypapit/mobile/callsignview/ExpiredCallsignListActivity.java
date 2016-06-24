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
 * Last Modified 6/20/15 1:43 PM
 *  Info url :
 *  http://code.google.com/p/mycallsign-android/
 *  http://blog.mypapit.net
 *  http://kirostudio.com
 *  http://mypapit.net
 */

package net.mypapit.mobile.callsignview;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.mypapit.mobile.callsignview.db.ConstantsInstaller;
import net.mypapit.mobile.callsignview.db.MyCursorAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExpiredCallsignListActivity extends ActionBarActivity {

    private ConstantsInstaller placeData;
    private SQLiteDatabase db;
    private Cursor cursor;
    private ListView listView;
    private MyCursorAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expired_callsign_list);

        listView = (ListView) findViewById(R.id.expiredListView);

        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        int version = MainActivity.strDBVERSION;
        if (placeData == null || db == null || !db.isOpen()) {
            placeData = new ConstantsInstaller(this, "callsign.db", null, version, R.raw.callsign);
            db = placeData.getReadableDatabase();


        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -5);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String in5years = sdf.format(calendar.getTime());

        cursor =  db.rawQuery("SELECT * from aa WHERE expire < ?", new String[]{in5years});

        adapter = new MyCursorAdapter(this, cursor, placeData, adapter.FLAG_REGISTER_CONTENT_OBSERVER);

        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent passIntent = new Intent();
                passIntent.setClassName("net.mypapit.mobile.callsignview", "net.mypapit.mobile.callsignview.CallsignDetailActivity");

                Cursor cursor1 = (Cursor) listView.getItemAtPosition(position);
                //  db = placeData.getReadableDatabase();
                Cursor cursor2 = db.rawQuery(
                        "SELECT _id,callsign,handle,aa,expire FROM aa WHERE callsign LIKE ?",
                        new String[]{"%" + cursor1.getString(cursor1.getColumnIndex("callsign"))});
                cursor2.moveToFirst();


                Callsign cs = new Callsign(cursor2.getString(cursor2.getColumnIndex("callsign")), cursor2.getString(cursor2.getColumnIndex("handle")));
                cs.setAa(cursor2.getString(cursor2.getColumnIndex("aa")));
                cs.setExpire(cursor2.getString(cursor2.getColumnIndex("expire")));


                passIntent.putExtra("Callsign", cs);
                startActivityForResult(passIntent, -1);

            }
        });



    }


   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void onPause() {
        super.onPause();

        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);


    }

}
