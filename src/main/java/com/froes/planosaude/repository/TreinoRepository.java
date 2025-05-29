package com.froes.planosaude.repository;

import com.froes.planosaude.model.Treino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TreinoRepository extends JpaRepository<Treino, Long> {
    List<Treino> findByDataBetween(LocalDateTime start, LocalDateTime end);
}