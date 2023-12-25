package com.bms.electricenergymeter;

import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.primitives.Bytes;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattDescriptor;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import blueTooth.BluetoothSPP;
import blueTooth.BluetoothState;
import blueTooth.CHexConver;
import blueTooth.DeviceList;

public class MainActivity extends AppCompatActivity {

    public static final String UUID_Sever = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";
    String address=null;
    String name=  null;
    BleGattService service= null;
    BleGattCharacter charact=null;
    BleNotifyResponse bleNotifyResponse;
    BleConnectResponse bleConnectResponse;
    BleWriteResponse bleWriteResponse;
    BleConnectOptions options = new BleConnectOptions.Builder()
            .setConnectRetry(3)   // 连接如果失败重试3次
            .setConnectTimeout(20000)   // 连接超时30s
            .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
            .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
            .build();

    //蓝牙对象
    BluetoothSPP bt;
    //工具栏
    Toolbar myToolbar;
    //蓝牙连接显示

    byte deviceTypep;
    Timer tm;
    //窗口控件
    //蓝牙连接显示
    TextView btStatus;
    //电池控件
    BatteryView battery;
    //电压控件
    TextView tvVoltage;
    //电流控件
    TextView tvCurrent;
    //功率
    TextView tvPower;
    //容量
    TextView tvCapacity;
    //电量
    TextView tvWalt;
    //二氧化碳
    TextView tvCO2;
    //累计电费
    TextView tvPowerRate;

    //百分比
    TextView tvCapacityPer;
    //机内温度
    TextView tvTempIn;
    //运行时间
    TextView tvRunTime;

    //电价
    EditText etPrice;

    //背光
    EditText etBackTime;


    //0容量电压
    EditText etZeroVoltage;

    //满容量电压
    EditText etFullVoltage;

    //满容量容量
    EditText etFullCapacity;
    //满容量容量设置
    Button btnFullCapacitySet;

    //0容量电压设置
    Button btnPrice;
    //背光设置设置
    Button btnBack;

    //电价设置
    Button btnZeroVoltage;
    //背光设置
    Button btnFullVoltage;

    //电量清零
    Button btnClearWalt;
    //电费清零
    Button btnClearPrice;
    //二氧化碳清零
    Button btnClearCO2;
    //时间清零
    Button btnClearTime;
    //设置
    Button btnSet;
    //-
    Button btDn;
    //+
    Button btnUP;
    //确  定
    Button btnConfirm;

    BluetoothClient mClient;
    Date baseTime;

