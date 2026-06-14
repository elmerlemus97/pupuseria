package com.example.proyectodamgt01.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Entity de Room: representa la tabla Producto en SQLite.
@Entity(tableName = "Producto")
public class Producto {
    // Llave primaria autoincremental: id_producto.
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_producto")
    public int idProducto;

    // Datos principales del producto que se vende en la pupuseria.
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
