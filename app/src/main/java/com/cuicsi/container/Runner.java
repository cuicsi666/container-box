package com.cuicsi.container;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/** 通过本机 root 服务（rc-agent）执行命令，无需 Magisk 授权弹窗 */
public class Runner {

    private static final String AGENT = "http://127.0.0.1:8787/api/exec";
    private static final String TOKEN = "dc37f94344820df49639306cf9eae76c99e195d3a417ad36";

    public static volatile String lastError = "";

    public static String run(String cmd, int timeoutSec) {
        HttpURLConnection c = null;
        try {
            URL url = new URL(AGENT);
            c = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setConnectTimeout(4000);
            c.setReadTimeout(Math.max(8000, timeoutSec * 1000 + 5000));
            c.setRequestProperty("Content-Type", "application/json");
            c.setRequestProperty("X-RC-Token", TOKEN);

            String body = "{\"cmd\":" + jsonStr(cmd) + ",\"timeout_ms\":" + (timeoutSec * 1000) + "}";
            OutputStream os = c.getOutputStream();
            os.write(body.getBytes("UTF-8"));
            os.close();

            int code = c.getResponseCode();
            InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            br.close();

            String out = extract(sb.toString(), "output");
            lastError = "";
            return out.trim();
        } catch (Exception e) {
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            return "执行失败：" + lastError;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private static String jsonStr(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
            }
        }
        return sb.append("\"").toString();
    }

    private static String extract(String json, String key) {
        String k = "\"" + key + "\":\"";
        int i = json.indexOf(k);
        if (i < 0) return "";
        i += k.length();
        StringBuilder sb = new StringBuilder();
        while (i < json.length()) {
            char ch = json.charAt(i);
            if (ch == '\\' && i + 1 < json.length()) {
                char n = json.charAt(i + 1);
                if (n == 'n') sb.append('\n');
                else if (n == 't') sb.append('\t');
                else if (n == 'r') sb.append('\r');
                else sb.append(n);
                i += 2;
                continue;
            }
            if (ch == '"') break;
            sb.append(ch);
            i++;
        }
        return sb.toString();
    }

    /** 底层 shell 执行（走 agent，即 root） */
    public static String sh(String cmd, int timeoutSec) {
        return run(cmd, timeoutSec);
    }

    /** 在 Alpine 容器里执行 */
    public static String alpine(String cmd, int timeoutSec) {
        String esc = cmd.replace("'", "'\\''");
        return run("/data/local/udk/udk '" + esc + "'", timeoutSec);
    }
}
