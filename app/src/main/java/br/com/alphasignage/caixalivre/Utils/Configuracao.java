package br.com.alphasignage.caixalivre.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import br.com.alphasignage.caixalivre.R;

/**
 * Created by Escalar Comunicação on 17/04/2018.
 */

public class Configuracao {
    public static String send_address = "";//será a chaver de conexão.
    public static String num_caixa = "";

    public static void setSend_address(String send_address,Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.send_address), send_address);
        editor.apply();
        Configuracao.send_address=send_address;
    }

    public static String getSend_address(Context context){
        if(!send_address.equals(""))
            return send_address;
        else{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String defaultValue = "";
            String groupName = sharedPref.getString(context.getString(R.string.send_address), defaultValue);
            Configuracao.send_address=groupName;
            return groupName;
        }

    }
    public static void setNum_caixa(String num_caixa,Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.num_caixa), num_caixa);
        editor.apply();
        Configuracao.num_caixa=num_caixa;
    }

    public static String getNum_caixa(Context context){
        if(!num_caixa.equals(""))
            return num_caixa;
        else{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String defaultValue = "1";
            String num_caixa = sharedPref.getString(context.getString(R.string.num_caixa), defaultValue);
            Configuracao.num_caixa=num_caixa;
            return num_caixa;
        }

    }
}
