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
 * Last Modified 6/25/16 8:39 PM
 *  Info url :
 *  https://github.com/mypapit/mycallsign-android
 *  http://code.google.com/p/mycallsign-android/
 *  https://blog.mypapit.net
 *  http://kirostudio.com
 *
 */

package net.mypapit.mobile.callsignview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.listviewtipsandtricks.widget.DontPressWithParentCheckBox;
import com.google.gson.Gson;
import com.makeramen.RoundedImageView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopQueriedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TopQueriedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopQueriedFragment extends Fragment implements TopQueryRequestListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView listView;

    private OnFragmentInteractionListener mListener;

    public TopQueriedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TopQueriedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TopQueriedFragment newInstance(String param1, String param2)  {
        TopQueriedFragment fragment = new TopQueriedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);


        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }





    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_top_queried, container, false);

        listView = (ListView) view.findViewById(R.id.lisviewTopQuery);



        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        new TopQueryDownloaded(this).execute();

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

    @Override
    public void topQueryRequestCompleted(TopQueryModel[] topQueryModel) {

        if (topQueryModel == null) {
            return;
        }

        TopQueryAdapter adapter = new TopQueryAdapter(getActivity(),topQueryModel);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();;



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

class TopQueryAdapter extends BaseAdapter {

    private  Context context;
    private TopQueryModel data[];

    public TopQueryAdapter(Context ctx, TopQueryModel data[])
    {
        this.context=ctx;
        this.data = data;


    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder =null;
        if (convertView == null){
            LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_top_queried_list,parent,false);

            holder = new ViewHolder();
            holder.tvCallsignTop = (TextView) convertView.findViewById(R.id.tvCallsigntop);
            holder.tvCallsignNumQueries = (TextView) convertView.findViewById(R.id.tvCallsignnumquery);
            holder.roundedImageView = (RoundedImageView) convertView.findViewById(R.id.roundTextView);

            convertView.setTag(holder);



        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvCallsignTop.setText(data[position].callsign);
        int total= data[position].total;

        String plural = String.format("%1$d %2$s",total,context.getString(R.string.top_query_plural));
        String singular = String.format("%1$d %2$s",total,context.getString(R.string.top_query_singular));



        if (total > 1) {
            holder.tvCallsignNumQueries.setText(plural);
        } else {
            holder.tvCallsignNumQueries.setText(singular);
        }


        return convertView;
    }

    class ViewHolder {
        TextView tvCallsignTop, tvCallsignNumQueries;
        RoundedImageView roundedImageView;




    }

}

class TopQueryDownloaded extends AsyncTask<Void,Void,TopQueryModel[]>{

    TopQueryRequestListener listener;

    public TopQueryDownloaded(TopQueryRequestListener listener){
        this.listener = listener;
    }

    @Override
    protected TopQueryModel[] doInBackground(Void... params) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(MainActivity.QUERY_API)
                .build();

        try {
            Response response = client.newCall(request).execute();
            Gson gson = new Gson();

            TopQueryModel[] topQueryModels = gson.fromJson(response.body().string(), TopQueryModel[].class);

            if (topQueryModels != null) {
/*** For debug only
                for (TopQueryModel model : topQueryModels){

                    System.out.println("Callsign : " + model.callsign + " query: " + model.total);
                }
*/

                if (topQueryModels.length > 1)
                    return topQueryModels;
            }



        } catch (IOException io) {
            Log.d ("TopQuery fails", io.getMessage());
            return  null;
        }



        return null;
    }

    public void onPostExecute(TopQueryModel[] topQueryModels) {

        listener.topQueryRequestCompleted(topQueryModels);
    }


}

interface TopQueryRequestListener {
    void topQueryRequestCompleted(TopQueryModel[] topQueryModel);
}