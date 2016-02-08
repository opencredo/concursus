package com.opencredo.concourse.domain.time;

import com.google.common.base.Charsets;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public final class TimeUUID {

    private static final long START_EPOCH = makeEpoch();
    private static final long CLOCK_SEQ_AND_NODE = makeClockSeqAndNode();
    private static final long MIN_CLOCK_SEQ_AND_NODE = -9187201950435737472L;
    private static final long MAX_CLOCK_SEQ_AND_NODE = 9187201950435737471L;
    private static final AtomicLong lastTimestamp = new AtomicLong(0L);

    private TimeUUID() {
    }

    private static long makeEpoch() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"));
        c.set(1, 1582);
        c.set(2, 9);
        c.set(5, 15);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        c.set(14, 0);
        return c.getTimeInMillis();
    }

    private static long makeNode() {
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            Iterator props = getAllLocalAddresses().iterator();

            while(props.hasNext()) {
                String hash = (String)props.next();
                update(e, hash);
            }

            Properties var7 = System.getProperties();
            update(e, var7.getProperty("java.vendor"));
            update(e, var7.getProperty("java.vendor.url"));
            update(e, var7.getProperty("java.version"));
            update(e, var7.getProperty("os.arch"));
            update(e, var7.getProperty("os.name"));
            update(e, var7.getProperty("os.version"));
            byte[] var8 = e.digest();
            long node = 0L;

            for(int i = 0; i < 6; ++i) {
                node |= (255L & (long)var8[i]) << i * 8;
            }

            return node | 1099511627776L;
        } catch (NoSuchAlgorithmException var6) {
            throw new RuntimeException(var6);
        }
    }

    private static void update(MessageDigest digest, String value) {
        if(value != null) {
            digest.update(value.getBytes(Charsets.UTF_8));
        }

    }

    private static long makeClockSeqAndNode() {
        long clock = (new Random(System.currentTimeMillis())).nextLong();
        long node = makeNode();
        long lsb = 0L;
        lsb |= (clock & 16383L) << 48;
        lsb |= -9223372036854775808L;
        lsb |= node;
        return lsb;
    }

    public static UUID timeBased() {
        return new UUID(makeMSB(getCurrentTimestamp()), CLOCK_SEQ_AND_NODE);
    }

    public static UUID startOf(long timestamp) {
        return new UUID(makeMSB(fromUnixTimestamp(timestamp)), MIN_CLOCK_SEQ_AND_NODE);
    }

    public static UUID endOf(long timestamp) {
        long uuidTstamp = fromUnixTimestamp(timestamp + 1L) - 1L;
        return new UUID(makeMSB(uuidTstamp), MAX_CLOCK_SEQ_AND_NODE);
    }

    public static long unixTimestamp(UUID uuid) {
        if(uuid.version() != 1) {
            throw new IllegalArgumentException(String.format("Can only retrieve the unix timestamp for version 1 uuid (provided version %d)", new Object[]{Integer.valueOf(uuid.version())}));
        } else {
            long timestamp = uuid.timestamp();
            return timestamp / 10000L + START_EPOCH;
        }
    }

    public static Instant getInstant(UUID uuid) {
        return Instant.ofEpochMilli(unixTimestamp(uuid));
    }

    private static long getCurrentTimestamp() {
        while(true) {
            long now = fromUnixTimestamp(System.currentTimeMillis());
            long last = lastTimestamp.get();
            if(now > last) {
                if(lastTimestamp.compareAndSet(last, now)) {
                    return now;
                }
            } else {
                long lastMillis = millisOf(last);
                if(millisOf(now) < millisOf(last)) {
                    return lastTimestamp.incrementAndGet();
                }

                long candidate = last + 1L;
                if(millisOf(candidate) == lastMillis && lastTimestamp.compareAndSet(last, candidate)) {
                    return candidate;
                }
            }
        }
    }

    static long fromUnixTimestamp(long tstamp) {
        return (tstamp - START_EPOCH) * 10000L;
    }

    private static long millisOf(long timestamp) {
        return timestamp / 10000L;
    }

    static long makeMSB(long timestamp) {
        long msb = 0L;
        msb |= (4294967295L & timestamp) << 32;
        msb |= (281470681743360L & timestamp) >>> 16;
        msb |= (1152640029630136320L & timestamp) >>> 48;
        msb |= 4096L;
        return msb;
    }

    private static Set<String> getAllLocalAddresses() {
        HashSet allIps = new HashSet();

        try {
            InetAddress en = InetAddress.getLocalHost();
            allIps.add(en.toString());
            allIps.add(en.getCanonicalHostName());
            InetAddress[] enumIpAddr = InetAddress.getAllByName(en.getCanonicalHostName());
            if(enumIpAddr != null) {
                for(int i = 0; i < enumIpAddr.length; ++i) {
                    allIps.add(enumIpAddr[i].toString());
                }
            }
        } catch (UnknownHostException var5) {
            ;
        }

        try {
            Enumeration var6 = NetworkInterface.getNetworkInterfaces();
            if(var6 != null) {
                while(var6.hasMoreElements()) {
                    Enumeration var7 = ((NetworkInterface)var6.nextElement()).getInetAddresses();

                    while(var7.hasMoreElements()) {
                        allIps.add(((InetAddress)var7.nextElement()).toString());
                    }
                }
            }
        } catch (SocketException var4) {
            ;
        }

        return allIps;
    }
}

