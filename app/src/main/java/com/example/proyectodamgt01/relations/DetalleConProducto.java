package com.example.proyectodamgt01.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.proyectodamgt01.entities.DetallePedido;
import com.example.proyectodamgt01.entities.Producto;

// Relacion para mostrar un detalle junto con los datos del producto vendido.
public class DetalleConProducto {
    // Datos propios de la linea: cantidad, precio_unitario y subtotal.
    @Embedded
    public DetallePedido detallePedido;

    // Producto asociado al detalle por producto_id -> id_producto.
    @Relation(
            parentColumn = "producto_id",
            entityColumn = "id_producto"
    )
    public Producto producto;
}
