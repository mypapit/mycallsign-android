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
 * Last Modified 2/23/15 2:45 AM
 *  Info url :
 *  http://code.google.com/p/mycallsign-android/
 *  http://blog.mypapit.net
 *  http://kirostudio.com
 *  http://mypapit.net
 */

package net.mypapit.mobile.callsignview;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;

import net.mypapit.mobile.callsignview.db.ConstantsInstaller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mypapit on 2/9/15.
 */
public class CallsignDetailActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener, ShareActionProvider.OnShareTargetSelectedListener {
    private int FAVORITE;
    private Callsign csinfo;
    private TextView tvHandle, tvCallsign;
    private AnimationSet animSet;
    private Animation anim;
    private ConstantsInstaller placeData;
    private SQLiteDatabase db;
    private CheckBox btnStar;
    private Cursor cursor;
    private boolean restartActivity;
    private ShareActionProvider mShareActionProvider;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        animSet = (AnimationSet) AnimationUtils.loadAnimation(this, R.anim.view_bounce_scale);

/*
        btnQrz = (ImageView) findViewById(R.id.btnQRZ);
        btnQrz.setOnClickListener(this);

*/

        if (placeData == null || db == null || !db.isOpen()) {
            placeData = new ConstantsInstaller(this, "callsign.db", null, MainActivity.strDBVERSION, R.raw.callsign);
            db = placeData.getReadableDatabase();


        }


        csinfo = new Callsign("", "");
        csinfo = (Callsign) getIntent().getSerializableExtra("Callsign");
        restartActivity = getIntent().getBooleanExtra("restartActivity", false);

        tvCallsign = (TextView) findViewById(R.id.tvdCallsign);
        tvHandle = (TextView) findViewById(R.id.tvdHandle);
        TextView tvAA = (TextView) findViewById(R.id.tvdAA);
        TextView tvExpire = (TextView) findViewById(R.id.tvdExpiry);
        btnStar = (CheckBox) findViewById(R.id.btn_star);


        tvCallsign.setText(csinfo.getCallsign());
        tvHandle.setText(csinfo.getHandle());
        tvAA.setText(csinfo.getAa());
        tvExpire.setText(csinfo.getExpire());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String expire = tvExpire.getText().toString();
            Date date = sdf.parse(expire);

            if (new Date().after(date)) {
                tvExpire.setText(expire + " (EXPIRED)");
            }


        } catch (ParseException exception) {


        }


        //  mSpring.setEndValue(0f);

        //set title to Callsign
        actionBar.setTitle(tvCallsign.getText());
        int isTablet = getResources().getInteger(R.integer.isTablet);
        if (isTablet > 0) {
            // do nothing if it is a tablet

        } else {
            if (csinfo.getHandle().length() > 26) {
                float size = tvHandle.getTextSize();
                size = size * 0.4f;
                tvHandle.setTextSize(size);

            }

        }

        cursor = db.rawQuery("SELECT _id,callsign,favorite FROM aa WHERE callsign LIKE ?", new String[]{tvCallsign.getText().toString()});
        FAVORITE = cursor.getColumnIndex("favorite");

        btnStar.setOnCheckedChangeListener(null);
        cursor.moveToFirst();

        if (cursor.getInt(FAVORITE) > 0) {
            btnStar.setChecked(true);
            // Log.d("net.mypapit.mobile.TVCALLSIGNROW", "CHECKBOX-setchecked-early: " + btnStar.toString());
        } else {
            btnStar.setChecked(false);
        }

        btnStar.setOnCheckedChangeListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);

        MenuItem item = menu.findItem(R.id.action_sharedetails);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setOnShareTargetSelectedListener(this);

        this.setShareIntent();

        return true;
    }

    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");

            intent.putExtra(Intent.EXTRA_SUBJECT, "Callsign: " + csinfo.getCallsign());
            intent.putExtra(Intent.EXTRA_TITLE, "Callsign: " + csinfo.getCallsign());
            intent.putExtra(Intent.EXTRA_TEXT, "Callsign: " + csinfo.getCallsign() + "\nHandle: " + csinfo.getHandle() + "\nAA: " + csinfo.getAa() + "\nExpiry: " + csinfo.getExpire() + "\n");


            mShareActionProvider.setShareIntent(intent);
        }

    }

    //this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                if (!restartActivity) {
                    finish();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                break;
        }
        return true;


    }


    protected void onPause() {
        super.onPause();

        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);


    }

    protected void onStart() {
        super.onStart();
        tvHandle.startAnimation(animSet);


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int row = 0;
        ContentValues values = new ContentValues();
        if (isChecked) {
            values.put("favorite", 1);
            row = db.update("aa", values, "callsign = ?", new String[]{tvCallsign.getText().toString()});
            Snackbar.with(getApplicationContext()).text("Added to favorite list").show(this);

        } else {

            values.put("favorite", 0);
            row = db.update("aa", values, "callsign = ?", new String[]{tvCallsign.getText().toString()});
            Snackbar.with(getApplicationContext()).text("Removed from favorite list").show(this);


        }
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
      /*  intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_SUBJECT, "Callsign: " + csinfo.getCallsign());
        intent.putExtra(Intent.EXTRA_TITLE, "Callsign: " + csinfo.getCallsign());
        intent.putExtra(Intent.EXTRA_TEXT, "Callsign: " + csinfo.getCallsign() + "\nHandle: " + csinfo.getHandle() + "\nAA: " + csinfo.getAa() + "\nExpiry: " + csinfo.getExpire() + "\n");
        */

        return false;
    }
}
