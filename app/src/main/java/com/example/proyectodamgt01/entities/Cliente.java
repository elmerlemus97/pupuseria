package com.example.proyectodamgt01.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Entity de Room: representa a los clientes que realizan pedidos.
@Entity(tableName = "Cliente")
public class Cliente {
    // Llave primaria autoincremental: id_cliente.
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cliente")
    public int idCliente;

    // Informacion basica del cliente para pedidos locales o delivery.
    public String nombre;
    public String telefono;
    public String direccion;

    public Cliente(String nombre, String telefono, String direccion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }
}
