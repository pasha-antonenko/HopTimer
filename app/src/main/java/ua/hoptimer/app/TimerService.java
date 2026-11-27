package ua.hoptimer.app;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import org.json.*;

public class TimerService extends Service {
    static final String CHANNEL="hop_timer_channel"; Handler handler=new Handler(Looper.getMainLooper()); android.speech.tts.TextToSpeech tts;
    long total=0, elapsedBefore=0, startedAt=0; boolean running=false; JSONArray events=new JSONArray(); boolean[] fired=new boolean[10]; PowerManager.WakeLock wake;
    Runnable tick=new Runnable(){ public void run(){ if(!running)return; long e=elapsed(); checkEvents(e); broadcast(e); updateNotification(e); if(e>=total){ running=false; releaseWake(); broadcast(e); stopForeground(false); } else handler.postDelayed(this,500); }};
    @Override public void onCreate(){super.onCreate(); createChannel();}
    void createChannel(){ tts=new android.speech.tts.TextToSpeech(this, status->{ if(status==android.speech.tts.TextToSpeech.SUCCESS) tts.setLanguage(new java.util.Locale("uk","UA")); }); NotificationManager nm=getSystemService(NotificationManager.class); nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Hop Timer",NotificationManager.IMPORTANCE_HIGH)); }
    Notification notification(String s){ return new Notification.Builder(this,CHANNEL).setContentTitle("Hop Timer").setContentText(s).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setOngoing(running).build(); }
    @Override public int onStartCommand(Intent i,int flags,int id){ if(i==null)return START_STICKY; String a=i.getAction();
        if("START".equals(a)){ total=i.getLongExtra("total",3600); try{events=new JSONArray(i.getStringExtra("events"));}catch(Exception e){events=new JSONArray();} fired=new boolean[10]; elapsedBefore=0; startedAt=SystemClock.elapsedRealtime(); running=true; acquireWake(); startForeground(1,notification("Кипіння розпочато")); handler.removeCallbacks(tick); handler.post(tick); }
        else if("TOGGLE".equals(a)){ if(running){elapsedBefore=elapsed();running=false;handler.removeCallbacks(tick);releaseWake();}else if(total>0&&elapsedBefore<total){startedAt=SystemClock.elapsedRealtime();running=true;acquireWake();startForeground(1,notification("Таймер продовжено"));handler.post(tick);} broadcast(elapsedBefore); }
        else if("STOP".equals(a)){running=false;handler.removeCallbacks(tick);releaseWake();stopForeground(true);stopSelf();}
        return START_STICKY;
    }
    long elapsed(){return elapsedBefore+(running?(SystemClock.elapsedRealtime()-startedAt)/1000:0);}
    void checkEvents(long e){for(int n=0;n<events.length()&&n<10;n++){JSONObject o=events.optJSONObject(n);if(o==null||fired[n])continue; long at=total-o.optLong("minute")*60L;if(e>=at){fired[n]=true;String name=o.optString("name","хміль"),g=o.optString("grams","");String msg="Додати "+g+" г "+name; alert(msg);}}}
    void alert(String msg){ try{ UriSound(); }catch(Exception ignored){} android.os.Vibrator vib=(android.os.Vibrator)getSystemService(VIBRATOR_SERVICE); long[] pattern=new long[]{0,500,250,500,250,800}; if(Build.VERSION.SDK_INT>=26) vib.vibrate(VibrationEffect.createWaveform(pattern,-1)); else vib.vibrate(pattern,-1); if(tts!=null) tts.speak(msg,android.speech.tts.TextToSpeech.QUEUE_ADD,null,"hop-"+System.currentTimeMillis()); Notification n=new Notification.Builder(this,CHANNEL).setContentTitle("ЧАС ДОДАВАТИ ХМІЛЬ!").setContentText(msg).setStyle(new Notification.BigTextStyle().bigText(msg)).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setAutoCancel(true).build(); ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify((int)(System.currentTimeMillis()%100000),n); }
    void UriSound(){ MediaPlayer mp=MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI); if(mp!=null){mp.setOnCompletionListener(MediaPlayer::release);mp.start();} }
    void broadcast(long e){Intent b=new Intent("ua.hoptimer.UPDATE");b.setPackage(getPackageName());b.putExtra("elapsed",e);b.putExtra("total",total);b.putExtra("running",running);b.putExtra("status",running?"Кипіння триває":(e>=total?"Кипіння завершено":"Пауза"));sendBroadcast(b);}
    void updateNotification(long e){((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,notification("Залишилося "+MainActivity.fmt(Math.max(0,total-e))));}
    void acquireWake(){if(wake==null){wake=((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"HopTimer:Timer");wake.setReferenceCounted(false);}if(!wake.isHeld())wake.acquire();}
    void releaseWake(){if(wake!=null&&wake.isHeld())wake.release();}
    @Override public void onDestroy(){handler.removeCallbacks(tick);releaseWake();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
