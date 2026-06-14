package com.example.proyectodamgt01.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.proyectodamgt01.entities.Cliente;
import com.example.proyectodamgt01.entities.Pedido;
import com.example.proyectodamgt01.entities.Usuario;

import java.util.List;

// Relacion compuesta para pintar una tarjeta completa de pedido.
public class PedidoConDetalles {
    // Cabecera del pedido: fecha, hora, total, estado, modo, etc.
    @Embedded
    public Pedido pedido;

    // Usuario vendedor que tomo el pedido.
    @Relation(
            parentColumn = "usuario_id",
            entityColumn = "id_usuario"
    )
    public Usuario usuario;

    // Cliente del pedido.
    @Relation(
            parentColumn = "cliente_id",
            entityColumn = "id_cliente"
    )
    public Cliente cliente;

    // Lineas del pedido; cada linea incluye ademas su producto.
    @Relation(
            entity = com.example.proyectodamgt01.entities.DetallePedido.class,
            parentColumn = "id_pedido",
            entityColumn = "pedido_id"
    )
    public List<DetalleConProducto> detalles;
}
