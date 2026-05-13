package com.go2super.socket.util;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketUtil {

    public static String getRemoteIP(Socket socket) {

        InetSocketAddress identifier = (InetSocketAddress) socket.getRemoteSocketAddress();
        Inet4Address inetAddress = (Inet4Address) identifier.getAddress();

        return inetAddress.toString();

    }

    public static String getIpAddress(byte[] rawBytes) {

        int i = 4;
        StringBuilder ipAddress = new StringBuilder();
        for (byte raw : rawBytes) {
            ipAddress.append(raw & 0xFF);
            if (--i > 0) {
                ipAddress.append(".");
            }
        }
        return ipAddress.toString();
    }

}
