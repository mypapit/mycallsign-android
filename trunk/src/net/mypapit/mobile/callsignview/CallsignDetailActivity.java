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
 * CallsignDetailActivity.java
 * For displaying details on Callsigns
 * Mobile Malaysian Callsign Search Application
 *
 *
 * Callsign data are taken from SKMM (MCMC) website http://www.skmm.gov.my/registers1/aa.asp?aa=AARadio
 * MYCallsign logo was created by piju (http://9m2pju.blogspot.com)
 * 
 * 
 */


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

public class CallsignDetailActivity extends Activity {

	private CallsignInfo csinfo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.details_page);
        csinfo = new CallsignInfo();
        
        csinfo = (CallsignInfo) getIntent().getSerializableExtra("CallsignInfo");
        
        TextView tvcallsign = (TextView) findViewById(R.id.csdetails);
        TextView tvhandle = (TextView) findViewById(R.id.handledetails);
        TextView tvaa = (TextView) findViewById(R.id.AA);
        TextView tvExpiry = (TextView) findViewById(R.id.Expiry);
        
        tvcallsign.setText(csinfo.callsign);
        tvhandle.setText(csinfo.handle);
        tvaa.setText(csinfo.aa);
        tvExpiry.setText(csinfo.expire);
        
        
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,R.drawable.ic_smallicon);
        
        
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_callsign_detail, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	
    	case R.id.itemShare: 
    		
    		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
    		sharingIntent.setType("text/plain");
    		sharingIntent.putExtra(android.content.Intent.EXTRA_TITLE, "Callsign info");
    		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "MyCallsign Data");
    		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Callsign: " + csinfo.callsign + "\nHandle: "+csinfo.handle+"\nAA: "+ csinfo.aa +"\nExpiry: " + csinfo.expire +"\n\n http://callsign.ashamradio.com/mycallsign.php?mycallsign="+csinfo.callsign);
    		//sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://callsign.ashamradio.com/mycallsign.php?mycallsign="+csinfo.callsign);
    		startActivity(Intent.createChooser(sharingIntent, "Share via"));
    		
    		
    		    	
    		return true;
    		
    		
    	
    	}
		return false;
    	
    }
}
