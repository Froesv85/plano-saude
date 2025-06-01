package com.froes.planosaude.repository;

import com.froes.planosaude.model.Refeicao;
import com.froes.planosaude.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RefeicaoRepository extends JpaRepository<Refeicao, Long> {
    // Removido: findByDataBetween (sem filtro de usuário, inseguro)
    // Mantido: Métodos filtrados por usuário
    List<Refeicao> findByUsuarioAndDataBetween(Usuario usuario, LocalDateTime start, LocalDateTime end);

   //// List<Refeicao> findByUsuarioAndDataBetweenOrderByDataDesc(Usuario usuario, LocalDateTime start, LocalDateTime end);

    // Adicionado: Buscar refeições de hoje para um usuário
    List<Refeicao> findByUsuarioAndDataBetweenOrderByDataDesc(
            Usuario usuario, LocalDateTime startOfDay, LocalDateTime endOfDay);
}