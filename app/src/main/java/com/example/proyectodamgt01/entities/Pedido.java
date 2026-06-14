package com.example.proyectodamgt01.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Pedido",
        foreignKeys = {
                @ForeignKey(
                        entity = Usuario.class,
                        parentColumns = "id_usuario",
                        childColumns = "usuario_id",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = Cliente.class,
                        parentColumns = "id_cliente",
                        childColumns = "cliente_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index("usuario_id"),
                @Index("cliente_id")
        }
)
public class Pedido {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_pedido")
    public int idPedido;

    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

    @ColumnInfo(name = "cliente_id")
    public int clienteId;

    public String fecha;
    public String hora;
    public double total;

    // 0 = Pendiente, 1 = Entregado
    @ColumnInfo(name = "estado_entrega")
    public int estadoEntrega;

    // 0 = Local, 1 = Delivery
    @ColumnInfo(name = "modo_entrega")
    public int modoEntrega;

    public Pedido(int usuarioId, int clienteId, String fecha, String hora, double total, int estadoEntrega, int modoEntrega) {
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.hora = hora;
        this.total = total;
        this.estadoEntrega = estadoEntrega;
        this.modoEntrega = modoEntrega;
    }
}
