package com.example.proyectodamgt01.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.proyectodamgt01.entities.DetallePedido;

import java.util.List;

@Dao
public interface DetallePedidoDao {
    // Inserta una linea del pedido.
    @Insert
    long insertar(DetallePedido detallePedido);

    @Update
    void actualizar(DetallePedido detallePedido);

    @Delete
    void eliminar(DetallePedido detallePedido);

    @Query("SELECT * FROM DetallePedido WHERE pedido_id = :pedidoId")
    List<DetallePedido> listarPorPedido(int pedidoId);

    // Suma subtotales de un pedido. Util para recalcular total si hace falta.
    @Query("SELECT SUM(subtotal) FROM DetallePedido WHERE pedido_id = :pedidoId")
    double calcularTotalPedido(int pedidoId);

    // Se usa al modificar un pedido: borrar detalles viejos y guardar los nuevos.
    @Query("DELETE FROM DetallePedido WHERE pedido_id = :pedidoId")
    void eliminarPorPedido(int pedidoId);
}
