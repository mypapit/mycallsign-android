package net.mypapit.mobile.callsignview;


/*
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * MYCallsign 1.0 for Android <mypapit@gmail.com> (9w2wtf)
 * Copyright 2012 Mohammad Hafiz bin Ismail. All rights reserved.
 *
 * Info url :
 * http://code.google.com/p/mycallsign-android/
 * http://kirostudio.com
 * http://m.ashamradio.com/
 * 
 * 
 * CallsignViewActivity.java
 * MyCallsign main Activity class
 * Mobile Malaysian Callsign Search Application
 *
 *
 * Callsign data are taken from SKMM (MCMC) website http://www.skmm.gov.my/registers1/aa.asp?aa=AARadio
 * MYCallsign logo was created by piju (http://9m2pju.blogspot.com)
 * 
 * 
 */


import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.woozzu.android.util.StringMatcher;
import com.woozzu.android.widget.IndexableListView;

public class CallsignViewActivity extends Activity implements TextWatcher,Runnable {
    /** Called when the activity is first created. */
	private Dialog progressDialog;	
	
	public ConstantsInstaller placeData;
	public SQLiteDatabase db;
	public static int strDBVERSION=5;
	
	
	public ClearableEditText searchText;
	//public EditText searchText;
	public Cursor cursor,defaultcursor;
	public ListAdapter adapter;
	//public ListView lvCallsign;
	public IndexableListView lvCallsign;
	private Spinner spinPrefix;
	
