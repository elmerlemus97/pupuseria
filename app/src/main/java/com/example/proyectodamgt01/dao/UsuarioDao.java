package com.example.proyectodamgt01.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.proyectodamgt01.entities.Usuario;

import java.util.List;

@Dao
public interface UsuarioDao {
    @Insert
    long insertar(Usuario usuario);

    @Update
    void actualizar(Usuario usuario);

    @Delete
    void eliminar(Usuario usuario);

    @Query("SELECT * FROM Usuarios ORDER BY username ASC")
    List<Usuario> listarTodos();

    @Query("SELECT * FROM Usuarios WHERE id_usuario = :idUsuario LIMIT 1")
    Usuario buscarPorId(int idUsuario);

    @Query("SELECT * FROM Usuarios WHERE username = :username AND password = :password AND estado = 1 LIMIT 1")
    Usuario login(String username, String password);

    @Query("UPDATE Usuarios SET estado = :estado WHERE id_usuario = :idUsuario")
    void cambiarEstado(int idUsuario, int estado);

    @Query("SELECT COUNT(*) FROM Usuarios")
    int contar();

    @Query("SELECT COUNT(*) FROM Usuarios WHERE username = :username")
    int contarPorUsername(String username);
}
