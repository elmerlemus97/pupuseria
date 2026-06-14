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
    @Insert
    long insertar(Producto producto);

    @Update
    void actualizar(Producto producto);

    @Delete
    void eliminar(Producto producto);

    @Query("SELECT * FROM Producto ORDER BY nombre ASC")
    List<Producto> listarTodos();

    @Query("SELECT * FROM Producto WHERE estado = 1 ORDER BY nombre ASC")
    List<Producto> listarActivos();

    @Query("SELECT * FROM Producto WHERE id_producto = :idProducto LIMIT 1")
    Producto buscarPorId(int idProducto);

    @Query("UPDATE Producto SET estado = :estado WHERE id_producto = :idProducto")
    void cambiarEstado(int idProducto, int estado);

    @Query("SELECT COUNT(*) FROM Producto")
    int contar();
}