	CallsignViewActivity cva;
	String[] testString = new String[]{"fakap","those nonsense"};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.main);
        
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,R.drawable.ic_smallicon);
        
        //searchText = new EditText(this);
        
        Log.w("alive and well","i'm alive");
        
        
        //show progress dialog while application is populating the database for the first time.
        this.showProgress();
	     Thread thread = new Thread(this);
	     thread.start();
        
        
        
        
   


        
        lvCallsign = (IndexableListView) findViewById(R.id.lvCallsignView);
        searchText = (ClearableEditText) findViewById (R.id.etCallsignFilter);
       
        
        spinPrefix = (Spinner) findViewById(R.id.spinPrefix);
        this.fillSpinner();

        if (spinPrefix.getSelectedItem().toString().equals("Name")){ 
        	searchText.setHint("Enter Name");
        } else {
        	searchText.setHint("Enter Callsign");
        }

        
      //get save preferences
        SharedPreferences prefs=getPreferences(Context.MODE_PRIVATE);
        int val = prefs.getInt("callsign", 0);
        
        spinPrefix.setSelection(val);
        
        //save activity
        cva = this;
        spinPrefix.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
				// update: 1 August 2012 (mypapit@gmail.com) 
				// let the user search for Callsign or Handle-Operator Name
				
				// search by name
				if ( spinPrefix.getSelectedItem().toString().equals("Name") ) {
					String strHandleName = new String(searchText.getText().toString());
					searchText.setHint("Enter Name");
					if (strHandleName.length()>3) {

						cursor = db.rawQuery("SELECT _id,callsign,handle FROM aa WHERE handle LIKE ?" , new String[]{"%" + strHandleName +"%"});
						
						
					} else  {

						cursor=defaultcursor;

					}


				} else {
					// search by callsign-prefix
					//get callsign-prefix and postfix from searchText TextView
					String callsign = new String(spinPrefix.getSelectedItem().toString()+""+searchText.getText().toString());
					searchText.setHint("Enter Callsign");
					if (callsign.length()>3) {
						
						cursor = db.rawQuery("SELECT _id,callsign,handle FROM aa WHERE callsign LIKE ?" , new String[]{"%" + callsign +"%"});
						
					} else  {

						cursor=defaultcursor;

					}

				}


 			/*	
 				adapter = new MyCursorAdapter(cva, R.layout.callsign_layout, cursor, new String[] {"callsign","handle"}, new int[] {R.id.callsign,R.id.handle});
 
				lvCallsign.setAdapter(adapter);
				lvCallsign.setFastScrollEnabled(true);
				lvCallsign.setVerticalFadingEdgeEnabled(false);
				lvCallsign.setVerticalScrollBarEnabled(true);
				*/
				
				
				
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        	
		}); // end spinPrefix.setOnItemSelectedListener() anonymous class
        
        
        
        lvCallsign.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        		//Animation animation = new ScaleAnimation(1,1,1,0);
        		final View clickedView = view;
        		final int lvposition = position;
        		
        		Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.csfade);
        		
        		 //BounceInterpolator bi = new BounceInterpolator();
        	     //bi.getInterpolation(2.0f);
        	     //animation.setInterpolator(bi);
        		
        		animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						//clickedView.setVisibility(View.INVISIBLE);
						
						
						
						
						
						Intent passIntent = new Intent();
		        		passIntent.setClassName("net.mypapit.mobile.callsignview", "net.mypapit.mobile.callsignview.CallsignDetailActivity");
		        		
		        		Cursor cursor1 =(Cursor) lvCallsign.getItemAtPosition(lvposition);
		        		
		        		String strCallsign = cursor1.getString(cursor1.getColumnIndex("callsign"));
		        		
		        		
		        		cursor=db.rawQuery("SELECT _id,callsign,handle,aa,expire FROM aa WHERE callsign LIKE ? ORDER BY handle" , new String[]{"%" + strCallsign +"%"});
		        		
		        		cursor.moveToFirst();
		        		
		        		CallsignInfo csinfo = new CallsignInfo();
		        		csinfo.callsign = cursor.getString(cursor.getColumnIndex("callsign"));
		        		csinfo.handle = cursor.getString(cursor.getColumnIndex("handle"));
		        		csinfo.aa = cursor.getString(cursor.getColumnIndex("aa"));
		        		csinfo.expire = cursor.getString(cursor.getColumnIndex("expire"));
		        		
		        		passIntent.putExtra("CallsignInfo", csinfo);
		        		startActivityForResult(passIntent,-1);
						
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						
					}
        			
        		});
        		
        		animation.setDuration(180);
        		 view.startAnimation(animation);
        		 
        		
        		
        		//original intent
        		 
        		
        		
        	}
        }
        
        		
        );
        
        
        
        //lvCallsign.setFastScrollEnabled(true);
        searchText.addTextChangedListener(this);
       // this.hideProgress();
                               
        
    }
    
    

	private void fillSpinner() {
		// TODO Auto-generated method stub
		
		
		ArrayAdapter<Object> spinAdapter = new ArrayAdapter<Object>(this,android.R.layout.simple_spinner_item,new String[]{"9W2","9M2","9W6","9W8","9M6","9M8","9M4","Name"});
		spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinPrefix.setAdapter(spinAdapter);
		//android.R.layout.simple_spinner_dropdown_item
		
	}



	@Override
	public void afterTextChanged(Editable arg0) {
		if (arg0.toString().length() < 2) {
			   //cursor = db.query("aa", new String[]{"_id","callsign","handle"}, null, null, null, null, "handle");
			adapter = new MyCursorAdapter(this, R.layout.callsign_layout, defaultcursor, new String[] {"callsign","handle"}, new int[] {R.id.callsign,R.id.handle});   
			lvCallsign.setAdapter(adapter);
			
		}
		
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

		String callsign = new String(spinPrefix.getSelectedItem().toString()+""+ arg0.toString());

		// update: 1 August 2012 (mypapit@gmail.com) 
		// let the user search for Callsign or Handle-Operator Name

		// search by name
		if ( spinPrefix.getSelectedItem().toString().equals("Name") ) {
			String strHandleName = new String(searchText.getText().toString());
			Log.d("strHandleName","name : " + strHandleName );
			if (strHandleName.length()>3) {
				//cursor = db.query("aa", new String[]{"_id","callsign","handle"}, "callsign", new String[]{"%"+callsign+"%"}, null, null, "handle");
				cursor = db.rawQuery("SELECT _id,callsign,handle FROM aa WHERE handle LIKE ? ORDER BY handle" , new String[]{"%" + strHandleName +"%"});


			} else  {
				//   cursor = db.query("aa", new String[]{"_id","callsign","handle"}, null, null, null, null, "handle");
				cursor=defaultcursor;

			}


		} else {
			// search by callsign-prefix
			//get callsign-prefix and postfix from searchText TextView
			 callsign = new String(spinPrefix.getSelectedItem().toString()+""+arg0.toString());

			if (callsign.length()>3) {
				//cursor = db.query("aa", new String[]{"_id","callsign","handle"}, "callsign", new String[]{"%"+callsign+"%"}, null, null, "handle");
				Log.w("strHandleName","callsign : " + callsign );
				cursor = db.rawQuery("SELECT _id,callsign,handle FROM aa WHERE callsign LIKE ? ORDER BY handle" , new String[]{"%" + callsign +"%"});

			} else  {
				//   cursor = db.query("aa", new String[]{"_id","callsign","handle"}, null, null, null, null, "handle");
				cursor=defaultcursor;

			}

		}

		adapter = new MyCursorAdapter(this, R.layout.callsign_layout, cursor, new String[] {"callsign","handle"}, new int[] {R.id.callsign,R.id.handle});
		lvCallsign.setAdapter(adapter);




	}
	
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.list_menu, menu);
    	
    	

    	
    	    	
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()) {
    	case R.id.itemAbout:
    		// do something
    		try {
    		showDialog();
    		} catch (NameNotFoundException ex){
    			Toast toast = Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT);
    			toast.show();
    			
    		
    		}
    		return true;
    	case R.id.itemRate:
    		
    		Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.getPackageName()));
    		
    		try {
    			startActivity(urlIntent);
    			
    			
    		} catch (ActivityNotFoundException anfe){
    			Toast.makeText(this, "Couldn't launch Google Play store", Toast.LENGTH_LONG).show();
    		}
    		
    		return true;
    	
    	}
    	
    	return super.onOptionsItemSelected(item);
    	
    }
    
    private void showDialog() throws NameNotFoundException {
		// TODO Auto-generated method stub
    	final Dialog dialog = new Dialog(this);
    	dialog.setContentView(R.layout.about_dialog);
    	dialog.setTitle("About MYCallsign "+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
    	dialog.setCancelable(true);
    	
    	//text
    	TextView text = (TextView) dialog.findViewById(R.id.tvAbout);
    	text.setText(R.string.txtLicense);
    	
    	//icon image
    	ImageView img = (ImageView) dialog.findViewById(R.id.ivAbout);
    	img.setImageResource(R.drawable.ic_launcher);
    	
    	
    	
    	dialog.show();
    	
    	
    	
    	
		
	}
    
    public void showProgress() {
    	progressDialog = new Dialog(this);
    	progressDialog.setContentView(R.layout.progress_dialog);
    	progressDialog.setCancelable(false);
    	progressDialog.show();
    	
    }
    
    public synchronized  void hideProgress() {
    	progressDialog.dismiss();
    	adapter = new MyCursorAdapter(this, R.layout.callsign_layout, cursor, new String[] {"callsign","handle"}, new int[] {R.id.callsign,R.id.handle});
        lvCallsign.setAdapter(adapter);


    	
    }



	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (placeData == null||db==null|| !db.isOpen()){
			placeData = new ConstantsInstaller(this,"callsign.db",null,this.strDBVERSION,R.raw.callsign);
			db = placeData.getWritableDatabase();
		}
        
        
        
        //cursor = db.rawQuery("SELECT callsign,handle FROM aa", "");
        cursor = db.query("aa", new String[]{"_id","callsign","handle"}, null, null, null, null, "handle");
        defaultcursor = cursor;
        
        
        this.runOnUiThread(new Runnable() {
/*        cva.runOnUiThread(new Runnable() { */
        


        	public void run() {
        		 adapter = new MyCursorAdapter(getApplicationContext(), R.layout.callsign_layout, cursor, new String[] {"callsign","handle"}, new int[] {R.id.callsign,R.id.handle});
    			 
        			lvCallsign.setAdapter(adapter);
        			lvCallsign.setFastScrollEnabled(true);
        			lvCallsign.setVerticalFadingEdgeEnabled(false);
        			lvCallsign.setVerticalScrollBarEnabled(true);
 		
        		
        	}
        		
        		
        		
        });
       
        
        
        handler.sendEmptyMessage(0);
        
		
	}
	
	private Handler handler = new Handler(){ 
		public void handleMessage(Message msg) {
	        
	        
	        
	        
	        hideProgress();
			
		}
	};
	
	protected void onPause(){

		super.onPause();
		//save callsign prefix preference when the application goes background
		//
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putInt("callsign", spinPrefix.getSelectedItemPosition());
		editor.commit();
		/*if (db.isOpen()) {
			db.close();
		}*/

	}
	
	protected void onResume(){
		super.onResume();
	     
	/*     if (!db.isOpen()) {
	    	 placeData = new ConstantsInstaller(this,"callsign.db",null,this.strDBVERSION,R.raw.callsign);
				db = placeData.getWritableDatabase();
	    	 
	     }*/
	     
	}
	
	
	//
	// MyCursorAdapter: inner-class to ease up fast-search indexing 
	//
	class MyCursorAdapter extends SimpleCursorAdapter implements SectionIndexer {
		//AlphabetIndexer alphaIndexer;
		private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		public MyCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout,c,from,to);
			
			//alphaIndexer = new AlphabetIndexer(c,cursor.getColumnIndex("handle")," ABCDEFGHIJKLMNOPQRSTUVWXYZ");
			
			
			
		}
		
		public int getPositionForSection(int section) {
			//return alphaIndexer.getPositionForSection(section);
			// If there is no item for current section, previous section will be selected
			for (int i = section; i >= 0; i--) {
				for (int j = 0; j < getCount(); j++) {
					if (i == 0) {
						// For numeric section
						for (int k = 0; k <= 9; k++) {
							if (StringMatcher.match(String.valueOf( ((Cursor) getItem(j)).getString(2).charAt(0)), String.valueOf(k)))
								return j;
							
						}
					} else {
						if (StringMatcher.match(String.valueOf( ((Cursor) getItem(j)).getString(2).charAt(0)), String.valueOf(mSections.charAt(i))))
							return j;
					}
				}
			}
			return 0;

			
		
		}
		
		public int getSectionForPosition(int position){
			//return alphaIndexer.getSectionForPosition(position);
			return 0;
			
			
		}
		
		public Object[] getSections() {
			
			String[] sections = new String[mSections.length()];
			for (int i = 0; i < mSections.length(); i++)
				sections[i] = String.valueOf(mSections.charAt(i));
			return sections;
		}
			
	
	
	} // end MyCursorAdapter inner-class
	
	


}