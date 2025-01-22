package tun.proxy.common.utils;

/*
    This file is part of NetGuard.

    NetGuard is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015-2017 by Marcel Bokhorst (M66B)
*/

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class AddressUtils {
    private static final String TAG = "TunProxy:Util";

    public static List<String> getDefaultDNS(Context context) {
        String dns1 = null;
        String dns2 = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network an = cm.getActiveNetwork();
            if (an != null) {
                LinkProperties lp = cm.getLinkProperties(an);
                if (lp != null) {
                    List<InetAddress> dns = lp.getDnsServers();
                    if (!dns.isEmpty()) dns1 = dns.get(0).getHostAddress();
                    if (dns.size() > 1) dns2 = dns.get(1).getHostAddress();
                }
            }
        } else {
            dns1 = getSystemProperty("net.dns1");
            dns2 = getSystemProperty("net.dns2");
        }

        List<String> listDns = new ArrayList<>();
        listDns.add(TextUtils.isEmpty(dns1) ? "8.8.8.8" : dns1);
        listDns.add(TextUtils.isEmpty(dns2) ? "8.8.4.4" : dns2);
        return listDns;
    }

    public static boolean isValidIPv4Address(String ipAddress) {
        String host = ipAddress;

        if (ipAddress.contains(":")) {
            String[] split = ipAddress.split(":");
            if (split.length != 2) {
                return false;
            }
            host = split[0];
            int port = Integer.parseInt(split[1]);
            if (port <= 0 || port > 65535) {
                return false;
            }
        }

        String[] part = host.split("\\.");

        if (part.length != 4)
            return false;

        for (String p : part) {
            try {
                int num = Integer.parseInt(p);

                if (num < 0 || num > 255)
                    return false;
            } catch (NumberFormatException w) {
                return false;
            }
        }

        return true;
    }

    private static String getSystemProperty(String propertyName) {
        try {
            @SuppressLint("PrivateApi")
            Class<?> cls = Class.forName("android.os.SystemProperties");
            Method method = cls.getMethod("get", String.class);
            return (String) method.invoke(cls, propertyName);
        } catch (Exception e) {
            Log.d(TAG, "Cannot read system property: " + propertyName, e);
            return null;
        }
    }
}
