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
    private DateFormat sdf;
    private Date now;
    private int colorfilter;
    private DontPressWithParentCheckBox btnStar;
    //private SQLiteDatabase db;
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
        holder.btnStar = (DontPressWithParentCheckBox) view.findViewById(R.id.btn_star);


        try {
            Date date = sdf.parse(cursor.getString(EXPIRE));

            if (now.after(date)) {
                holder.roundView.setColorFilter(colorfilter, android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                holder.roundView.clearColorFilter();
            }

        } catch (ParseException exception) {

        }

        holder.btnStar.setOnCheckedChangeListener(null);

        if (cursor.getInt(FAVORITE) > 0) {
            holder.btnStar.setChecked(true);
            Log.d("net.mypapit.mobile.TVCALLSIGNROW", "CHECKBOX-setchecked-early: " + holder.btnStar.toString());
        } else {
            holder.btnStar.setChecked(false);
        }

        holder.btnStar.setOnCheckedChangeListener(this);
        // holder.btnStar.setTag(R.id.btn_star,cursor);

        view.setTag(holder);
        return view;


    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        holder = (ViewHolder) view.getTag();
        holder.tvCallsignRow.setText(cursor.getString(CALLSIGN));
        holder.tvHandleRow.setText(cursor.getString(HANDLE));
        holder.btnStar.setTag(holder.btnStar.getId(), holder.tvCallsignRow.getText().toString());

        // holder.btnStar.setTag(R.id.btn_star,cursor.getPosition());


/*

        tvCallsignRow = (TextView) view.findViewById(R.id.tvCallsignrow);
        tvHandleRow = (TextView) view.findViewById(R.id.tvHandlerow);
        roundView = (RoundedImageView) view.findViewById(R.id.roundView);
        btnStar = (CheckBox) view.findViewById(R.id.btn_star);

*/




/*



        try {
            Date date = sdf.parse(cursor.getString(EXPIRE));

            if (now.after(date)){
                holder.roundView.setColorFilter(colorfilter, android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                holder.roundView.clearColorFilter();
            }

        } catch (ParseException exception){

        }



*/


/*
        CompoundButton.OnCheckedChangeListener mStarCheckedChanceChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Cursor tempcursor;
                if (isChecked) {
                    tempcursor=db.rawQuery("UPDATE aa SET favorite=? WHERE callsign=?", new String[]{"1", tvCallsignRow.getText().toString()});


                } else {
                    tempcursor=db.rawQuery("UPDATE aa SET favorite=? WHERE callsign=?", new String[]{"0", tvCallsignRow.getText().toString()});



                }

                tempcursor.moveToFirst();
                tempcursor.close();











                // do nothing
            }
        };
*/


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

//        Cursor tempcursor;


        ContentValues values = new ContentValues();
        int row = 0;

        String cs = (String) holder.btnStar.getTag(buttonView.getId());


        SQLiteDatabase db = pdata.getWritableDatabase();
        db.beginTransaction();
        // String cs = current.getString(current.getColumnIndex("callsign"));
        if (isChecked) {


            //          tempcursor=db.rawQuery("UPDATE aa SET favorite=? WHERE callsign=?", new String[]{"1", tvCallsignRow.getText().toString()});

            values.put("favorite", 1);
            row = db.update("aa", values, "callsign = ?", new String[]{cs});
            Log.d("net.mypapit.mobile.TVCALLSIGNROW", "CHECKBOXpressed: " + buttonView.toString());
            Log.d("net.mypapit.mobile.TVCALLSIGNROW", "rows affected,checked: " + row + "cs:  " + cs);


        } else {

            //        tempcursor=db.rawQuery("UPDATE aa SET favorite=? WHERE callsign=?", new String[]{"0", tvCallsignRow.getText().toString()});
            values.put("favorite", 0);
            row = db.update("aa", values, "callsign = ?", new String[]{cs});
            //Log.d("net.mypapit.mobile.TVCALLSIGNROW", "CHECKBOXpressed/unchecked: " + buttonView.toString());
            Log.d("net.mypapit.mobile.TVCALLSIGNROW", "rows affected: " + row);
            Log.d("net.mypapit.mobile.TVCALLSIGNROW", "rows affected,checked: " + row + "cs:  " + cs);


        }
        db.setTransactionSuccessful();

        //   onContentChanged();
        // db.close();
        db.endTransaction();


        //  tempcursor.moveToFirst();
        //tempcursor.close();
        getCursor().requery();
/*
        Cursor newCursor=db.query("aa", new String[] { "_id", "callsign", "handle","expire","favorite" },
                null, null, null, null, null);
        newCursor.moveToFirst();
        mAlphabetIndexer = new AlphabetIndexer(newCursor,
                newCursor.getColumnIndex("handle"),
                " ABCDEFGHIJKLMNOPQRTSUVWXYZ");


        changeCursor(newCursor);*/

        notifyDataSetChanged();


    }

    protected void onContentChanged() {
        super.onContentChanged();


        // notifyDataSetChanged();

    }

    class ViewHolder {
        TextView tvCallsignRow, tvHandleRow;
        RoundedImageView roundView;
        DontPressWithParentCheckBox btnStar;


    }


}

