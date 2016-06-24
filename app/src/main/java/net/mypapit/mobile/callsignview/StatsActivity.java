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
 * Last Modified 2/23/15 2:34 AM
 *  Info url :
 *  http://code.google.com/p/mycallsign-android/
 *  http://blog.mypapit.net
 *  http://kirostudio.com
 *  http://mypapit.net
 */

package net.mypapit.mobile.callsignview;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.mypapit.mobile.callsignview.db.ConstantsInstaller;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;


public class StatsActivity extends ActionBarActivity {
    private int totalentries, count9w, count9m, count9w2, count9w6, count9w8;
    private ConstantsInstaller placeData;
    private SQLiteDatabase db;
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        PieChartView chart;
        PieChartData data;

        button = (Button) findViewById(R.id.btnExpired);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btnExpired) {
                    Intent intent = new Intent(StatsActivity.this, ExpiredCallsignListActivity.class);
                    startActivity(intent);




                }



            }
        });


        SharedPreferences prefs = getSharedPreferences("stats", MODE_PRIVATE);

        int version = MainActivity.strDBVERSION;
        if (placeData == null || db == null || !db.isOpen()) {
            placeData = new ConstantsInstaller(this, "callsign.db", null, version, R.raw.callsign);
            db = placeData.getReadableDatabase();


        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int totalexpired = db.rawQuery("SELECT _id from aa WHERE expire < ?", new String[]{sdf.format(new Date())}).getCount();

        totalentries = prefs.getInt("totalentries", 270);
        count9m = prefs.getInt("count9m", 50);
        count9w = prefs.getInt("count9w", 225);
        count9w2 = prefs.getInt("count9w2", 75);
        count9w6 = prefs.getInt("count9w6", 75);
        count9w8 = prefs.getInt("count9w8", 75);

        DecimalFormat tf = new DecimalFormat("#,###");
        TextView tv = (TextView) findViewById(R.id.chartTitle);
        TextView tvExpired = (TextView) findViewById(R.id.expiredTitle);
        TextView tvExpiredfuture = (TextView) findViewById(R.id.expiredfutureTitle);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 90);

        String in90days = sdf.format(calendar.getTime());
        int totalexpiredin90days = db.rawQuery("SELECT _id from aa WHERE expire < ?", new String[]{in90days}).getCount();

        totalexpiredin90days = totalexpiredin90days - totalexpired;

        if (totalexpiredin90days < 0) {
            tvExpiredfuture.setText("");
        } else {
            tvExpiredfuture.setText(tf.format(totalexpiredin90days) + " callsigns will expire in 90 days");
        }


        //tv.setText("Total Entries: " + totalentries + ", Total 9M: "+ count9m+", Total 9W: " + count9w+", Total expired: "+totalexpired);

        tv.setText(tf.format(totalentries) + " listed callsigns!");
        tvExpired.setText(tf.format(totalexpired) + " callsigns expired until today");

        chart = (PieChartView) findViewById(R.id.chart);

        List<SliceValue> values = new ArrayList<SliceValue>();


        SliceValue slice9m = new SliceValue(count9m, getResources().getColor(R.color.purple_400));

        //  SliceValue slice9w = new SliceValue(count9w, ChartUtils.pickColor());
        SliceValue slice9w2 = new SliceValue(count9w2, getResources().getColor(R.color.green_300));
        SliceValue slice9w6 = new SliceValue(count9w6, getResources().getColor(R.color.orange_A400));
        SliceValue slice9w8 = new SliceValue(count9w8, getResources().getColor(R.color.deep_orange_600));

        SliceValue sliceothers = new SliceValue(totalentries - (count9m + count9w2 + count9w6 + count9w8), ChartUtils.pickColor());

        float percent9m = (float) count9m / (float) totalentries;
       // float percent9w = (float) count9w / (float) totalentries;
        float percent9w2 = (float) count9w2 / (float) totalentries;
        float percent9w6 = (float) count9w6 / (float) totalentries;
        float percent9w8 = (float) count9w8 / (float) totalentries;
        float percentothers = (float) (totalentries - (count9m + count9w2 + count9w6 + count9w8)) / (float) totalentries;

        DecimalFormat df = new DecimalFormat("#.##%");

        StringBuffer sb = new StringBuffer("9M ");
        sb.append("(").append(df.format(percent9m)).append(") - ").append(tf.format(count9m));
        slice9m.setLabel(sb.toString().toCharArray());
/*
        sb = new StringBuffer("9W ");
        sb.append("("+df.format(percent9w)+") - "+count9w );

        slice9w.setLabel(sb.toString().toCharArray());
*/

        sb = new StringBuffer("9W2 ");
        sb.append("(").append(df.format(percent9w2)).append(") - ").append(tf.format(count9w2));
        slice9w2.setLabel(sb.toString().toCharArray());
        slice9w2.setSliceSpacing(18);


        sb = new StringBuffer("9W6 ");
        sb.append("(").append(df.format(percent9w6)).append(") - ").append(tf.format(count9w6));
        slice9w6.setLabel(sb.toString().toCharArray());
        slice9w6.setSliceSpacing(18);

        sb = new StringBuffer("9W8 ");
        sb.append("(").append(df.format(percent9w8)).append(") - ").append(tf.format(count9w8));
        slice9w8.setLabel(sb.toString().toCharArray());


        sb = new StringBuffer("Others ");
        sb.append("(").append(df.format(percentothers)).append(") - ").append(totalentries - (count9m + count9w2 + count9w6 + count9w8));
        slice9w6.setSliceSpacing(20);

        sliceothers.setLabel(sb.toString().toCharArray());


        values.add(slice9m);
        values.add(slice9w2);
        values.add(slice9w6);
        values.add(slice9w8);
        values.add(sliceothers);

        data = new PieChartData(values);
        data.setHasLabels(true);
        data.setHasLabelsOutside(true);
        data.setHasLabelsOnlyForSelected(true);
        chart.setPieChartData(data);
        chart.setCircleFillRatio(0.6f);
        chart.setValueSelectionEnabled(true);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //  getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
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
