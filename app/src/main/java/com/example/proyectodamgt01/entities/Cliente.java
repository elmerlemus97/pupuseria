package com.example.proyectodamgt01.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Cliente")
public class Cliente {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cliente")
    public int idCliente;

    public String nombre;
    public String telefono;
    public String direccion;

    public Cliente(String nombre, String telefono, String direccion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }
}
