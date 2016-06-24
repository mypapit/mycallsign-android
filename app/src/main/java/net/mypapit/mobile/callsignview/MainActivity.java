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
 * Last Modified 2/23/15 3:55 AM
 *  Info url :
 *  http://code.google.com/p/mycallsign-android/
 *  http://blog.mypapit.net
 *  http://kirostudio.com
 *  http://mypapit.net
 */

package net.mypapit.mobile.callsignview;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

//import com.afollestad.materialdialogs.MaterialDialogCompat;
import com.makeramen.RoundedImageView;
import com.nispok.snackbar.Snackbar;

import net.mypapit.mobile.callsignview.db.ConstantsInstaller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    //please increment strDBVERSION when callsign.txt is updated
    public static final int strDBVERSION = 0x30;
    private ConstantsInstaller placeData;
    private SQLiteDatabase db;
    private Cursor cursor, defaultcursor;
    private boolean mSearchHandle = false;
    private SearchView searchView;
    private MyCursorAdapter adapter;
    private ListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        mSearchHandle = prefs.getBoolean("mSearchHandle", false);


        lv = (ListView) this.findViewById(R.id.listView);
        new LoadDatabaseTask(this).execute("load database");





        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent passIntent = new Intent();
                passIntent.setClassName("net.mypapit.mobile.callsignview", "net.mypapit.mobile.callsignview.CallsignDetailActivity");

                Cursor cursor1 = (Cursor) lv.getItemAtPosition(position);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);


        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        if (!mSearchHandle) {
            searchView.setQueryHint("Search Callsign");
        } else {
            searchView.setQueryHint("Search Handle/Name");
        }
        searchView.setOnQueryTextListener(this);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;


        switch (item.getItemId()) {

            //noinspection SimplifiableIfStatement
            case R.id.action_filter:
                //MaterialDialog dialog;
               // MaterialDialogCompat.Builder dialogBuilder = new MaterialDialogCompat.Builder(this);


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

                dialogBuilder.setTitle("Search by");
                dialogBuilder.setSingleChoiceItems(new String[]{"Callsign", "Handle"}, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("prefs", getApplicationContext().MODE_PRIVATE).edit();

                        switch (which) {
                            case 0:
                                mSearchHandle = false;
                                searchView.setQueryHint("Search Callsign");
                                showToast("Search by Callsign");
                                editor.putBoolean("mSearchHandle", mSearchHandle);
                                editor.commit();


                                break;

                            case 1:
                                mSearchHandle = true;
                                searchView.setQueryHint("Search Handle/Name");
                                showToast("Search by Handle/Name ");
                                editor.putBoolean("mSearchHandle", mSearchHandle);
                                editor.commit();

                                break;


                        }


                    }
                });

                dialogBuilder.show();
                return true;


            case R.id.showMap:
                //Snackbar.with(this).text("Map is not available yet").show(this);
                intent = new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(intent);




                break;

            case R.id.showFavorites:
                intent = new Intent();
                intent.setClassName("net.mypapit.mobile.callsignview", "net.mypapit.mobile.callsignview.FavoriteActivity");
                startActivity(intent);
                break;

            case R.id.showStats:
                intent = new Intent();
                intent.setClassName("net.mypapit.mobile.callsignview", "net.mypapit.mobile.callsignview.StatsActivity");
                startActivity(intent);
                break;

            case R.id.showAbout:
                try {
                    showDialog();
                } catch (PackageManager.NameNotFoundException ex) {
                    Snackbar.with(this).text(ex.toString()).show(this);


                }


                break;


        }

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        showResult(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        showResult(query);
        return false;
    }

    private void showResult(String query) {

        Cursor cursor;


        if (mSearchHandle) {

            cursor = this.searchHandle(query);


        } else {
            cursor = this.searchCallsign(query);
        }
            /*
            adapter = new MyCursorAdapter(getApplicationContext(),
                    R.layout.layout, cursor, new String[] {
                    "callsign", "handle" }, new int[] {
                    R.id.tvCallsignrow, R.id.tvHandlerow}, MyCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                    */

        adapter = new MyCursorAdapter(this, cursor, placeData, 0);
        lv.setAdapter(adapter);


    }

    private Cursor searchCallsign(String callsign) {

        String query = "SELECT * FROM aa WHERE callsign LIKE ? ORDER BY handle;";
        //Cursor cursor = db.rawQuery(query, new String[]{"%" + callsign});
        // Log.d("MYPAPIT.NET",callsign);
        Cursor cursor;

        if (callsign.startsWith("9")) {
            cursor = db.rawQuery(query, new String[]{callsign + "%"});
            // Log.d("MYPAPIT.NET dalam 9",callsign);

        } else {
            cursor = db.rawQuery(query, new String[]{"%" + callsign});
            //Log.d("MYPAPIT.NET luar 9",callsign);
        }


        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;

    }

    private Cursor searchHandle(String handle) {
        String query = "SELECT * FROM aa WHERE handle LIKE ? ORDER BY handle;";
        //Cursor cursor = db.rawQuery(query, new String[]{"%" + callsign});
        //  Log.d("MYPAPIT.NET",handle);
        Cursor cursor;


        cursor = db.rawQuery(query, new String[]{"%" + handle + "%"});
        //Log.d("MYPAPIT.NET",handle);


        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;


    }

    private void showToast(String message) {
        Context context = getApplicationContext();
        LayoutInflater inflater = getLayoutInflater();

        View customToastroot = inflater.inflate(R.layout.custom_toast, null);
        TextView tvToast = (TextView) customToastroot.findViewById(R.id.tvToast);
        tvToast.setText(message);

        Toast customtoast = new Toast(context);

        customtoast.setView(customToastroot);
        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        customtoast.setDuration(Toast.LENGTH_SHORT);
        customtoast.show();


    }

    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);


    }

    protected void onRestart() {
        super.onRestart();
        adapter.notifyDataSetChanged();


    }

    private void showDialog() throws PackageManager.NameNotFoundException {

        final AppCompatDialog dialog = new AppCompatDialog(this);
        dialog.setContentView(R.layout.about_dialog);
        dialog.setTitle("MYCallsign " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        dialog.setCancelable(true);

        // display licence text and disclaimer
        TextView text = (TextView) dialog.findViewById(R.id.tvAbout);
        text.setText(R.string.txtLicense);

        // icon image
        ImageView img = (ImageView) dialog.findViewById(R.id.ivAbout);
        img.setImageResource(R.drawable.ic_launcher);

        dialog.show();

    }

    /**
     * Created by mypapit on 2/8/15.
     */
    private class MyCursorAdapter extends CursorAdapter implements SectionIndexer {
        private final int EXPIRE, CALLSIGN, HANDLE, FAVORITE;
        private LayoutInflater inflater;
        private AlphabetIndexer mAlphabetIndexer;
        private TextView tvCallsignRow, tvHandleRow;
        private RoundedImageView roundView;
        private DateFormat sdf;
        private Date now;
        private int colorfilter;

        private ConstantsInstaller pdata;

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


            view.setTag(holder);
            return view;


        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            holder = (ViewHolder) view.getTag();
            holder.tvCallsignRow.setText(cursor.getString(CALLSIGN));
            holder.tvHandleRow.setText(cursor.getString(HANDLE));

            try {
                Date date = sdf.parse(cursor.getString(EXPIRE));


                if (now.after(date)) {
                    holder.roundView.setColorFilter(colorfilter, android.graphics.PorterDuff.Mode.MULTIPLY);
                }

                else {
                    holder.roundView.clearColorFilter();
                }

            } catch (ParseException exception) {

            }


        }


        class ViewHolder {
            TextView tvCallsignRow, tvHandleRow;
            RoundedImageView roundView;


        }


    }

    class LoadDatabaseTask extends AsyncTask<String, String, String> {


        private MainActivity activity;

        public LoadDatabaseTask(MainActivity activity) {
            this.activity = activity;

        }

        protected void onPreExecute() {
            super.onPreExecute();


        }


        @Override
        protected String doInBackground(String... params) {


            if (placeData == null || db == null || !db.isOpen()) {
                placeData = new ConstantsInstaller(activity, "callsign.db", null,
                        MainActivity.strDBVERSION, R.raw.callsign);
                db = placeData.getReadableDatabase();


            }

            cursor = db.query("aa", new String[]{"_id", "callsign", "handle", "expire", "favorite"},
                    null, null, null, null, null);
            defaultcursor = cursor;

            return null;

        }

        protected void onPostExecute(String test) {
            adapter = new MyCursorAdapter(activity, cursor, placeData, adapter.FLAG_REGISTER_CONTENT_OBSERVER);

            lv.setAdapter(adapter);
            lv.setFastScrollEnabled(true);
            lv.setVerticalFadingEdgeEnabled(false);
            lv.setVerticalScrollBarEnabled(true);

        }
    }

}
