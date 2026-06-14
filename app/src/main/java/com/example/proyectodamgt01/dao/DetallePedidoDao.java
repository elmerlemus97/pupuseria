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
    @Insert
    long insertar(DetallePedido detallePedido);

    @Update
    void actualizar(DetallePedido detallePedido);

    @Delete
    void eliminar(DetallePedido detallePedido);

    @Query("SELECT * FROM DetallePedido WHERE pedido_id = :pedidoId")
    List<DetallePedido> listarPorPedido(int pedidoId);

    @Query("SELECT SUM(subtotal) FROM DetallePedido WHERE pedido_id = :pedidoId")
    double calcularTotalPedido(int pedidoId);

    @Query("DELETE FROM DetallePedido WHERE pedido_id = :pedidoId")
    void eliminarPorPedido(int pedidoId);
}
