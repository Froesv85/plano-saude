package com.froes.planosaude.repository;

import com.froes.planosaude.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByStartBetween(LocalDateTime start, LocalDateTime end);
}