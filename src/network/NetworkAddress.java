package network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Best-effort detection of the machine's reachable LAN IPv4 address so the
 * host lobby can show the IP the other player should type in to join.
 */
public final class NetworkAddress {

    private NetworkAddress() {}

    /**
     * Returns a non-loopback IPv4 address bound to an active network
     * interface, or {@code null} if none is available. Prefers addresses on
     * the typical private-network ranges (192.168/16, 10/8, 172.16/12).
     */
    public static String getLanAddress() {
        String fallback = null;
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface ni : Collections.list(ifaces)) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) continue;
                    String host = addr.getHostAddress();
                    if (host == null || host.contains(":")) continue; // IPv6
                    if (isPrivate(host)) return host;
                    if (fallback == null) fallback = host;
                }
            }
        } catch (Exception ignored) {
            // Fall through to the fallback / null
        }
        return fallback;
    }

    private static boolean isPrivate(String ip) {
        return ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("172.16.") || ip.startsWith("172.17.")
                || ip.startsWith("172.18.") || ip.startsWith("172.19.")
                || ip.startsWith("172.20.") || ip.startsWith("172.21.")
                || ip.startsWith("172.22.") || ip.startsWith("172.23.")
                || ip.startsWith("172.24.") || ip.startsWith("172.25.")
                || ip.startsWith("172.26.") || ip.startsWith("172.27.")
                || ip.startsWith("172.28.") || ip.startsWith("172.29.")
                || ip.startsWith("172.30.") || ip.startsWith("172.31.");
    }
}
