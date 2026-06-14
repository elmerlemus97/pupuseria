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
    // Inserta un usuario. Los registros publicos nacen con estado 0: inactivo.
    @Insert
    long insertar(Usuario usuario);

    // Actualiza username, password o estado de un usuario.
    @Update
    void actualizar(Usuario usuario);

    @Delete
    void eliminar(Usuario usuario);

    @Query("SELECT * FROM Usuarios ORDER BY username ASC")
    List<Usuario> listarTodos();

    @Query("SELECT * FROM Usuarios WHERE id_usuario = :idUsuario LIMIT 1")
    Usuario buscarPorId(int idUsuario);

    // Login: solo devuelve usuario si credenciales coinciden y estado = 1.
    @Query("SELECT * FROM Usuarios WHERE username = :username AND password = :password AND estado = 1 LIMIT 1")
    Usuario login(String username, String password);

    // Activacion/aprobacion de acceso desde el CRUD de usuarios.
    @Query("UPDATE Usuarios SET estado = :estado WHERE id_usuario = :idUsuario")
    void cambiarEstado(int idUsuario, int estado);

    // Se usa para crear admin inicial si la tabla esta vacia.
    @Query("SELECT COUNT(*) FROM Usuarios")
    int contar();

    // Evita usernames duplicados.
    @Query("SELECT COUNT(*) FROM Usuarios WHERE username = :username")
    int contarPorUsername(String username);
}
