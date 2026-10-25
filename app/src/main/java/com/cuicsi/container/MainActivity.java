package com.cuicsi.container;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int BG = 0xFFF4F6F9, CARD = 0xFFFFFFFF, TEXT = 0xFF111827,
            SUB = 0xFF5B6577, DIM = 0xFF98A2B3, ACCENT = 0xFF0E9F6E,
            RED = 0xFFE11D48, LINE = 0xFFE3E7EE, BLUE = 0xFF2563EB;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private LinearLayout content;
    private TextView envAlpine, envUdocker, envDisk, envState;
    private TextView svcBadge, svcInfo;
    private Button btnStart, btnStop, btnOpen, btnLog;
    private TextView containersText, imagesText;
    private boolean running = false;
    private boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
        refresh();
    }

    // ---------------- UI ----------------

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        scroll.setFillViewport(true);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(12), dp(14), dp(24));
        scroll.addView(content, new ViewGroup.LayoutParams(-1, -2));
        setContentView(scroll);

        // 标题
        content.addView(titleRow());

        // 环境卡
        content.addView(envCard());

        // 服务标题
        content.addView(sectionTitle("我的服务"));

        // 服务卡（qBittorrent）
        content.addView(serviceCard());

        // 容器
        content.addView(sectionTitle("容器（udocker）"));
        content.addView(containersCard());

        // 镜像
        content.addView(sectionTitle("镜像"));
        content.addView(imagesCard());

        // 自定义命令
        content.addView(sectionTitle("高级：执行命令"));
        content.addView(commandCard());
    }

    private View titleRow() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(0, 0, 0, dp(10));

        TextView t = new TextView(this);
        t.setText("容器管家");
        t.setTextSize(20);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(TEXT);
        box.addView(t, new LinearLayout.LayoutParams(0, -2, 1));

        Button refresh = new Button(this);
        refresh.setText("刷新");
        refresh.setTextSize(12);
        refresh.setAllCaps(false);
        refresh.setTextColor(ACCENT);
        refresh.setBackgroundColor(Color.TRANSPARENT);
        refresh.setOnClickListener(v -> refresh());
        box.addView(refresh);
        return box;
    }

    private View envCard() {
        LinearLayout card = card();
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("运行环境");
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(TEXT);
        head.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        envState = new TextView(this);
        envState.setText("检测中…");
        envState.setTextSize(11);
        envState.setTextColor(DIM);
        head.addView(envState);
        card.addView(head);

        envAlpine = line(card, "Alpine", "—");
        envUdocker = line(card, "udocker", "—");
        envDisk = line(card, "磁盘占用", "—");

        TextView tip = new TextView(this);
        tip.setText("root 权限 · proot 容器 · 开机自启（Magisk 模块 udocker）");
        tip.setTextSize(10);
        tip.setTextColor(DIM);
        tip.setPadding(0, dp(6), 0, 0);
        card.addView(tip);
        return card;
    }

    private View serviceCard() {
        LinearLayout card = card();

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = new TextView(this);
        name.setText("qBittorrent 下载器");
        name.setTextSize(15);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextColor(TEXT);
        head.addView(name, new LinearLayout.LayoutParams(0, -2, 1));

        svcBadge = new TextView(this);
        svcBadge.setText("检测中");
        svcBadge.setTextSize(11);
        svcBadge.setPadding(dp(9), dp(3), dp(9), dp(3));
        svcBadge.setTextColor(DIM);
        svcBadge.setBackgroundResource(R.drawable.btn_gray);
        head.addView(svcBadge);
        card.addView(head);

        TextView desc = new TextView(this);
        desc.setText("BT / PT 下载器 · WebUI 管理 · 下载目录 /sdcard/下载/qb");
        desc.setTextSize(11);
        desc.setTextColor(SUB);
        desc.setPadding(0, dp(6), 0, dp(4));
        card.addView(desc);

        svcInfo = new TextView(this);
        svcInfo.setText("端口 8080 · http://192.168.2.100:8080");
        svcInfo.setTextSize(10);
        svcInfo.setTextColor(DIM);
        card.addView(svcInfo);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setPadding(0, dp(12), 0, 0);

        btnOpen = smallBtn("打开网页面板", ACCENT, Color.WHITE);
        btnOpen.setOnClickListener(v -> openWebUI());
        row1.addView(btnOpen, new LinearLayout.LayoutParams(0, -2, 1));

        btnLog = smallBtn("日志", 0xFFF0F3F7, TEXT);
        btnLog.setOnClickListener(v -> showLog());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 0.6f);
        lp.leftMargin = dp(8);
        row1.addView(btnLog, lp);
        card.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, dp(8), 0, 0);

        btnStart = smallBtn("启动服务", ACCENT, Color.WHITE);
        btnStart.setOnClickListener(v -> startService());
        row2.addView(btnStart, new LinearLayout.LayoutParams(0, -2, 1));

        btnStop = smallBtn("停止服务", 0xFFFDE8ED, RED);
        btnStop.setOnClickListener(v -> stopService());
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, -2, 1);
        lp2.leftMargin = dp(8);
        row2.addView(btnStop, lp2);
        card.addView(row2);

        return card;
    }

    private View containersCard() {
        LinearLayout card = card();
        containersText = mono("加载中…");
        card.addView(containersText);
        return card;
    }

    private View imagesCard() {
        LinearLayout card = card();
        imagesText = mono("加载中…");
        card.addView(imagesText);
        return card;
    }

    private View commandCard() {
        LinearLayout card = card();
        EditText ed = new EditText(this);
        ed.setHint("例如：ls /sdcard/下载/qb");
        ed.setTextSize(12);
        ed.setPadding(dp(10), dp(8), dp(10), dp(8));
        ed.setBackgroundResource(R.drawable.btn_gray);
        ed.setInputType(InputType.TYPE_CLASS_TEXT);
        card.addView(ed);

        TextView out = mono("");
        out.setTextColor(SUB);

        Button run = smallBtn("在 Alpine 里执行", ACCENT, Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(8);
        run.setOnClickListener(v -> {
            String cmd = ed.getText().toString().trim();
            if (cmd.isEmpty()) { toast("请输入命令"); return; }
            out.setText("执行中…");
            new Thread(() -> {
                String r = Runner.alpine(cmd, 60);
                ui.post(() -> out.setText(r.isEmpty() ? "（无输出）" : r));
            }).start();
        });
        card.addView(run, lp);
        card.addView(out);
        return card;
    }

    // ---------------- 数据刷新 ----------------

    private void refresh() {
        if (busy) return;
        busy = true;
        envState.setText("读取中…");

        new Thread(() -> {
            // 1) 服务进程状态（快）
            String ps = Runner.sh("pgrep -f qbittorrent-nox >/dev/null 2>&1 && echo RUNNING || echo STOPPED", 15);
            boolean run = ps.contains("RUNNING");

            // 2) 一次性进 Alpine 取全部信息
            String all = Runner.alpine(
                    "echo '@@ALPINE@@'; cat /etc/alpine-release; "
                  + "echo '@@UDOCKER@@'; udocker --allow-root --version 2>&1 | head -1; "
                  + "echo '@@PS@@'; udocker --allow-root ps 2>&1 | head -8; "
                  + "echo '@@IMG@@'; udocker --allow-root images 2>&1 | head -8", 60);

            // 3) 磁盘
            String disk = Runner.sh("du -sh /data/local/udk 2>/dev/null | cut -f1", 30);

            ui.post(() -> {
                busy = false;
                running = run;
                envAlpine.setText(pick(all, "@@ALPINE@@", "@@UDOCKER@@"));
                String ud = pick(all, "@@UDOCKER@@", "@@PS@@");
                envUdocker.setText(ud.isEmpty() ? "未安装" : ud);
                envDisk.setText(disk.isEmpty() ? "—" : disk);
                envState.setText(all.contains("执行失败") ? "root 服务不可用" : "正常");
                envState.setTextColor(all.contains("执行失败") ? RED : ACCENT);

                updateServiceUI();
                containersText.setText(orNone(pick(all, "@@PS@@", "@@IMG@@")));
                imagesText.setText(orNone(pick(all, "@@IMG@@", null)));
            });
        }).start();
    }

    private void updateServiceUI() {
        if (running) {
            svcBadge.setText("运行中");
            svcBadge.setTextColor(ACCENT);
            btnStart.setEnabled(false);
            btnStart.setAlpha(0.45f);
            btnStop.setEnabled(true);
            btnStop.setAlpha(1f);
        } else {
            svcBadge.setText("已停止");
            svcBadge.setTextColor(RED);
            btnStart.setEnabled(true);
            btnStart.setAlpha(1f);
            btnStop.setEnabled(false);
            btnStop.setAlpha(0.45f);
        }
    }

    // ---------------- 操作 ----------------

    private void startService() {
        toast("正在启动…");
        new Thread(() -> {
            Runner.sh("nohup setsid sh /data/local/udk/qb-start.sh >/data/adb/qb.log 2>&1 < /dev/null &", 20);
            try { Thread.sleep(9000); } catch (InterruptedException ignored) { }
            String ps = Runner.sh("pgrep -f qbittorrent-nox >/dev/null 2>&1 && echo RUNNING || echo STOPPED", 15);
            boolean ok = ps.contains("RUNNING");
            ui.post(() -> {
                running = ok;
                updateServiceUI();
                toast(ok ? "已启动" : "启动失败，查看日志");
            });
        }).start();
    }

    private void stopService() {
        toast("正在停止…");
        new Thread(() -> {
            Runner.sh("pkill -9 -f qbittorrent-nox", 20);
            try { Thread.sleep(1500); } catch (InterruptedException ignored) { }
            ui.post(() -> {
                running = false;
                updateServiceUI();
                toast("已停止");
            });
        }).start();
    }

    private void openWebUI() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://127.0.0.1:8080")));
        } catch (Exception e) {
            toast("没有可用浏览器");
        }
    }

    private void showLog() {
        new Thread(() -> {
            String log = Runner.sh("tail -40 /data/adb/qb.log 2>/dev/null", 20);
            ui.post(() -> new AlertDialog.Builder(this)
                    .setTitle("qBittorrent 日志")
                    .setMessage(log.isEmpty() ? "暂无日志" : log)
                    .setPositiveButton("关闭", null)
                    .show());
        }).start();
    }

    // ---------------- 小工具 ----------------

    private String pick(String all, String start, String end) {
        int i = all.indexOf(start);
        if (i < 0) return "";
        i += start.length();
        int j = end == null ? all.length() : all.indexOf(end, i);
        if (j < 0) j = all.length();
        return all.substring(i, j).trim();
    }

    private String orNone(String s) { return s == null || s.trim().isEmpty() ? "（暂无）" : s.trim(); }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackgroundResource(R.drawable.card_bg);
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        c.setLayoutParams(lp);
        return c;
    }

    private View sectionTitle(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(12);
        t.setTextColor(SUB);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(dp(4), dp(2), 0, dp(6));
        return t;
    }

    private TextView line(LinearLayout parent, String k, String v) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(5), 0, 0);

        TextView kk = new TextView(this);
        kk.setText(k);
        kk.setTextSize(11);
        kk.setTextColor(DIM);
        kk.setWidth(dp(78));
        row.addView(kk);

        TextView vv = new TextView(this);
        vv.setText(v);
        vv.setTextSize(12);
        vv.setTextColor(TEXT);
        row.addView(vv, new LinearLayout.LayoutParams(0, -2, 1));
        parent.addView(row);
        return vv;
    }

    private TextView mono(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(9.5f);
        t.setTypeface(Typeface.MONOSPACE);
        t.setTextColor(SUB);
        t.setTextIsSelectable(true);
        t.setHorizontallyScrolling(true);
        return t;
    }

    private Button smallBtn(String text, int bg, int fg) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setTextColor(fg);
        b.setBackgroundColor(bg);
        b.setPadding(0, dp(8), 0, dp(8));
        return b;
    }

    private int dp(int v) {
        return (int) (getResources().getDisplayMetrics().density * v);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
