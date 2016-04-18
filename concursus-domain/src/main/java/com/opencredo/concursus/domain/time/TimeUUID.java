package com.opencredo.concursus.domain.time;

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
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Utility class for generating TimeUUIDs,
 */
public final class TimeUUID {

    private static final long START_EPOCH = makeEpoch();

    private static final long CLOCK_SEQ_AND_NODE = makeClockSeqAndNode();
    private static final AtomicLong lastTimestamp = new AtomicLong(0L);

    private TimeUUID() {
    }

    private static long makeEpoch() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"));
        c.set(Calendar.YEAR, 1582);
        c.set(Calendar.MONTH, 9);
        c.set(Calendar.DAY_OF_MONTH, 15);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static long makeNode() {
        byte[] digestBytes = addSystemProperties(hashLocalAddresses()).digest();

        return LongStream.range(0, 6)
                .map(i -> digestBytes[(int) i] << (i * 8))
                .reduce(0x0000010000000000L, (acc, i) -> acc | i);
    }

    private static MessageDigest addSystemProperties(MessageDigest digest) {
        return addToDigest(digest,
                Stream.of("java.vendor", "java.vendor.url", "java.version", "os.arch", "os.name", "os.version")
                .map(System.getProperties()::getProperty));
    }

    private static MessageDigest hashLocalAddresses() {
        try {
            return addToDigest(MessageDigest.getInstance("MD5"),
                    getAllLocalAddresses().stream());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static MessageDigest addToDigest(MessageDigest digest, Stream<String> values) {
        values.filter(propertyValue -> propertyValue != null)
                .map(propertyValue -> propertyValue.getBytes(Charsets.UTF_8))
                .forEach(digest::update);
        return digest;
    }

    private static long makeClockSeqAndNode() {
        long clock = (new Random(System.currentTimeMillis())).nextLong();
        return makeNode() | ((clock & 0x3FFFL) << 48) | 0x8000000000000000L;
    }

    /**
     * Create a time-based (type 1) UUID
     * @return The created UUID.
     */
    public static UUID timeBased() {
        return new UUID(makeMSB(getCurrentTimestamp()), CLOCK_SEQ_AND_NODE);
    }

    /**
     * Get the unix timestamp encoded in a time-based (type 1) UUID.
     * @param uuid The UUID to retrieve the unix timestamp from.
     * @return The unix timestamp.
     */
    public static long unixTimestamp(UUID uuid) {
        if(uuid.version() != 1) {
            throw new IllegalArgumentException(String.format(
                    "Can only retrieve the unix timestamp for version 1 uuid (provided version %d)",
                    uuid.version()));
        } else {
            return uuid.timestamp() / 10000L + START_EPOCH;
        }
    }

    /**
     * Get the {@link Instant} encoded in a time-based (type 1) UUID.
     * @param uuid The UUID to retrieve the {@link Instant} from.
     * @return The {@link Instant}.
     */
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
                if (millisOf(candidate) == lastMillis && lastTimestamp.compareAndSet(last, candidate)) {
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
        return 0x1000L
                | ((0xFFFFFFFFL & timestamp) << 32)          // move bottom 32-bit word to top
                | ((0xFFFF00000000L & timestamp) >>> 16)     // move bytes 5 and 6 to bytes 3 and 4
                | ((0x0FFF000000000000L & timestamp) >>> 48); // move bytes 7 and 8 to bytes 1 and 2, dropping top nybble
    }

    private static Set<String> getAllLocalAddresses() {
        Set<String> allIps = getLocalHostAddresses();

        getNetworkInterfaces().ifPresent(networkInterfaces ->
            addNetworkInterfaces(allIps, networkInterfaces)
        );

        return allIps;
    }

    private static void addNetworkInterfaces(Set<String> allIps, Enumeration<NetworkInterface> networkInterfaces) {
        while(networkInterfaces.hasMoreElements()) {
            Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();

            while(inetAddresses.hasMoreElements()) {
                allIps.add((inetAddresses.nextElement()).toString());
            }
        }
    }

    private static Optional<Enumeration<NetworkInterface>> getNetworkInterfaces() {
        try {
            return Optional.ofNullable(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e) {
            return Optional.empty();
        }
    }

    private static Set<String> getLocalHostAddresses() {
        Set<String> allIps = new HashSet<>();

        try {
            InetAddress en = InetAddress.getLocalHost();

            allIps.add(en.toString());
            allIps.add(en.getCanonicalHostName());

            Stream.of(InetAddress.getAllByName(en.getCanonicalHostName()))
                    .map(InetAddress::toString)
                    .forEach(allIps::add);
        } catch (UnknownHostException e) {
        }

        return allIps;
    }
}

