package com.ignacio.godrive.includes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.ignacio.godrive.R;

// Clase de utilidad para mostrar una barra de herramientas personalizada en actividades.
public class MyToolBar {

    // Método estático para mostrar una barra de herramientas personalizada.
    // Recibe como parámetros la actividad (AppCompatActivity), un título y un indicador booleano para mostrar el botón de navegación hacia atrás.
    public static void show(AppCompatActivity activity, String titulo, boolean upButton){
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        // Verificar si la barra de herramientas es nula antes de intentar establecer el título.
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(titulo);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        }
    }
}
