package br.com.alphasignage.caixalivre.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.support.v7.app.AppCompatActivity;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import br.com.alphasignage.caixalivre.Adapter.MyAdapter;
import br.com.alphasignage.caixalivre.BroadcastReceiver.WifiDirectBroadcastReceiver;
import br.com.alphasignage.caixalivre.R;
import br.com.alphasignage.caixalivre.Service.DataTransferService;
import br.com.alphasignage.caixalivre.Service.FileTransferService;
import br.com.alphasignage.caixalivre.Task.DataServerAsyncTask;
import br.com.alphasignage.caixalivre.Task.FileServerAsyncTask;
import br.com.alphasignage.caixalivre.Utils.Configuracao;
import br.com.alphasignage.caixalivre.Utils.Utils;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private static final long INTERVALO_CHAMAR = 1000;
    private Button discover;
    private Button stopdiscover;
    private Button stopconnect;
    private Button sendpicture;
    private Button senddata;
    private Button begrouppwener;

    boolean chamou = false;

    private Switch swtPrefencial;

    private TextView txtNumCaixa;
    private EditText edtNumCaixa;

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private List peers = new ArrayList();
    private List<HashMap<String, String>> peersshow = new ArrayList();

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private WifiP2pInfo info;

    private FileServerAsyncTask mServerTask;
    private DataServerAsyncTask mDataTask;

    private Utils utils;
    private Spinner spAtendimentos;
    private boolean editandoCaixa = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        initIntentFilter();
        initReceiver();
        initEvents();
        initDefaultConnection();
    }
    private void initDefaultConnection(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!Configuracao.getSend_address(getApplicationContext()).equals("")){//não está vazia
                    DiscoverPeers();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Buscando padrao",Toast.LENGTH_SHORT).show();

                            CreateConnect(Configuracao.getSend_address(getApplicationContext()),"");
                        }

                    },2000);
             }
            }
        },2000);

    }


    private void initEvents() {

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopConnect();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DiscoverPeers();
                    }
                },2000);


            }
        });
        begrouppwener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeGroupOwener();
            }
        });

        stopdiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopDiscoverPeers();
            }
        });
        stopconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopConnect();
            }
        });
        sendpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 20);
            }
        });

        final Handler handler = new Handler();
        
        senddata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!chamou) {
                    shakeItBaby();
                    Intent serviceIntent = new Intent(MainActivity.this,
                            DataTransferService.class);
                    //adicionar extras aqui
                    serviceIntent.putExtra("data",txtNumCaixa.getText()+";"+spAtendimentos.getSelectedItemPosition()+";"+swtPrefencial.isChecked());
                    serviceIntent.setAction(DataTransferService.ACTION_SEND_FILE);

                    serviceIntent.putExtra(DataTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            info.groupOwnerAddress.getHostAddress());
                    Log.i("address", "owenerip is " + info.groupOwnerAddress.getHostAddress());
                    serviceIntent.putExtra(DataTransferService.EXTRAS_GROUP_OWNER_PORT,
                            8888);
                    MainActivity.this.startService(serviceIntent);

                    //mexe no botao
                    senddata.setAlpha(0.5f);
                    senddata.setEnabled(false);
                    Toast.makeText(getApplicationContext(),"Você chamou para seu caixa.",Toast.LENGTH_SHORT).show();
                    chamou = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chamou  = false;
                            senddata.setAlpha(1f);
                            senddata.setEnabled(true);
                        }
                    },INTERVALO_CHAMAR);
                }
            }
        });


        mAdapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {

                CreateConnect(peersshow.get(position).get("address"),
                        peersshow.get(position).get("name"));


            }

            @Override
            public void OnItemLongClick(View view, int position) {
            }
        });
    }
    private void shakeItBaby() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
        }
    }

    private void BeGroupOwener() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 20) {
            super.onActivityResult(requestCode, resultCode, data);
            Uri uri = data.getData();
            Intent serviceIntent = new Intent(MainActivity.this,
                    FileTransferService.class);

            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
                    uri.toString());

            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                    8988);
            MainActivity.this.startService(serviceIntent);
        }
    }

    private void StopConnect() {
        TextView view = (TextView) findViewById(R.id.tv_main);
        view.setText("Desconectado");
        SetButtonGone();
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    /*A demo base on API which you can connect android device by wifidirect,
    and you can send file or data by socket,what is the most important is that you can set
    which device is the client or service.*/

    private void CreateConnect(final String address, final String name) {
        WifiP2pDevice device;

        WifiP2pConfig config = new WifiP2pConfig();

        Log.i("xyz", address);

        config.deviceAddress = address;
        /*mac地址*/

        config.wps.setup = WpsInfo.PBC;
        Log.i("address", "MAC IS " + address);
        if (address.equals("9a:ff:d0:23:85:97")) {
            config.groupOwnerIntent = 0;
            Log.i("address", "lingyige shisun");
        }
        if (address.equals("36:80:b3:e8:69:a6")) {
            config.groupOwnerIntent = 15;
            Log.i("address", "lingyigeshiwo");

        }

        Log.i("address", "lingyige youxianji" + String.valueOf(config.groupOwnerIntent));

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("xyz","Sucesso salvando para proximos.");
                Configuracao.setSend_address(address,getApplicationContext());
                mRecyclerView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int reason) {
                Log.d("xyz","Falha."+reason);
                if(reason == WifiP2pManager.BUSY)
                {
                    StopConnect();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DiscoverPeers();
                        }
                    },2000);
                }
             //   Configuracao.setSend_address("",getApplicationContext());
                mRecyclerView.setVisibility(View.GONE);
                TextView view = (TextView) findViewById(R.id.tv_main);
                view.setText("Falha ao se conectar, tente novamente.");

            }
        });
    }

    private void StopDiscoverPeers() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {


            }
        });
    }

    private void initView() {

        begrouppwener= (Button) findViewById(R.id.bt_bgowner);
        stopdiscover = (Button) findViewById(R.id.bt_stopdiscover);
        discover = (Button) findViewById(R.id.bt_discover);
        stopconnect = (Button) findViewById(R.id.bt_stopconnect);
        sendpicture = (Button) findViewById(R.id.bt_sendpicture);
        senddata = (Button) findViewById(R.id.bt_senddata);
        swtPrefencial = (Switch) findViewById(R.id.switch1);
        sendpicture.setVisibility(View.GONE);
        senddata.setVisibility(View.GONE);


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        mAdapter = new MyAdapter(peersshow);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager
                (this.getApplicationContext()));

        spAtendimentos = (Spinner) findViewById(R.id.spAtendimentos);
        List<String> list = new ArrayList<String>();
        list.add("Padrão");
        list.add("Jogos");
        list.add("Pagamentos");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAtendimentos.setAdapter(dataAdapter);

        ImageView imgHelpAtd = findViewById(R.id.imgHelpAtd);
        imgHelpAtd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Atendimentos podem ser criados no aplicativo central",Toast.LENGTH_SHORT).show();
            }
        });

        final Button btChangeCaixa = findViewById(R.id.btChangeCaixa);
        txtNumCaixa = findViewById(R.id.txtNumCaixa);
        txtNumCaixa.setText(Configuracao.getNum_caixa(getApplicationContext()));
       edtNumCaixa = findViewById(R.id.edtNumCaixa);
       editandoCaixa = false;
        btChangeCaixa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editandoCaixa){
                    txtNumCaixa.setVisibility(View.GONE);
                    edtNumCaixa.setText(txtNumCaixa.getText()+"");
                    edtNumCaixa.setVisibility(View.VISIBLE);
                    edtNumCaixa.requestFocus();
                    edtNumCaixa.selectAll();
                    btChangeCaixa.setText("Salvar");
                    openKeyboard();
                    editandoCaixa = true;
                }else{
                    Configuracao.setNum_caixa(edtNumCaixa.getText()+"",getApplicationContext());//salvando
                    txtNumCaixa.setVisibility(View.VISIBLE);
                    edtNumCaixa.setVisibility(View.GONE);
                    openKeyboard();
                    txtNumCaixa.setText(edtNumCaixa.getText()+"");
                    btChangeCaixa.setText("Alterar");
                    editandoCaixa = false;
                }
            }
        });

    }


    private void openKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }
    private void initReceiver() {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, Looper.myLooper(), null);

        WifiP2pManager.PeerListListener mPeerListListerner = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peersList) {
                peers.clear();
                peersshow.clear();
                Collection<WifiP2pDevice> aList = peersList.getDeviceList();
                peers.addAll(aList);

                for (int i = 0; i < aList.size(); i++) {
                    WifiP2pDevice a = (WifiP2pDevice) peers.get(i);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", a.deviceName);
                    map.put("address", a.deviceAddress);
                    peersshow.add(map);
                }
                mAdapter = new MyAdapter(peersshow);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager
                        (MainActivity.this));
                mAdapter.SetOnItemClickListener(new MyAdapter.OnItemClickListener() {
                    @Override
                    public void OnItemClick(View view, int position) {
                        CreateConnect(peersshow.get(position).get("address"),
                                peersshow.get(position).get("name"));
                       // Log.d("Helllo","ruwold");
                        mRecyclerView.setVisibility(View.GONE);


                    }

                    @Override
                    public void OnItemLongClick(View view, int position) {

                    }
                });
            }
        };

        WifiP2pManager.ConnectionInfoListener mInfoListener = new WifiP2pManager.ConnectionInfoListener() {

            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo minfo) {

                Log.i("xyz", "InfoAvailable is on");
                info = minfo;
                TextView view = (TextView) findViewById(R.id.tv_main);
                if (info.groupFormed && info.isGroupOwner) {
                    Log.i("xyz", "owmer start");

                 /*   mServerTask = new FileServerAsyncTask(MainActivity.this, view);
                    mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/

                    mDataTask = new DataServerAsyncTask(MainActivity.this, view);
                    mDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } else if (info.groupFormed) {
                    view.setText("Conectado a: "+Configuracao.getSend_address(getApplicationContext()));
                    SetButtonVisible();


                }
            }
        };
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, mPeerListListerner, mInfoListener);
    }

    private void SetButtonVisible() {
        //sendpicture.setVisibility(View.VISIBLE);

        senddata.setVisibility(VISIBLE);
    }

    private void SetButtonGone() {
        sendpicture.setVisibility(View.GONE);
        senddata.setVisibility(View.GONE);
    }


    private void DiscoverPeers() {
        mRecyclerView.setVisibility(VISIBLE);
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    private void initIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mFilter);
        initDefaultConnection();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("xyz", "hehehe");
        StopConnect();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStop() {
        StopConnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        StopConnect();
        super.onDestroy();

    }

    public void ResetReceiver() {

        unregisterReceiver(mReceiver);
        registerReceiver(mReceiver, mFilter);

    }
}
