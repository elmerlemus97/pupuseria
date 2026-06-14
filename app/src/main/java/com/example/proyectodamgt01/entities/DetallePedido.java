package com.example.proyectodamgt01.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Entity de Room: lineas de detalle de cada pedido.
// Aqui se guardan los productos seleccionados, su cantidad y subtotal.
@Entity(
        tableName = "DetallePedido",
        foreignKeys = {
                @ForeignKey(
                        entity = Pedido.class,
                        parentColumns = "id_pedido",
                        childColumns = "pedido_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Producto.class,
                        parentColumns = "id_producto",
                        childColumns = "producto_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index("pedido_id"),
                @Index("producto_id")
        }
)
public class DetallePedido {
    // Llave primaria autoincremental: id_detalle.
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_detalle")
    public int idDetalle;

    // FK hacia Pedido: varios detalles pertenecen a una misma cabecera.
    @ColumnInfo(name = "pedido_id")
    public int pedidoId;

    // FK hacia Producto: producto vendido en esta linea.
    @ColumnInfo(name = "producto_id")
    public int productoId;

    public int cantidad;

    @ColumnInfo(name = "precio_unitario")
    public double precioUnitario;

    public double subtotal;

    public DetallePedido(int pedidoId, int productoId, int cantidad, double precioUnitario, double subtotal) {
        this.pedidoId = pedidoId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }
}
