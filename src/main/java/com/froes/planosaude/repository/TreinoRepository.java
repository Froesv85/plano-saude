package com.froes.planosaude.repository;

import com.froes.planosaude.model.Treino;
import com.froes.planosaude.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TreinoRepository extends JpaRepository<Treino, Long> {
    // Removido: findByDataBetween (sem filtro de usuário, inseguro)
    // Mantido: Métodos filtrados por usuário
    List<Treino> findByUsuarioAndDataBetween(Usuario usuario, LocalDateTime start, LocalDateTime end);

   // List<Treino> findByUsuarioAndDataBetweenOrderByDataDesc(Usuario usuario, LocalDateTime start, LocalDateTime end);

    // Adicionado: Buscar treinos de hoje para um usuário
    List<Treino> findByUsuarioAndDataBetweenOrderByDataDesc(
            Usuario usuario, LocalDateTime startOfDay, LocalDateTime endOfDay);
}