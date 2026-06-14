package com.example.proyectodamgt01.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.proyectodamgt01.dao.ClienteDao;
import com.example.proyectodamgt01.dao.DetallePedidoDao;
import com.example.proyectodamgt01.dao.PedidoDao;
import com.example.proyectodamgt01.dao.ProductoDao;
import com.example.proyectodamgt01.dao.UsuarioDao;
import com.example.proyectodamgt01.entities.Cliente;
import com.example.proyectodamgt01.entities.DetallePedido;
import com.example.proyectodamgt01.entities.Pedido;
import com.example.proyectodamgt01.entities.Producto;
import com.example.proyectodamgt01.entities.Usuario;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                Producto.class,
                Cliente.class,
                Usuario.class,
                Pedido.class,
                DetallePedido.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "pupuseria_db";
    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService DATABASE_EXECUTOR = Executors.newSingleThreadExecutor();

    public abstract ProductoDao productoDao();

    public abstract ClienteDao clienteDao();

    public abstract UsuarioDao usuarioDao();

    public abstract PedidoDao pedidoDao();

    public abstract DetallePedidoDao detallePedidoDao();

    public static AppDatabase getInstance(Context context) {
        // Patron singleton: evita crear varias conexiones a la misma base SQLite.
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addCallback(preloadCallback)
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    private static final RoomDatabase.Callback preloadCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            DATABASE_EXECUTOR.execute(() -> {
                if (INSTANCE == null) {
                    return;
                }

                // Datos iniciales del menu para que la tabla Producto no nazca vacia.
                ProductoDao productoDao = INSTANCE.productoDao();
                productoDao.insertar(new Producto("Queso", 0.50, 1));
                productoDao.insertar(new Producto("Frijol con queso", 0.50, 1));
                productoDao.insertar(new Producto("Revueltas", 0.60, 1));
                productoDao.insertar(new Producto("Loroco con queso", 0.75, 1));
                productoDao.insertar(new Producto("Chicharron", 0.65, 1));

                // Usuario inicial para pruebas: username admin, password admin, estado activo.
                INSTANCE.usuarioDao().insertar(new Usuario("admin", "admin", 1));
            });
        }
    };
}
