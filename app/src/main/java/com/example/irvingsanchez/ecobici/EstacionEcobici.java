package com.example.irvingsanchez.ecobici;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by irvingsanchez on 10/02/18.
 *
 * Clase que modela el objeto obtenido del web service
 * con los datos que se necesitan
 */

public class EstacionEcobici {

    private String nombre;
    private int free_bikes;
    private LatLng posicion;

    public EstacionEcobici(String nombre, int free_bikes, double latitude, double longitude) {
        this.nombre = nombre;
        this.free_bikes = free_bikes;
        posicion = new LatLng(latitude,longitude);

    }

    //  Getter para posicion del marcador
    public LatLng getPosicion() {
        return posicion;
    }

    // Metodo que crea y devuelve un marcador con los datos de la estacion
    public MarkerOptions crearMarcador(){
        return  new MarkerOptions()
                .title(nombre)
                .snippet("Aquí hay " + String.valueOf(free_bikes) + " bicicletas disponibles")
                .position(posicion);
    }
}
