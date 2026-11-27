package ua.hoptimer.app;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import org.json.*;

public class MainActivity extends Activity {
    EditText boil; EditText[] mins = new EditText[10], names = new EditText[10], grams = new EditText[10];
    TextView elapsed, remaining, status; Button start, pause, stop;
    boolean running=false;
    BroadcastReceiver receiver = new BroadcastReceiver() { public void onReceive(Context c, Intent i) {
        long sec=i.getLongExtra("elapsed",0); elapsed.setText(fmt(sec)); remaining.setText(fmt(Math.max(0, i.getLongExtra("total",0)-sec)));
        status.setText(i.getStringExtra("status"));
        running=i.getBooleanExtra("running",false); start.setEnabled(!running); pause.setEnabled(running);
    }};
    @Override public void onCreate(Bundle b){ super.onCreate(b); buildUi();
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},10);
        if(Build.VERSION.SDK_INT>=33) registerReceiver(receiver,new IntentFilter("ua.hoptimer.UPDATE"), Context.RECEIVER_NOT_EXPORTED); else registerReceiver(receiver,new IntentFilter("ua.hoptimer.UPDATE"));
    }
    TextView text(String s,int size){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(0xff202124); return t; }
    EditText field(String hint,String val,int width){ EditText e=new EditText(this); e.setSingleLine(true); e.setHint(hint); e.setText(val); e.setInputType( android.text.InputType.TYPE_CLASS_NUMBER | (hint.contains("г")?android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL:0)); e.setEms(width); return e; }
    LinearLayout vertical(){ LinearLayout l=new LinearLayout(this); l.setOrientation(1); l.setPadding(24,16,24,16); return l; }
    void buildUi(){
        ScrollView sc=new ScrollView(this); LinearLayout root=vertical(); sc.addView(root);
        TextView title=text("HOP TIMER",26); title.setTypeface(null,Typeface.BOLD); root.addView(title);
        root.addView(text("Загальний час кипіння, хв",16)); boil=field("60","60",8); root.addView(boil);
        root.addView(text("Поточний час від початку кипіння",16));
        elapsed=text("00:00:00",34); elapsed.setTypeface(null,Typeface.BOLD); root.addView(elapsed);
        remaining=text("Залишилося: 00:00:00",18); root.addView(remaining);
        status=text("Готовий до запуску",14); root.addView(status);
        LinearLayout buttons=new LinearLayout(this); start=new Button(this); start.setText("СТАРТ"); pause=new Button(this); pause.setText("ПАУЗА / ПРОДОВЖИТИ"); stop=new Button(this); stop.setText("СТОП"); buttons.addView(start,new LinearLayout.LayoutParams(0,-2,1)); buttons.addView(pause,new LinearLayout.LayoutParams(0,-2,1)); buttons.addView(stop,new LinearLayout.LayoutParams(0,-2,1)); root.addView(buttons);
        root.addView(text("ВНЕСЕННЯ ХМЕЛЮ (до 10 позицій)",18));
        TextView hdr=text("Хв до кінця     Назва хмелю        Грами",12); root.addView(hdr);
        for(int n=0;n<10;n++){
            LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
            mins[n]=field("хв","",4); names[n]=new EditText(this); names[n].setSingleLine(true); names[n].setHint("Cascade"); names[n].setTextSize(14);
            grams[n]=field("г","",4);
            row.addView(mins[n],new LinearLayout.LayoutParams(0,-2,1)); row.addView(names[n],new LinearLayout.LayoutParams(0,-2,2)); row.addView(grams[n],new LinearLayout.LayoutParams(0,-2,1)); root.addView(row);
        }
        start.setOnClickListener(v->begin()); pause.setOnClickListener(v->command("TOGGLE")); stop.setOnClickListener(v->command("STOP"));
        setContentView(sc);
    }
    void begin(){
        try{
            int total=Integer.parseInt(boil.getText().toString().trim()); if(total<1||total>600) throw new Exception();
            JSONArray arr=new JSONArray();
            for(int n=0;n<10;n++) if(mins[n].length()>0){
                int m=Integer.parseInt(mins[n].getText().toString().trim()); if(m<0||m>total) throw new Exception();
                JSONObject o=new JSONObject(); o.put("minute",m); o.put("name",names[n].getText().toString().trim()); o.put("grams",grams[n].getText().toString().trim()); arr.put(o);
            }
            Intent i=new Intent(this,TimerService.class); i.setAction("START"); i.putExtra("total",total*60L); i.putExtra("events",arr.toString());
            if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
        }catch(Exception e){ Toast.makeText(this,"Перевір час кипіння та позиції хмелю",Toast.LENGTH_LONG).show(); }
    }
    void command(String a){ Intent i=new Intent(this,TimerService.class); i.setAction(a); startService(i); }
    static String fmt(long sec){ long h=sec/3600,m=(sec%3600)/60,s=sec%60; return String.format(java.util.Locale.US,"%02d:%02d:%02d",h,m,s); }
    @Override protected void onDestroy(){ super.onDestroy(); try{unregisterReceiver(receiver);}catch(Exception ignored){} }
}
