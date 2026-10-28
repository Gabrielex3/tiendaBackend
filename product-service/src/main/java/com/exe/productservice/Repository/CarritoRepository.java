package com.exe.productservice.Repository;

import com.exe.productservice.Model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioId(Long usuarioId);

    Optional<Carrito> findBySessionId(String sessionId);
}
