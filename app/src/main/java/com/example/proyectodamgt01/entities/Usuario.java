package com.example.proyectodamgt01.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Entity de Room: representa usuarios que pueden ingresar a la app.
@Entity(tableName = "Usuarios")
public class Usuario {
    // Llave primaria autoincremental: id_usuario.
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    public int idUsuario;

    // Credenciales sencillas para el proyecto. En produccion se deberia cifrar password.
    public String username;
    public String password;

    // 1 = Activo, 0 = Inactivo
    public int estado;

    public Usuario(String username, String password, int estado) {
        this.username = username;
        this.password = password;
        this.estado = estado;
    }
}
