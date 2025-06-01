package com.froes.planosaude.repository;

import com.froes.planosaude.model.Usuario;
import com.froes.planosaude.model.UsuarioPeso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioPesoRepository extends JpaRepository<UsuarioPeso, Long> {
    List<UsuarioPeso> findByUsuarioOrderByDataRegistroDesc(Usuario usuario);

    // Adicionado: Buscar pesos por intervalo de data para um usuário específico
    List<UsuarioPeso> findByUsuarioAndDataRegistroBetweenOrderByDataRegistroDesc(
            Usuario usuario, java.time.LocalDateTime start, java.time.LocalDateTime end);
}