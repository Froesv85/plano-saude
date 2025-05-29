package com.froes.planosaude.repository;

import com.froes.planosaude.model.Refeicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RefeicaoRepository extends JpaRepository<Refeicao, Long> {
    List<Refeicao> findByDataBetween(LocalDateTime start, LocalDateTime end);
}