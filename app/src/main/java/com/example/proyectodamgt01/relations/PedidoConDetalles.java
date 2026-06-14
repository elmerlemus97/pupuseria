package com.example.proyectodamgt01.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.proyectodamgt01.entities.Cliente;
import com.example.proyectodamgt01.entities.Pedido;
import com.example.proyectodamgt01.entities.Usuario;

import java.util.List;

public class PedidoConDetalles {
    @Embedded
    public Pedido pedido;

    @Relation(
            parentColumn = "usuario_id",
            entityColumn = "id_usuario"
    )
    public Usuario usuario;

    @Relation(
            parentColumn = "cliente_id",
            entityColumn = "id_cliente"
    )
    public Cliente cliente;

    @Relation(
            entity = com.example.proyectodamgt01.entities.DetallePedido.class,
            parentColumn = "id_pedido",
            entityColumn = "pedido_id"
    )
    public List<DetalleConProducto> detalles;
}