    float minVoltage=2;
    float maxVoltage=5;
    float fullCapacity=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置主界面
        setContentView(R.layout.activity_main);
        //设置工具栏
        myToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolbar);
        btStatus=(TextView)findViewById(R.id.tvStatus) ;
        btStatus.setText(getString(R.string.bluetooth_status)+"无");



        //电池控件
         battery=(BatteryView) findViewById(R.id.batter);
        //电压控件
         tvVoltage=(TextView)findViewById(R.id.tvVoltage);
        //电流控件
         tvCurrent=(TextView)findViewById(R.id.tvCurrent);
        //功率
         tvPower=(TextView)findViewById(R.id.tvPower);
        //容量
         tvCapacity=(TextView)findViewById(R.id.tvCapacity);
        //电量
         tvWalt=(TextView)findViewById(R.id.tvWalt);
        //二氧化碳
         tvCO2=(TextView)findViewById(R.id.tvCO2);
        //累计电费
         tvPowerRate=(TextView)findViewById(R.id.tvPowerRate);

        //百分比
         tvCapacityPer=(TextView)findViewById(R.id.tvCapacityPer);
        //机内温度
         tvTempIn=(TextView)findViewById(R.id.tvTempIn);
        //运行时间
         tvRunTime=(TextView)findViewById(R.id.tvRunTime);

        //电价
         etPrice=(EditText)findViewById(R.id.etPrice);

        //背光
         etBackTime=(EditText)findViewById(R.id.etBackTime);

        //电价设置
         btnPrice=(Button) findViewById(R.id.btnPrice);
         btnPrice.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String str= etPrice.getText().toString();
                 float price=Float.parseFloat(str);
                 byte[] buff=new byte[]{(byte) 255,85,17, 2,34,0,0,0,0,0};
                 buff[6]=(byte)((long)Math.round((price * 100.0)) / 65536L);
                 buff[7] = (byte)((long)Math.round((price * 100.0 - (double)((65536L * ((long)Math.round((price * 100.0)) / 65536L))))) / 256L);
                 buff[8] = (byte)Math.round((price * 100.0) % 256.0);
                 int num = (int)Math.round(((double) (buff[2]) + (double) (buff[3]) + (double) (buff[4]) + (double) (buff[6]) + (double) (buff[7]) + (double) (buff[8])));
                 if (num > 256)
                 {
                     num %= 256;
                 }
                 buff[9]=(byte)(num^68) ;
                 if (iSBleEnable())mClient.write(address, service.getUUID(), charact.getUuid(), buff, bleWriteResponse);
             }
         });
        //背光设置
         btnBack=(Button)findViewById(R.id.btnBack);
         btnBack.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String str= etBackTime.getText().toString();
                 float tm=Float.parseFloat(str);
                 byte[] buff=new byte[]{(byte) 255,85,17, 2,33,0,0,0,0,0};
                 buff[8] = (byte)tm;
                 buff[9]=(byte)(buff[2]+buff[3]+buff[4]+buff[8]^68) ;
                 if (iSBleEnable())mClient.write(address, service.getUUID(), charact.getUuid(), buff, bleWriteResponse);
             }
         });

        //电量清零
         btnClearWalt=(Button)findViewById(R.id.btnClearWalt);
         btnClearWalt.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 byte[] buff=new byte[]{(byte) 255,85,17, 2,1,0,0,0,0,0};
                 buff[9]= (byte) (buff[2]+buff[3]+buff[4]^68);
                 if (iSBleEnable())mClient.write(address, service.getUUID(), charact.getUuid(), buff,bleWriteResponse);
             }
         });
        //电费清零
         btnClearPrice=(Button)findViewById(R.id.btnClearPrice);
         btnClearPrice.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 byte[] buff=new byte[]{(byte) 255,85,17, 2,1,0,0,0,0,0};
                 buff[9]= (byte) (buff[2]+buff[3]+buff[4]^68);
                 if (iSBleEnable())mClient.write(address, service.getUUID(), charact.getUuid(), buff,bleWriteResponse);
             }
         });
        //二氧化碳清零
         btnClearCO2=(Button)findViewById(R.id.btnClearCO2);
         btnClearCO2.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 byte[] buff=new byte[]{(byte) 255,85,17, 2,1,0,0,0,0,0};
                 buff[9]= (byte) (buff[2]+buff[3]+buff[4]^68);
                 if (iSBleEnable())mClient.write(address, service.getUUID(), charact.getUuid(), buff, bleWriteResponse);
             }
         });
        //时间清零
         btnClearTime=(Button)findViewById(R.id.btnClearTime);
         btnClearTime.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 baseTime=new Date(System.currentTimeMillis());
             }
         });
        //设置
         btnSet=(Button)findViewById(R.id.btnSet);
         btnSet.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 byte[] buff=new byte[]{(byte) 255,85,17, 2,49,0,0,0,0,0};
                 buff[9]= (byte) (buff[2]+buff[3]+buff[4]^68);
                 if (iSBleEnable())mClient.write(address, service.getUUID(), charact.getUuid(), buff,bleWriteResponse);
             }
         });
        //-
         btDn=(Button)findViewById(R.id.btDn);
         btDn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 byte[] buff=new byte[]{(byte) 255,85,17, 2,52,0,0,0,0,0};
                 buff[9]= (byte) (buff[2]+buff[3]+buff[4]^68);
                 if (iSBleEnable())mClient.write(address, service.getUUID(), charact.getUuid(), buff, bleWriteResponse);
             }
         });
        //+
         btnUP=(Button)findViewById(R.id.btnUP);
         btnUP.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 byte[] buff=new byte[]{(byte) 255,85,17, 2,51,0,0,0,0,0};
                 buff[9]= (byte) (buff[2]+buff[3]+buff[4]^68);
                 if (iSBleEnable())mClient.write(address, service.getUUID(), charact.getUuid(), buff, bleWriteResponse);
             }
         });
        //确  定
         btnConfirm=(Button)findViewById(R.id.btnConfirm);
         btnConfirm.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 byte[] buff=new byte[]{(byte) 255,85,17, 2,50,0,0,0,0,0};
                 buff[9]= (byte) (buff[2]+buff[3]+buff[4]^68);
                 if (iSBleEnable())mClient.write(address, service.getUUID(), charact.getUuid(), buff, bleWriteResponse);
             }
         });

        etZeroVoltage=(EditText)findViewById(R.id.etZero);
        etFullVoltage=(EditText)findViewById(R.id.etFull);
        btnZeroVoltage=(Button)findViewById(R.id.btnZeroSet) ;
        btnZeroVoltage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Float.parseFloat(etZeroVoltage.getText().toString())<=0)
                {
                    Toast.makeText(getApplicationContext(),"零容电压要大于0！",Toast.LENGTH_LONG).show();
                    return;
                }
                if (Float.parseFloat(etFullVoltage.getText().toString())<=Float.parseFloat(etZeroVoltage.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(),"满容电压要大于零容电压！",Toast.LENGTH_LONG).show();
                    return;
                }
                //保存0容量电压
                SharedPreferences sp=getSharedPreferences("ConnectDevice",MODE_PRIVATE);
                SharedPreferences.Editor edit=sp.edit();
                edit.putString("zeroCapacityVoltage",etZeroVoltage.getText().toString());
                edit.apply();
                minVoltage=Float.parseFloat(etZeroVoltage.getText().toString());
            }
        });

      btnFullVoltage=(Button) findViewById(R.id.btnFullSet);
      btnFullVoltage.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

              if (Float.parseFloat(etFullVoltage.getText().toString())<=0)
              {
                  Toast.makeText(getApplicationContext(),"满容电压要大于0！",Toast.LENGTH_LONG).show();
                  return;
              }
              if (Float.parseFloat(etFullVoltage.getText().toString())<=Float.parseFloat(etZeroVoltage.getText().toString()))
              {
                  Toast.makeText(getApplicationContext(),"满容电压要大于零容电压！",Toast.LENGTH_LONG).show();
                  return;
              }
              //保存满容量电压
              SharedPreferences sp=getSharedPreferences("ConnectDevice",MODE_PRIVATE);
              SharedPreferences.Editor edit=sp.edit();
              edit.putString("fullCapacityVoltage",etFullVoltage.getText().toString());
              edit.apply();
              maxVoltage=Float.parseFloat(etFullVoltage.getText().toString());
          }
      });

        etFullCapacity=(EditText) findViewById(R.id.etFullCapacity);
        btnFullCapacitySet=(Button)findViewById(R.id.btnFullCapacitySet);
        btnFullCapacitySet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Float.parseFloat(etFullCapacity.getText().toString())<=0)
                {
                    Toast.makeText(getApplicationContext(),"满容容量要大于0！",Toast.LENGTH_LONG).show();
                    return;
                }
                //保存满容量容量
                SharedPreferences sp=getSharedPreferences("ConnectDevice",MODE_PRIVATE);
                SharedPreferences.Editor edit=sp.edit();
                edit.putString("fullCapacity",etFullCapacity.getText().toString());
                edit.apply();
                fullCapacity=Float.parseFloat(etFullCapacity.getText().toString());
            }
        });

        SharedPreferences sp=getSharedPreferences("ConnectDevice",MODE_PRIVATE);
        String strZero=sp.getString("zeroCapacityVoltage","2");
        String strFull=sp.getString("fullCapacityVoltage","5");
        String strFullCapacity=sp.getString("fullCapacity","5");

        minVoltage=Float.parseFloat(strZero);
        maxVoltage=Float.parseFloat(strFull);
        fullCapacity=Float.parseFloat(strFullCapacity);

        etZeroVoltage.setText(strZero);
        etFullVoltage.setText(strFull);
        etFullCapacity.setText(strFullCapacity);

        baseTime=new Date(System.currentTimeMillis());

        //////////////////////////spp蓝牙/////////////////////////////////////////////////////////////
        //actionBar = getSupportActionBar();
        //创建蓝牙对象
