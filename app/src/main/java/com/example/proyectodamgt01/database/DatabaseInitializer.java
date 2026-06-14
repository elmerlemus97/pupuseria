package com.example.proyectodamgt01.database;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private DatabaseInitializer() {
    }

    public static void initialize(Context context) {
        Context appContext = context.getApplicationContext();

        EXECUTOR.execute(() -> {
            // Esta consulta obliga a Room a abrir/crear SQLite sin bloquear la pantalla.
            AppDatabase.getInstance(appContext).productoDao().contar();
        });
    }
}
