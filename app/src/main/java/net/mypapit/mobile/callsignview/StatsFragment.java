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
 * Last Modified 6/25/16 8:24 PM
 *  Info url :
 *  https://github.com/mypapit/mycallsign-android
 *  http://code.google.com/p/mycallsign-android/
 *  https://blog.mypapit.net
 *  http://kirostudio.com
 *
 */

package net.mypapit.mobile.callsignview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private ConstantsInstaller placeData;
    private SQLiteDatabase db;
    private Button button;

    TextView tv ;
    TextView tvExpired;
    TextView tvExpiredfuture;
    ;
    PieChartView chart;
    PieChartData data;

    private OnFragmentInteractionListener mListener;

    public StatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatsFragment newInstance(String param1, String param2) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        int totalentries, count9w, count9m, count9w2, count9w6, count9w8;





        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btnExpired) {
                    Intent intent = new Intent(getActivity(), ExpiredCallsignListActivity.class);
                    startActivity(intent);


                }


            }
        });


        SharedPreferences prefs = getActivity().getSharedPreferences("stats", Activity.MODE_PRIVATE);

        int version = MainActivity.strDBVERSION;
        if (placeData == null || db == null || !db.isOpen()) {
            placeData = new ConstantsInstaller(getActivity(), "callsign.db", null, version, R.raw.callsign);
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

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 90);

        String in90days = sdf.format(calendar.getTime());
        int totalexpiredin90days = db.rawQuery("SELECT _id from aa WHERE expire < ?", new String[]{in90days}).getCount();

        totalexpiredin90days = totalexpiredin90days - totalexpired;

        if (totalexpiredin90days < 0) {
            tvExpiredfuture.setText("");
        } else {
            tvExpiredfuture.setText(tf.format(totalexpiredin90days) + " " + getString(R.string.callsign_will_expire_90days));
        }


        //tv.setText("Total Entries: " + totalentries + ", Total 9M: "+ count9m+", Total 9W: " + count9w+", Total expired: "+totalexpired);

        tv.setText(tf.format(totalentries) + " "+getString(R.string.listed_callsign));
        tvExpired.setText(tf.format(totalexpired) + " "+getString(R.string.expired_until_today));



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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        button = (Button) view.findViewById(R.id.btnExpired);
        tv = (TextView) view.findViewById(R.id.chartTitle);
        tvExpired = (TextView) view.findViewById(R.id.expiredTitle);
        tvExpiredfuture = (TextView) view.findViewById(R.id.expiredfutureTitle);
        chart = (PieChartView) view.findViewById(R.id.chart);





        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