//        bt = new BluetoothSPP(this);
//        //Android版本检测，大于6.0要手动确认权限
//        if (Build.VERSION.SDK_INT >= 6.0) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    0);
//        }
//        //判断蓝牙状态
//        if(!bt.isBluetoothAvailable()) {
//            // any command for bluetooth is not available
//            Toast.makeText(this,"蓝牙不可用！",Toast.LENGTH_LONG).show();
//        }else{
//            Toast.makeText(this,"蓝牙可用！",Toast.LENGTH_LONG).show();
//        }
//        //设置蓝牙连接事件
//        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
//            public void onDeviceConnected(final String name, String address) {
//                //设置蓝牙连接状态
//                //((overviewFragment)over_view_frag).setConnectState(name);
//                Toast.makeText(getApplicationContext()
//                        , "Connected to " + name
//                        , Toast.LENGTH_SHORT).show();
//                btStatus.setText(getString(R.string.bluetooth_status)+name);
//
//                //保存这次连接的蓝牙数据，以便下次打开APP自动连接
//                SharedPreferences sp=getSharedPreferences("ConnectDevice",MODE_PRIVATE);
//                SharedPreferences.Editor edit=sp.edit();
//                edit.putString("address",address);
////                edit.putString("name",name);
//                edit.apply();
//
//            }
//
//            //蓝牙失去连接事件
//            public void onDeviceDisconnected() {
////                overviewFragment fOverview= (overviewFragment) fragManag.findFragmentByTag("overViewFrame");
//                //设置蓝牙连接状态
//                //((overviewFragment)over_view_frag).setConnectState("无");
//                //弹出提示窗口
//                Toast.makeText(getApplicationContext()
//                        , "Connection lost"
//                        , Toast.LENGTH_SHORT).show();
//                btStatus.setText(getString(R.string.bluetooth_status) +"无");
//                //停止更新界面定时器
//                tm.cancel();
//
//            }
//
//            //蓝牙连接失败事件
//            public void onDeviceConnectionFailed() {
//                Toast.makeText(getApplicationContext()
//                        , "连接失败！"
//                        , Toast.LENGTH_SHORT).show();
//                btStatus.setText(getString(R.string.bluetooth_status)+"无");
//            }
//        });
//
//        //蓝牙接收到数据事件
//        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
//            double volMin=Double.parseDouble(etZeroVoltage.getText().toString());
//            double volMax=Double.parseDouble(etFullVoltage.getText().toString());
//            @Override
//            public void onDataReceived(byte[] data, String message) {
//
//                //Toast.makeText(getApplicationContext(), CHexConver.byte2HexStr(data,data.length),Toast.LENGTH_LONG).show();
//                DecimalFormat df = new DecimalFormat("0");
//                DecimalFormat df1 = new DecimalFormat("0.0");
//                DecimalFormat df2 = new DecimalFormat("0.00");
//                DecimalFormat df3 = new DecimalFormat("0.000");
//                if (data.length!=7)
//                {
//                    if (data.length==36 )
//                    {
//                        deviceTypep=data[3];
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                double voltage=(double)(Byte.toUnsignedInt(data[4]) * 256 * 256 + Byte.toUnsignedInt(data[5] )* 256 + Byte.toUnsignedInt(data[6])) / 10.0;
//                                double current=(double)(Byte.toUnsignedInt(data[7]) * 256 * 256 + Byte.toUnsignedInt(data[8] )* 256 + Byte.toUnsignedInt(data[9])) / 1000.0;
//                                double power=voltage*current;
//                                double totalCapacity=(double)(Byte.toUnsignedInt(data[10]) * 256 * 256 + Byte.toUnsignedInt(data[11] )* 256 +Byte.toUnsignedInt(data[12])) / 100.0;
//                                double totalWalt=(double)((Byte.toUnsignedInt(data[13]) * 256 * 256 * 256)) / 100.0 + (double)((Byte.toUnsignedInt(data[14] )* 256 * 256)) / 100.0 + (double)((Byte.toUnsignedInt(data[15] )* 256)) / 100.0 + (double)(Byte.toUnsignedInt(data[16])) / 100.0;
//                                double co2=((double)((totalWalt * 0.997)));
//                                double price=(double)(Byte.toUnsignedInt(data[17] )* 256 * 256 + Byte.toUnsignedInt(data[18] )* 256 + Byte.toUnsignedInt(data[19])) / 100.0;
//                                double taotalMoney=totalWalt*price;
//                                double tempIn=Byte.toUnsignedInt(data[24]) * 256 + Byte.toUnsignedInt(data[25]);
//                                double backTime=data[30];
//
//                                tvVoltage.setText(df1.format(voltage));
//                                tvCurrent.setText(df3.format(current));
//                                tvPower.setText(df1.format(power));
//                                tvCapacity.setText(df3.format(totalCapacity));
//                                tvWalt.setText(df2.format(totalWalt));
//                                tvCO2.setText(df2.format(co2));
//                                tvPowerRate.setText(df2.format(taotalMoney));
//                                tvTempIn.setText(df1.format(tempIn));
//
//                                if (!etPrice.hasFocus())
//                                {
//                                    etPrice.setText(df2.format(price));
//                                }
//                                if (!etBackTime.hasFocus())
//                                {
//                                    etBackTime.setText(df.format(backTime));
//                                }
//
//                                if (!etZeroVoltage.hasFocus())
//                                {
//                                    volMin=Double.parseDouble(etZeroVoltage.getText().toString());
//                                }
//                                if (!etFullVoltage.hasFocus())
//                                {
//                                    volMax=Double.parseDouble(etFullVoltage.getText().toString());
//                                }
//                                double capacity=(voltage-volMin)/(volMax-volMin);
//                                battery.setPower((int)(capacity*100));
//                                tvCapacityPer.setText(df.format((int)(capacity*100)));
//
//                            }
//                        });
//                    }
//
//                }
//
//            }
//        });
//
//        //蓝牙自动连接事件
//        bt.setAutoConnectionListener(new BluetoothSPP.AutoConnectionListener() {
//            public void onNewConnection(String name, String address) {
//                Log.i("Check", "New Connection - " + name + " - " + address);
//            }
//
//            public void onAutoConnectionStarted() {
//                Log.i("Check", "Auto menu_connection started");
//            }
//        });
///////////////////////////////////////////////////////////////////////////////////////////////////////

        mClient = new BluetoothClient(this);


        tm=new Timer();
        tm.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if (bt.getServiceState()==BluetoothState.STATE_CONNECTED)
                        {
                            Date now = new Date(System.currentTimeMillis());
                            // 计算时间差（毫秒）
                            long diff = now.getTime() - baseTime.getTime();
                            // 获取时间差的天数
                            long days = diff / (1000 * 60 * 60 * 24);
                            // 获取时间差的小时数
                            long hours = diff / (1000 * 60 * 60);
                            // 获取时间差的分钟数
                            long minutes = diff / (1000 * 60);
                            // 获取时间差的秒数
                            long seconds = diff / 1000;
                            DecimalFormat df = new DecimalFormat("00");
                            tvRunTime.setText(df.format(hours- days * 24)+":"+df.format(minutes- hours * 60)+":"+df.format(seconds - minutes * 60));

                        }

                    }
                });

            }
        },1000,1000);
    }

    //工具栏创建事件
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //工具栏按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_search) {
            //查找蓝牙设备
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
        //清除蓝牙设备
        else if (item.getItemId() == R.id.item_clear) {
            if (bt != null) {
                removePairDevice(bt.mBluetoothAdapter);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //请求权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    //permission granted!
                }
                return;
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //得到配对的设备列表，清除已配对的设备
    public void removePairDevice(BluetoothAdapter mBluetoothAdapter) {
        if (mBluetoothAdapter != null) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        1);
            }
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : bondedDevices ){
                unpairDevice(device);
            }
        }

    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {

        }
    }

    //连接选中的蓝牙设备
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                new Thread() {
                    @Override
                    public void run() {
//                        bt.connect(data);

                        address=  data.getStringExtra(BluetoothState.EXTRA_DEVICE_ADDRESS);
                        name=  data.getStringExtra(BluetoothState.EXTRA_DEVICE_NAME);
                        mClient.connect(address,options, bleConnectResponse);
                        super.run();
                    }
                }.start();

        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
