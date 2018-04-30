package com.example.administrator.salonboss;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/11/21.
 */

public class OrderService extends Service {
    NotificationManager manager;//通知控制类
    int frontnote_ID=1;//必须大于0，否则前台服务不启动
    int notification_ID;


    String notitle;
    String nocontent;
    String nodate;
    String notime;
    String nolink;
    Context context=this;
    public void sendNow(){
        Log.d("sendNow", "sendNow");
        //1.取出cusid  2.mysql查询  3.通知
        //1.取出cusid
        SharedPreferences sharedPreferences=getSharedPreferences("mysalonbossid",MODE_PRIVATE);
        final int salnumber=sharedPreferences.getInt("salnumber",0);
        //2.mysql查询
        Runnable runnable=new Runnable() {
            @Override
            public void run() {

                try {
                    //注册驱动
                    Log.d("xRunnable123", "xRunnable123");
                    Class.forName("com.mysql.jdbc.Driver");
                    String url = "jdbc:mysql://47.96.173.116:3306/company";
                    java.sql.Connection conn = DriverManager.getConnection(url, "cusmysql", "j12321456");
                    Statement stmt = conn.createStatement();
                    //当天日期
                    Log.d("xRunnable", "xRunnable");
                    Date dNow = new Date( );
                    SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd");
                    String gmtclose = ft.format(dNow)+"000000";
                    String sql = "select * from treatment where salnumber="+salnumber+" and trestate='paid' and gmtclose>"+gmtclose;
                    Log.e("datestr",sql);
                    ResultSet rs = stmt.executeQuery(sql);

                    //储存此条通知ID ,判断是否已发此条通知
                    SharedPreferences sharedPreferencesnotification=getSharedPreferences("mynotification",MODE_PRIVATE);
                    SharedPreferences.Editor editornotification=sharedPreferencesnotification.edit();

                    manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    while (rs.next()) {
                        Log.v("yzy", "field1-->"+rs.getString(1)+"  field2-->"+rs.getString(2));
                        //3.通知
                        //判断通知id是否大于已储存的id，大于（表名是新的）则发送
                        if (sharedPreferencesnotification.getInt("noticeid",0)<rs.getInt(1)) {
                            notification_ID = rs.getInt(1);
                            Intent intent = new Intent(context, MainActivity.class);
                            //intent.putExtra("nolink", rs.getString(3));
                            PendingIntent pintent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                            Notification.Builder builder = new Notification.Builder(context);
                            builder.setSmallIcon(R.drawable.salonbosslogo);//设置图标
                            builder.setTicker("您有新的订单");//手机状态栏的提示；
                            builder.setWhen(System.currentTimeMillis());//设置时间
                            builder.setContentTitle(rs.getString(13)+"元订单");//设置标题
                            builder.setContentText(rs.getString(4)+":"+rs.getString(8));//设置通知内容
                            builder.setContentIntent(pintent);//点击后的意图
                            builder.setDefaults(Notification.DEFAULT_ALL);//设置震动
                            Notification notification = builder.build();//4.1以上
                            //builder.getNotification();
                            //每日设session=当前时间，如果过了当前时间，则不再发送   还没做
                            Log.v("not", "" + notification_ID);
                            manager.notify(notification_ID, notification);
                            //startForeground(notification_ID, notification);


                            editornotification.putInt("noticeid",rs.getInt(1));
                            editornotification.commit();
                        }
                    }
                    rs.close();
                    stmt.close();
                    conn.close();
                    Log.v("yzy", "success to connect!");

                }catch(ClassNotFoundException e)
                {
                    Log.v("yzy", "fail to connect!"+"  "+e.getMessage());
                } catch (SQLException e)
                {
                    Log.v("yzy", "fail to connect!"+"  "+e.getMessage());
                }
            }
        };
        new Thread(runnable).start();






    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            Log.d("Runnable", "Runnable");
            // handler自带方法实现定时器
            try {
                //每天都执行
                handler.postDelayed(this, 1000*10);
                sendNow();
                System.out.println("do...");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("exception...");
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("onCreate", "onCreate");
        runnable.run();
    }

    public int onStartCommand(Intent intent1, int flags, int startId) {

        //前台通知，保持服务
        Intent intent = new Intent(context, MainActivity.class);
        //intent.putExtra("nolink", "/customer/cosmetologist");
        PendingIntent pintent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.salonbosslogo);//设置图标
        builder.setTicker("");//手机状态栏的提示；
        builder.setWhen(System.currentTimeMillis());//设置时间
        builder.setContentTitle("院长助手");//设置标题
        builder.setContentText("实时订单提醒");//设置通知内容
        builder.setContentIntent(pintent);//点击后的意图
        //builder.setDefaults(Notification.DEFAULT_ALL);//设置震动
        Notification notification = builder.build();//4.1以上
        startForeground(frontnote_ID, notification);

        return super.onStartCommand(intent1, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
