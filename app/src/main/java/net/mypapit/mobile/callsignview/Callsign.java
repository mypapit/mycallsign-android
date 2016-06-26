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

import java.io.Serializable;

/**
 * Created by mypapit on 2/8/15.
 */
public class Callsign implements Serializable {
    private String callsign;
    private String handle, aa, expire;
    private int count, favorite;

    public Callsign(String callsign, String handle, String aa, String expire, int count, int favorite) {
        this.callsign = callsign;
        this.handle = handle;
        this.aa = aa;
        this.expire = expire;
        this.count = count;
        this.favorite = favorite;
    }

    public Callsign(String callsign, String handle) {
        this.callsign = callsign;
        this.handle = handle;
    }

    public String getAa() {
        return aa;
    }

    public void setAa(String aa) {
        this.aa = aa;
    }

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
}
