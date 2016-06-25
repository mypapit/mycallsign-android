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
 * Last Modified 2/20/15 9:10 PM
 *  Info url :
 *  http://code.google.com/p/mycallsign-android/
 *  http://blog.mypapit.net
 *  http://kirostudio.com
 *  http://mypapit.net
 */

package net.mypapit.mobile.callsignview.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.cyrilmottier.android.listviewtipsandtricks.widget.DontPressWithParentCheckBox;
import com.makeramen.RoundedImageView;

import net.mypapit.mobile.callsignview.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by mypapit on 2/8/15.
 */
public class MyCursorAdapter extends CursorAdapter implements SectionIndexer, CompoundButton.OnCheckedChangeListener {
    private final int EXPIRE, CALLSIGN, HANDLE, FAVORITE;
    private LayoutInflater inflater;
    private AlphabetIndexer mAlphabetIndexer;
    private TextView tvCallsignRow, tvHandleRow;
    private RoundedImageView roundView;
    private final DateFormat sdf;
    private final Date now;
    private final int colorfilter, colorfilter5years;
    private DontPressWithParentCheckBox btnStar;
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
        holder.btnStar = (DontPressWithParentCheckBox) view.findViewById(R.id.btn_star);


        view.setTag(holder);
        return view;


    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        holder = (ViewHolder) view.getTag();
        holder.tvCallsignRow.setText(cursor.getString(CALLSIGN));
        holder.tvHandleRow.setText(cursor.getString(HANDLE));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -5);
        Date fiveYears = calendar.getTime();


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


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

//        Cursor tempcursor;


        ContentValues values = new ContentValues();
        int row;

        String cs = (String) holder.btnStar.getTag(buttonView.getId());


        SQLiteDatabase db = pdata.getWritableDatabase();
        db.beginTransaction();

        if (isChecked) {


            values.put("favorite", 1);
            row = db.update("aa", values, "callsign = ?", new String[]{cs});
            Log.d("TVCALLSIGNROW", "CHECKBOXpressed: " + buttonView.toString());
            Log.d("TVCALLSIGNROW", "rows affected,checked: " + row + "cs:  " + cs);


        } else {


            values.put("favorite", 0);
            row = db.update("aa", values, "callsign = ?", new String[]{cs});

            Log.d("TVCALLSIGNROW", "rows affected: " + row);
            Log.d("TVCALLSIGNROW", "rows affected,checked: " + row + "cs:  " + cs);


        }
        db.setTransactionSuccessful();


        db.endTransaction();


        getCursor().requery();


        notifyDataSetChanged();


    }

    protected void onContentChanged() {
        super.onContentChanged();


    }

    class ViewHolder {
        TextView tvCallsignRow, tvHandleRow;
        RoundedImageView roundView;
        DontPressWithParentCheckBox btnStar;


    }


}

