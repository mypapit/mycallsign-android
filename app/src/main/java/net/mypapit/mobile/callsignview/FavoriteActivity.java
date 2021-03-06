/*
 * This file is part of MYCallsign
 *
 * Copyright (c) 2016 Mohammad Hafiz bin Ismail <mypapit@gmail.com>
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
 * Last Modified 6/26/16 2:54 AM
 *  Info url :
 *  https://github.com/mypapit/mycallsign-android
 *  http://code.google.com/p/mycallsign-android/
 *  https://blog.mypapit.net
 *  http://kirostudio.com
 *
 */

package net.mypapit.mobile.callsignview;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.makeramen.RoundedImageView;

import net.mypapit.mobile.callsignview.db.ConstantsInstaller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class FavoriteActivity extends ActionBarActivity {
    private ListView lv;
    private ConstantsInstaller placeData;
    private SQLiteDatabase db;
    private Cursor cursor, defaultcursor;
    private MyCursorAdapter adapter;
    private FavoriteActivity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        activity = this;


        lv = (ListView) this.findViewById(R.id.listViewFavorite);

        int version = MainActivity.strDBVERSION;

        if (placeData == null || db == null || !db.isOpen()) {
            placeData = new ConstantsInstaller(this, "callsign.db", null, version, R.raw.callsign);
            db = placeData.getReadableDatabase();


        }


        cursor = db.rawQuery("SELECT * from aa WHERE favorite > ?", new String[]{"0"});
        defaultcursor = cursor;


        adapter = new MyCursorAdapter(this, cursor, placeData, MyCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);


        lv.setAdapter(adapter);
        lv.setEmptyView(findViewById(R.id.empty));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent passIntent = new Intent(getApplicationContext(),CallsignDetailActivity.class);


                Cursor cursor1 = (Cursor) lv.getItemAtPosition(position);


                Callsign cs = new Callsign(cursor1.getString(cursor1.getColumnIndex("callsign")), cursor1.getString(cursor1.getColumnIndex("handle")));
                cs.setAa(cursor1.getString(cursor1.getColumnIndex("aa")));
                cs.setExpire(cursor1.getString(cursor1.getColumnIndex("expire")));

             //   cursor1.close();


                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(activity, (View) view, "profile");


                passIntent.putExtra("restartActivity", true);
                passIntent.putExtra("Callsign", cs);
                startActivityForResult(passIntent, -1,options.toBundle());

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onPause() {
        super.onPause();

        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);


    }

    private class MyCursorAdapter extends CursorAdapter implements SectionIndexer {
        private LayoutInflater inflater;


        private final AlphabetIndexer mAlphabetIndexer;
        private TextView tvCallsignRow, tvHandleRow;
        private RoundedImageView roundView;
        private final DateFormat sdf;
        private final Date now;
        private final int EXPIRE, CALLSIGN, HANDLE, FAVORITE;
        private final int colorfilter;

        //private SQLiteDatabase db;
        private final ConstantsInstaller pdata;

        private ViewHolder holder;
        //   Context context;

        public MyCursorAdapter(Context context, Cursor cursor, ConstantsInstaller placeData, int flags) {
            super(context, cursor, flags);
            //      this.context = context;
            //  this.db = db;
            pdata = placeData;

            sdf = new SimpleDateFormat("yyyy-MM-dd");
            now = new Date();
            EXPIRE = cursor.getColumnIndex("expire");
            CALLSIGN = cursor.getColumnIndex("callsign");
            HANDLE = cursor.getColumnIndex("handle");
            FAVORITE = cursor.getColumnIndex("favorite");
            colorfilter = context.getResources().getColor(R.color.orange_A400);


            //inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAlphabetIndexer = new AlphabetIndexer(cursor,
                    cursor.getColumnIndex("handle"),
                    " ABCDEFGHIJKLMNOPQRTSUVWXYZ");


        }


        @Override
        public Object[] getSections() {
            return mAlphabetIndexer.getSections();
        }

        @Override
        public int getPositionForSection(int section) {
            return mAlphabetIndexer.getPositionForSection(section);
        }

        @Override
        public int getSectionForPosition(int position) {
            return mAlphabetIndexer.getSectionForPosition(position);

        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.tvCallsignRow = (TextView) view.findViewById(R.id.tvCallsignrow);
            holder.tvHandleRow = (TextView) view.findViewById(R.id.tvHandlerow);
            holder.roundView = (RoundedImageView) view.findViewById(R.id.roundView);


            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.YEAR, -5);
            Date fiveYears = calendar.getTime();
            final int colorfilter5years = context.getResources().getColor(R.color.red_A400);


            try {
                Date date = sdf.parse(cursor.getString(EXPIRE));

                if (now.after(date)) {
                    holder.roundView.setColorFilter(colorfilter, android.graphics.PorterDuff.Mode.MULTIPLY);
                } else {
                    holder.roundView.clearColorFilter();
                }

                if (fiveYears.after(date)) {
                    holder.roundView.clearColorFilter();
                    holder.roundView.setColorFilter(colorfilter5years, android.graphics.PorterDuff.Mode.MULTIPLY);
                }


            } catch (ParseException exception) {
                holder.roundView.clearColorFilter();
            }


            view.setTag(holder);
            return view;


        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            holder = (ViewHolder) view.getTag();
            holder.tvCallsignRow.setText(cursor.getString(CALLSIGN));
            holder.tvHandleRow.setText(cursor.getString(HANDLE));


        }


        class ViewHolder {
            TextView tvCallsignRow, tvHandleRow;
            RoundedImageView roundView;
            //DontPressWithParentCheckBox btnStar;


        }


    }


}