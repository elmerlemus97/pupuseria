package com.example.proyectodamgt01.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.proyectodamgt01.entities.Producto;

import java.util.List;

@Dao
public interface ProductoDao {
    // Inserta una pupusa/producto nuevo y devuelve su id generado.
    @Insert
    long insertar(Producto producto);

    // Actualiza nombre, precio o estado de un producto existente.
    @Update
    void actualizar(Producto producto);

    // Elimina un producto si no esta protegido por relaciones de pedidos.
    @Delete
    void eliminar(Producto producto);

    // Lista todo el catalogo, activos e inactivos, para el CRUD.
    @Query("SELECT * FROM Producto ORDER BY nombre ASC")
    List<Producto> listarTodos();

    // Lista solo productos activos para la pantalla Ordenar.
    @Query("SELECT * FROM Producto WHERE estado = 1 ORDER BY nombre ASC")
    List<Producto> listarActivos();

    @Query("SELECT * FROM Producto WHERE id_producto = :idProducto LIMIT 1")
    Producto buscarPorId(int idProducto);

    @Query("UPDATE Producto SET estado = :estado WHERE id_producto = :idProducto")
    void cambiarEstado(int idProducto, int estado);

    // Se usa para saber si hay que precargar pupusas iniciales.
    @Query("SELECT COUNT(*) FROM Producto")
    int contar();
}
