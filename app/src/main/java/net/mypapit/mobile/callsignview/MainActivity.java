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
 * Last Modified 6/27/16 1:42 AM
 *  Info url :
 *  https://github.com/mypapit/mycallsign-android
 *  http://code.google.com/p/mycallsign-android/
 *  https://blog.mypapit.net
 *  http://kirostudio.com
 *
 */

package net.mypapit.mobile.callsignview;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
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

import com.makeramen.RoundedImageView;
import com.nispok.snackbar.Snackbar;

import net.mypapit.mobile.callsignview.db.ConstantsInstaller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//import com.afollestad.materialdialogs.MaterialDialogCompat;


public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    //please increment strDBVERSION when callsign.txt is updated
    public static final int strDBVERSION = 0x34;
    public static final String CLIENT_VERSION = "MYCallsign/2.1.3";
    public static final String URL_API = "http://api.repeater.my/v1/callsign/endp.php";
    public static final String QUERY_API = "http://api.repeater.my/v1/callsign/getcount.php";

    private ConstantsInstaller placeData;
    private SQLiteDatabase db;
    private Cursor cursor, defaultcursor;
    private boolean mSearchHandle = false;
    private SearchView searchView;
    private MyCursorAdapter adapter;
    private ListView lv;
    private MainActivity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        activity = this;


        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        mSearchHandle = prefs.getBoolean("mSearchHandle", false);


        lv = (ListView) this.findViewById(R.id.listView);
        new LoadDatabaseTask(this).execute("load database");


        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent passIntent = new Intent(getApplicationContext(),CallsignDetailActivity.class);
                Cursor cursor1 = (Cursor) lv.getItemAtPosition(position);
                Callsign cs = new Callsign(cursor1.getString(cursor1.getColumnIndex("callsign")), cursor1.getString(cursor1.getColumnIndex("handle")));
                cs.setAa(cursor1.getString(cursor1.getColumnIndex("aa")));
                cs.setExpire(cursor1.getString(cursor1.getColumnIndex("expire")));

                OkHttpClient client = new OkHttpClient();

                RequestBody formbody = new FormBody.Builder()
                        .add("apiver", "1")
                        .add("callsign", cs.getCallsign())
                        .add("device", Build.PRODUCT + " " + Build.MODEL)
                        .add("client", CLIENT_VERSION)
                        .build();
                Request request = new Request.Builder()
                        .url("http://api.repeater.my/v1/callsign/endp.php")
                        .post(formbody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("OKHttp", e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        System.out.println(response.body().string());

                    }
                });

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(activity, (View) view, "profile");

                passIntent.putExtra("Callsign", cs);

                startActivityForResult(passIntent, -1, options.toBundle());


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
        Intent intent;


        switch (item.getItemId()) {


            case R.id.action_filter:


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

                dialogBuilder.setTitle("Search by");
                dialogBuilder.setSingleChoiceItems(new String[]{"Callsign", "Handle"}, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).edit();

                        switch (which) {
                            case 0:
                                mSearchHandle = false;
                                searchView.setQueryHint("Search Callsign");
                                showToast("Search by Callsign");
                                editor.putBoolean("mSearchHandle", mSearchHandle);
                                editor.apply();


                                break;

                            case 1:
                                mSearchHandle = true;
                                searchView.setQueryHint("Search Handle/Name");
                                showToast("Search by Handle/Name ");
                                editor.putBoolean("mSearchHandle", mSearchHandle);
                                editor.apply();
                                break;
                        }
                    }
                });

                dialogBuilder.show();
                return true;


            case R.id.showMap:
                intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                break;

            case R.id.showFavorites:
                intent = new Intent(this,FavoriteActivity.class);
                startActivity(intent);
                break;

            case R.id.showStats:
                intent = new Intent(this,StatsActivity.class);
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

        adapter.swapCursor(cursor);
        //  adapter = new MyCursorAdapter(this, cursor, placeData, 0);
        //lv.setAdapter(adapter);


    }

    private Cursor searchCallsign(String callsign) {

        String query = "SELECT * FROM aa WHERE callsign LIKE ? ORDER BY handle;";

        Cursor cursor;

        if (callsign.startsWith("9")) {
            cursor = db.rawQuery(query, new String[]{callsign + "%"});
            // Log.d("MYPAPIT.NET dalam 9",callsign);

        } else {
            cursor = db.rawQuery(query, new String[]{"%" + callsign});
            //Log.d("MYPAPIT.NET luar 9",callsign);
        }


        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
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
        //overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);


    }

    protected void onRestart() {
        super.onRestart();
        // adapter.notifyDataSetChanged();


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


    public ProgressDialog progressDialog() {

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.progress_dialog));
        dialog.setTitle(getResources().getString(R.string.progress_title));
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();

        return dialog;


    }


    /**
     * Created by mypapit on 2/8/15.
     */
    private class MyCursorAdapter extends CursorAdapter implements SectionIndexer {
        private final int EXPIRE, CALLSIGN, HANDLE, FAVORITE;
        private final int colorfilter, colorfilter5years;
        private final AlphabetIndexer mAlphabetIndexer;
        private final DateFormat sdf;
        private final Date now;
        private final ConstantsInstaller pdata;
        private LayoutInflater inflater;
        private TextView tvCallsignRow, tvHandleRow;
        private RoundedImageView roundView;
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
            colorfilter5years = context.getResources().getColor(R.color.red_A400);


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

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.YEAR, -5);
                Date fiveYears = calendar.getTime();


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

            }


        }


        class ViewHolder {
            TextView tvCallsignRow, tvHandleRow;
            RoundedImageView roundView;


        }


    }

    class LoadDatabaseTask extends AsyncTask<String, String, String> {

        private final MainActivity activity;
        private ProgressDialog dialog;

        public LoadDatabaseTask(MainActivity activity) {
            this.activity = activity;

        }

        protected void onPreExecute() {
            super.onPreExecute();
            dialog = activity.progressDialog();


        }


        @Override
        protected String doInBackground(String... params) {


            if (placeData == null || db == null || !db.isOpen()) {
                placeData = new ConstantsInstaller(activity, "callsign.db", null,
                        MainActivity.strDBVERSION, R.raw.callsign);
                db = placeData.getReadableDatabase();


            }

            cursor = db.query("aa", new String[]{"_id", "callsign", "handle", "expire", "favorite", "aa"},
                    null, null, null, null, null);
            defaultcursor = cursor;

            return null;

        }

        protected void onPostExecute(String test) {
            adapter = new MyCursorAdapter(activity, cursor, placeData, MyCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            lv.setAdapter(adapter);
            lv.setFastScrollEnabled(true);
            lv.setVerticalFadingEdgeEnabled(false);
            lv.setVerticalScrollBarEnabled(true);
            dialog.dismiss();

        }
    }

}
