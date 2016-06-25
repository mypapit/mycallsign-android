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
 * Last Modified 6/22/16 6:03 PM
 *  Info url :
 *  https://github.com/mypapit/mycallsign-android
 *  http://code.google.com/p/mycallsign-android/
 *  https://blog.mypapit.net
 *  http://kirostudio.com
 *
 */

package net.mypapit.mobile.callsignview;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mypapit on 6/22/16.
 */
public class CallsignModel {

    @SerializedName("callsign")
    public String callsign;

    @SerializedName("name")
    public String name;

    @SerializedName("valid")
    public int valid;

    @SerializedName("lat")
    public double lat;

    @SerializedName("lng")
    public double lng;

    @SerializedName("locality")
    public String locality;

    @SerializedName("status")
    public String status;

    @SerializedName("distance")
    public double distance;

    @SerializedName("time")
    public String time;

    @SerializedName("deviceid")
    public String deviceid;

    @SerializedName("phoneno")
    public String phoneno;

    @SerializedName("client")
    public String client;


    public LatLng getLatLng() {


        return new LatLng(lat, lng);

    }


}
