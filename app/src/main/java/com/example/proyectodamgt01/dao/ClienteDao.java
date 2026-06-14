package com.example.proyectodamgt01.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.proyectodamgt01.entities.Cliente;

import java.util.List;

@Dao
public interface ClienteDao {
    // Inserta cliente nuevo al confirmar un pedido.
    @Insert
    long insertar(Cliente cliente);

    // Actualiza datos del cliente si ya existia por telefono.
    @Update
    void actualizar(Cliente cliente);

    @Delete
    void eliminar(Cliente cliente);

    @Query("SELECT * FROM Cliente ORDER BY nombre ASC")
    List<Cliente> listarTodos();

    @Query("SELECT * FROM Cliente WHERE id_cliente = :idCliente LIMIT 1")
    Cliente buscarPorId(int idCliente);

    // El telefono se usa como forma simple de reutilizar clientes existentes.
    @Query("SELECT * FROM Cliente WHERE telefono = :telefono LIMIT 1")
    Cliente buscarPorTelefono(String telefono);
}
