package com.example.irvingsanchez.ecobici;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by irvingsanchez on 10/02/18.
 *
 * Clase que procesa una peticion http
 * y procesa su respuesta retornando un string
 *
 */

public class HTTPDataHandler {

    static String stream = null;
    private static String DEBUG_MESSAGE = "Http request";

    public HTTPDataHandler(){}

    /*
    *   Metodo que ejecuta una solicitud http y que retorna la respuesta del servidor
    *   siempre que la solicitud haya sido exitosa
     */
    public String getHTTPData(String urlString){

        try{
            // Ejecutar peticion http y obtener codigo de conexion
            URL url = new URL(urlString);
            HttpURLConnection urlConnection
                    = (HttpURLConnection) url.openConnection();
            int conncode = urlConnection.getResponseCode();
            Log.i(DEBUG_MESSAGE, "Codigo " + conncode);
            //  Si la respuesta fue exitosa obtener respuesta y procesarla a un string
            if(conncode == 200){
                InputStream in =
                        new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = r.readLine()) != null){
                    sb.append(line);
                }
                stream = sb.toString();
                urlConnection.disconnect();
            }

        }catch (MalformedURLException e){
            Log.i(DEBUG_MESSAGE, "MalformedURL");
            e.printStackTrace();
        }catch (IOException e){
            Log.i(DEBUG_MESSAGE,"IOException");
            e.printStackTrace();
        }finally {

        }
        return stream;
    }

}