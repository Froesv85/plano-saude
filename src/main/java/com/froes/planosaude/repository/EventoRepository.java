package com.froes.planosaude.repository;

import com.froes.planosaude.model.Evento;
import com.froes.planosaude.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    // Removido: findByStartBetween (sem filtro de usuário, inseguro)
    // Mantido: Métodos filtrados por usuário
    List<Evento> findByUsuarioAndStartBetween(Usuario usuario, LocalDateTime start, LocalDateTime end);

    ///List<Evento> findByUsuarioAndStartBetweenOrderByStartDesc(Usuario usuario, LocalDateTime start, LocalDateTime end);

    // Adicionado: Buscar eventos de hoje para um usuário
    List<Evento> findByUsuarioAndStartBetweenOrderByStartDesc(
            Usuario usuario, LocalDateTime startOfDay, LocalDateTime endOfDay);
}