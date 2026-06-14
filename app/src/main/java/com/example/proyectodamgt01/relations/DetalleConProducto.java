package com.example.proyectodamgt01.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.proyectodamgt01.entities.DetallePedido;
import com.example.proyectodamgt01.entities.Producto;

public class DetalleConProducto {
    @Embedded
    public DetallePedido detallePedido;

    @Relation(
            parentColumn = "producto_id",
            entityColumn = "id_producto"
    )
    public Producto producto;
}
