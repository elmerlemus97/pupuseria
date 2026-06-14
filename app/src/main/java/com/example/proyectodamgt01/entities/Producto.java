package com.example.proyectodamgt01.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Producto")
public class Producto {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_producto")
    public int idProducto;

    public String nombre;
    public double precio;

    // 1 = Activo, 0 = Inactivo
    public int estado;

    public Producto(String nombre, double precio, int estado) {
        this.nombre = nombre;
        this.precio = precio;
        this.estado = estado;
    }
}
