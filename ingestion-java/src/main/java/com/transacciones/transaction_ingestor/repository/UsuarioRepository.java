package com.transacciones.transaction_ingestor.repository;

import com.transacciones.transaction_ingestor.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}