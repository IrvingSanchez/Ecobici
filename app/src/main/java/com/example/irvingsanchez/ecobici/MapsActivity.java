package com.example.irvingsanchez.ecobici;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    //  Constantes
    final private String URIECOBICI = "https://api.citybik.es/v2/networks/ecobici";

    //  Variables
    private ArrayList<EstacionEcobici> estaciones = new ArrayList<>();
    private SupportMapFragment mapFragment;
    private FloatingActionButton botonNavegar;
    private Marker marcadorSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Obtain the SupportMapFragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment);
        //  Vincular variable con FAB del UI y ocultar boton en espera que se seleccione
        //  una estacion para navegar hacia ella
        botonNavegar = (FloatingActionButton) findViewById(R.id.fabNavegar);
        botonNavegar.setVisibility(View.INVISIBLE);

        //  Ejecutar AsyncTask para poder obtener y parsear los datos de las ecobicis
        new GetData().execute(URIECOBICI);
    }

    /*
    *   Metodo para notificar que ya se pueden crear los marcadores
    *   una vez que el mapa y los datos del web services están cargados
     */
    public void cargarMapa() {
        mapFragment.getMapAsync(this);
    }

    /*
    *   Metodo que se invoca al presionar el FAB navegar,
    *   utiliza el marcador seleccionado para navegar mediante waze
    *   hacia la cicloestacion
     */
    public void navegarWaze(View v) {
        try {
            //  Obtener posicion del marcador seleccionado
            double latitude = marcadorSeleccionado.getPosition().latitude;
            double longitude = marcadorSeleccionado.getPosition().longitude;

            //  Construir URI y abrir waze
            String uri = "https://waze.com/ul?ll=" + latitude + "," + longitude;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Si Waze no esta instalado lo abre en google play
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
            startActivity(intent);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap map) {

        //  Si hay datos en el arreglo estaciones
         if (!estaciones.isEmpty()){
             // Obtener cada estacion y agregar su marcador al mapa
             for (int i = 0; i < estaciones.size(); i++)
             {
                 map.addMarker(estaciones.get(i).crearMarcador());
             }

             // Establecer como vista principal del mapa la primer estacion
             //  obtenida y centrar la camara a esa estacion
             LatLng zoom = estaciones.get(0).getPosicion();
             map.moveCamera(CameraUpdateFactory.newLatLngZoom(zoom, 15));
         }

         /*
         *  Listener que esta a la espera  que se quite la seleccion de una estacion
         *  para poder hacer invisible el fab navegar
          */
         map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
             @Override
             public void onMapClick(LatLng latLng) {
                 botonNavegar.setVisibility(View.INVISIBLE);
             }
         });


        /*
        *   Listener que se ejecuta al hacer clic en un marcador.
         */
         map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
             @Override
             public boolean onMarkerClick(Marker marker) {
                 // Hacer visible FAB navegar
                 botonNavegar.setVisibility(View.VISIBLE);
                 // Centrar en la pantalla el marcador
                 map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                 // Mostrar informacion del marcador y establecerlo como seleccionado
                 marker.showInfoWindow();
                 marcadorSeleccionado = marker;

                 return true;
             }
         });

    }

    /*
    *   Clase privada que nos permitira obtener los datos del web service de citybik
    *   en segundo plano. Una vez que se obtenga el Json como respuesta lo parseara
    *   y creara una arreglo con las cicloestaciones que tienen bicis disponibles.
     */
    private class GetData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //  Hacer peticion http hacia el ws, regresa la respuesta
            String stream = null;
            String urlString = params[0];
            HTTPDataHandler hh = new HTTPDataHandler();
            stream = hh.getHTTPData(urlString);
            return stream;
        }

        //  Cuando la peticicion fue exitosa se ejecuta este metodo
        //  pasando como parametro la respuesta del servidor
        @Override
        protected void onPostExecute(String s) {
            //  si la respuesta es diferente de null
            if(s != null){
                try {
                    //  crear objeto y obtener el json de network
                    JSONObject jsonObject = new JSONObject(s);
                    jsonObject = jsonObject.getJSONObject("network");

                    //  crear array de tipo json con las estaciones
                    JSONArray jsonArray = jsonObject.getJSONArray("stations");

                    //  para cada estacion obtenida
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        //  obtener los datos de interes
                        //  num de bicis disponibles, nombre de cicloestacion y ubicacion
                        JSONObject estacionJson = jsonArray.getJSONObject(i);
                        int free_bikes = estacionJson.getInt("free_bikes");
                        String name = estacionJson.getString("name");
                        double latitude = estacionJson.getDouble("latitude");
                        double longitude = estacionJson.getDouble("longitude");

                        //  si tiene bicis disponibles crea el objeto y lo agrega al arreglo
                        if (free_bikes > 0)
                        {
                            EstacionEcobici estacion = new EstacionEcobici
                                    (name,free_bikes,latitude,longitude);
                            estaciones.add(estacion);
                        }
                    }
                    //  notificar que ya se cargo el mapa y los datos exitosamente
                    //  para poder crear los marcadores
                    cargarMapa();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
