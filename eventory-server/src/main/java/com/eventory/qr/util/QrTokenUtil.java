package com.eventory.qr.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * (토큰 생성/검증 + HMAC 서명)
 * */
public final class QrTokenUtil {
    private QrTokenUtil() {}

    /**
     * 데이터 포맷(짧게): r=<reservationId>&c=<code>&e=<epochSec>&s=<hexSig>
     */
    public static String buildToken(long reservationId, String code, long expEpochSec, String secret) {
        String payload = "r=" + reservationId + "&c=" + code + "&e=" + expEpochSec;
        String sigHex = hmacHex(payload, secret);
        return payload + "&s=" + sigHex;
    }

    public static boolean verify(String token, String secret) {
        Map<String, String> map = parse(token);
        if (!map.containsKey("r") || !map.containsKey("c") || !map.containsKey("e") || !map.containsKey("s")) return false;
        String payload = "r=" + map.get("r") + "&c=" + map.get("c") + "&e=" + map.get("e");
        String sig = hmacHex(payload, secret);
        long now = Instant.now().getEpochSecond();
        long exp = Long.parseLong(map.get("e"));
        return sig.equalsIgnoreCase(map.get("s")) && now <= exp;
    }

    public static Map<String, String> parse(String token) {
        Map<String, String> map = new HashMap<>();
        for (String p : token.split("&")) {
            int i = p.indexOf('=');
            if (i > 0) map.put(p.substring(0, i), p.substring(i + 1));
        }
        return map;
    }

    public static String hmacHex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 실패", e);
        }
    }
}