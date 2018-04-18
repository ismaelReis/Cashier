package br.com.alphasignage.caixalivre.Task;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import br.com.alphasignage.caixalivre.Activity.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class DataServerAsyncTask extends
        AsyncTask<Void, Void, String> {

    private TextView statusText;
    private MainActivity activity;

    /**
     * @param statusText
     */
    public DataServerAsyncTask(MainActivity activity, View statusText) {
        this.statusText = (TextView) statusText;
        this.activity=activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            while(true) {
                Log.i("xyz", "data doinback");
                ServerSocket serverSocket = new ServerSocket(8888);
                Log.i("xyz", "Criação de socket concluída");
                Socket client = serverSocket.accept();
                Log.i("xyz", "Cancelado");
                InputStream inputstream = client.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int i;
                while ((i = inputstream.read()) != -1) {
                    baos.write(i);
                }

                final String str = baos.toString();
                serverSocket.close();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("xyz", "data onpost");

                        if(!str.equals("null"))
                        Toast.makeText(activity, "result: "+str, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(activity, "Nova conexão detectada.", Toast.LENGTH_SHORT).show();
                        if (str != null) {
                            statusText.setText("Data-String is " + str);
                        }
                    }
                });
            }
           // return null;

        } catch (IOException e) {
            Log.e("xyz", e.toString());
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {

        Log.i("xyz", "data onpost");

        Toast.makeText(activity, "result: "+result, Toast.LENGTH_SHORT).show();

        if (result != null) {
            statusText.setText("Data-String is " + result);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {

    }

}