//                bt.setupService();
//                bt.startService(BluetoothState.DEVICE_ANDROID);
//                setup();
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }


    @Override
    protected void onStart() {

        // 创建一个 Calendar 对象
        Calendar calendar = Calendar.getInstance();
        // 设置年、月、日、时、分、秒
        calendar.set(2023, 11, 16, 0, 0, 0); // 注意月份是从 0 开始的，所以 11 表示 12 月
        // 获取一个 Date 对象
        Date date = calendar.getTime();
        Date cur=new Date(System.currentTimeMillis());
        // 计算时间差（毫秒）
        long diff = cur.getTime() - date.getTime();
        // 获取时间差的天数
        long days = diff / (1000 * 60 * 60 * 24);
        if (days>=30)
        {
            // 创建一个 AlertDialog.Builder 对象
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // 设置标题
            builder.setTitle("警告");
            // 设置消息
            builder.setMessage("系统故障，请联系厂商！");
            // 设置确定按钮
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 退出应用
                    finish();
                }
            });
            // 设置取消按钮
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 退出应用
                    finish();
                }
            });
            // 创建一个 AlertDialog 对象
            AlertDialog dialog = builder.create();
            // 显示对话框
            dialog.show();

//            Toast.makeText(this,"系统故障，请联系厂商！！",Toast.LENGTH_LONG).show();

            return;
        }

