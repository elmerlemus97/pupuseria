package com.example.proyectodamgt01.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.proyectodamgt01.entities.Pedido;
import com.example.proyectodamgt01.relations.PedidoConDetalles;

import java.util.List;

@Dao
public interface PedidoDao {
    // Inserta cabecera del pedido y devuelve id_pedido.
    @Insert
    long insertar(Pedido pedido);

    // Actualiza cabecera al modificar un pedido pendiente.
    @Update
    void actualizar(Pedido pedido);

    @Delete
    void eliminar(Pedido pedido);

    @Query("SELECT * FROM Pedido ORDER BY id_pedido DESC")
    List<Pedido> listarTodos();

    @Query("SELECT * FROM Pedido WHERE estado_entrega = :estadoEntrega ORDER BY id_pedido DESC")
    List<Pedido> listarPorEstadoEntrega(int estadoEntrega);

    @Query("SELECT * FROM Pedido WHERE id_pedido = :idPedido LIMIT 1")
    Pedido buscarPorId(int idPedido);

    // Consulta un pedido con usuario, cliente y detalles.
    @Transaction
    @Query("SELECT * FROM Pedido WHERE id_pedido = :idPedido LIMIT 1")
    PedidoConDetalles obtenerPedidoConDetalles(int idPedido);

    // Lista pendientes o entregados con sus relaciones para pintar tarjetas.
    @Transaction
    @Query("SELECT * FROM Pedido WHERE estado_entrega = :estadoEntrega ORDER BY id_pedido DESC")
    List<PedidoConDetalles> listarConDetallesPorEstado(int estadoEntrega);

    @Query("UPDATE Pedido SET total = :total WHERE id_pedido = :idPedido")
    void actualizarTotal(int idPedido, double total);

    @Query("UPDATE Pedido SET estado_entrega = :estadoEntrega WHERE id_pedido = :idPedido")
    void cambiarEstadoEntrega(int idPedido, int estadoEntrega);

    // Al borrar cabecera, DetallePedido se borra por CASCADE.
    @Query("DELETE FROM Pedido WHERE id_pedido = :idPedido")
    void eliminarPorId(int idPedido);
}
