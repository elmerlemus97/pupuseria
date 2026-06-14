package com.example.proyectodamgt01.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Usuarios")
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    public int idUsuario;

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