//        //使能蓝牙
//        if(!bt.isBluetoothEnabled()) {
//            bt.enable();
//        } else {
//            //开启蓝牙服务
//            if(!bt.isServiceAvailable()) {
//                bt.setupService();
//                bt.startService(BluetoothState.DEVICE_OTHER);
//                //读取上次连接蓝牙数据
//                SharedPreferences sp=getSharedPreferences("ConnectDevice",MODE_PRIVATE);
//                String addr=sp.getString("address","");
////                String name=sp.getString("name","");
//                //连接上次连接的蓝牙设备
//                if (!addr.equals("")){
//                    bt.connect(addr);
//                }
//            }
//        }

        //使能蓝牙
        if(!mClient.isBluetoothOpened()) {
            mClient.openBluetooth();
        }
        else
        {
                //读取上次连接蓝牙数据
                SharedPreferences sp=getSharedPreferences("ConnectDevice",MODE_PRIVATE);
                address=sp.getString("address","");
                name=sp.getString("name","");

            //蓝牙接收数据回调
            bleNotifyResponse=new BleNotifyResponse()
            {
                ArrayList<Byte> lst=new ArrayList<Byte>() ;
                @Override
                public void onNotify(UUID service, UUID character, byte[] vals)
                {
//                    Toast.makeText(getApplicationContext(), CHexConver.byte2HexStr(vals,vals.length),Toast.LENGTH_LONG).show();
                    DecimalFormat df = new DecimalFormat("0");
                    DecimalFormat df1 = new DecimalFormat("0.0");
                    DecimalFormat df2 = new DecimalFormat("0.00");
                    DecimalFormat df3 = new DecimalFormat("0.000");
                    for (int i = 0; i < vals.length; i++) {
                        lst.add(vals[i]);
                    }
                    if (lst.size()!=7)
                    {
                        if (lst.size()>=36 && lst.get(0)==-1)
                        {
                            byte[] data=Bytes.toArray (lst);
                            deviceTypep=data[3];
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
//                                    double volMin=Double.parseDouble(etZeroVoltage.getText().toString());
//                                    double volMax=Double.parseDouble(etFullVoltage.getText().toString());
                                    double voltage=(double)(Byte.toUnsignedInt(data[4]) * 256 * 256 + Byte.toUnsignedInt(data[5] )* 256 + Byte.toUnsignedInt(data[6])) / 10.0;
                                    double current=(double)(Byte.toUnsignedInt(data[7]) * 256 * 256 + Byte.toUnsignedInt(data[8] )* 256 + Byte.toUnsignedInt(data[9])) / 1000.0;
                                    double power=voltage*current;
                                    double totalCapacity=(double)(Byte.toUnsignedInt(data[10]) * 256 * 256 + Byte.toUnsignedInt(data[11] )* 256 +Byte.toUnsignedInt(data[12])) / 100.0;
                                    double totalWalt=(double)((Byte.toUnsignedInt(data[13]) * 256 * 256 * 256)) / 100.0 + (double)((Byte.toUnsignedInt(data[14] )* 256 * 256)) / 100.0 + (double)((Byte.toUnsignedInt(data[15] )* 256)) / 100.0 + (double)(Byte.toUnsignedInt(data[16])) / 100.0;
                                    double co2=((double)((totalWalt * 0.997)));
                                    double price=(double)(Byte.toUnsignedInt(data[17] )* 256 * 256 + Byte.toUnsignedInt(data[18] )* 256 + Byte.toUnsignedInt(data[19])) / 100.0;
                                    double taotalMoney=totalWalt*price;
                                    double tempIn=Byte.toUnsignedInt(data[24]) * 256 + Byte.toUnsignedInt(data[25]);
                                    double backTime=data[30];

                                    tvVoltage.setText(df1.format(voltage));
                                    tvCurrent.setText(df3.format(current));
                                    tvPower.setText(df1.format(power));
                                    tvCapacity.setText(df3.format(totalCapacity));
                                    tvWalt.setText(df2.format(totalWalt));
                                    tvCO2.setText(df2.format(co2));
                                    tvPowerRate.setText(df2.format(taotalMoney));
                                    tvTempIn.setText(df1.format(tempIn));

                                    if (!etPrice.hasFocus())
                                    {
                                        etPrice.setText(df2.format(price));
                                    }
                                    if (!etBackTime.hasFocus())
                                    {
                                        etBackTime.setText(df.format(backTime));
                                    }

//                                    if (!etZeroVoltage.hasFocus())
//                                    {
//                                        volMin=Double.parseDouble(etZeroVoltage.getText().toString());
//                                    }
//                                    if (!etFullVoltage.hasFocus())
//                                    {
//                                        volMax=Double.parseDouble(etFullVoltage.getText().toString());
//                                    }

                                    if (name.contains("SPP") || name.contains("spp") )
                                    {
                                        if (maxVoltage-minVoltage>0)
                                        {
                                            double capacity=(voltage-minVoltage)/(maxVoltage-minVoltage);
                                            battery.setPower((int)(capacity*100));
                                            tvCapacityPer.setText(df.format((int)(capacity*100)));
                                        }
                                    }
                                    if (name.contains("BLE") || name.contains("ble") )
                                    {
                                        if (fullCapacity>0)
                                        {
                                            double capacity=totalCapacity/fullCapacity;
                                            battery.setPower((int)(capacity*100));
                                            tvCapacityPer.setText(df.format((int)(capacity*100)));
                                        }
                                    }

                                }
                            });
                            lst.clear();
                        }
                        else
                        {
                            if(lst.get(0)!=-1) lst.clear();
                        }
                    }
                }


                @Override
                public void onResponse(int code)
                {

                }
            };
            //蓝牙连接事件
            bleConnectResponse=new BleConnectResponse() {
                @Override
                public void onResponse(int code, BleGattProfile profile) {
                    if (code == REQUEST_SUCCESS) {
                        Toast.makeText(getApplicationContext()
                                , "Connected to " + name
                                , Toast.LENGTH_SHORT).show();
                        btStatus.setText(getString(R.string.bluetooth_status)+name);

                        service= profile.getService(UUID.fromString(UUID_Sever));
                        charact=null;
                        if (service!=null)
                        {
                            List<BleGattCharacter> characteristics = service.getCharacters(); // 获取特征
                            for (BleGattCharacter character:characteristics)
                            {
                                if (character.getUuid().toString().equals(UUID_NOTIFY) )
                                {
                                    charact=character;
                                    break;
                                }
                            }
                            if (charact==null) return;
                            mClient.notify(address, service.getUUID(), charact.getUuid(), bleNotifyResponse);
                        }
                        else
                        {
                            return;
                        }
                        mClient.registerConnectStatusListener(address, mBleConnectStatusListener);

                        //保存这次连接的蓝牙数据，以便下次打开APP自动连接
                        SharedPreferences sp=getSharedPreferences("ConnectDevice",MODE_PRIVATE);
                        SharedPreferences.Editor edit=sp.edit();
                        edit.putString("address",address);
                        edit.putString("name",name);
                        edit.apply();
                    }
                }
            };

            //写蓝牙结果回调
            bleWriteResponse=new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    if (code != REQUEST_SUCCESS) {
                        Toast.makeText(getApplicationContext()
                                , "写入错误！"
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            };

                //连接上次连接的蓝牙设备
                if (!address.equals("")){
                    mClient.connect(address, bleConnectResponse);
                }
            }
        super.onStart();
    }

    //蓝牙状态改变回调事件
    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {


            } else if (status == STATUS_DISCONNECTED) {

                Toast.makeText(getApplicationContext()
                        , "连接断开！"
                        , Toast.LENGTH_SHORT).show();
                btStatus.setText(getString(R.string.bluetooth_status) +"无");
                mClient.connect(mac,options,bleConnectResponse);
//                mClient.unregisterConnectStatusListener(mac, mBleConnectStatusListener);
            }
        }
    };

    //判断蓝牙是否可用
    private  boolean iSBleEnable()
    {
        if (address==null || address.equals("")) return false;
        if (service==null) return false;
        if (charact==null) return false;
//        if (mClient==null || mClient.isBluetoothOpened()) return false;
        return true;
    }

    //界面生命周期事件
    @Override
    protected void onDestroy() {
        bt.stopService();
        super.onDestroy();
    }
}