package com.froes.planosaude.repository;

import com.froes.planosaude.model.PlanoSaude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanoSaudeRepository extends JpaRepository<PlanoSaude, Long> {
    /**
     * Finds health plans by user ID and plan type.
     *
     * @param usuarioId User's ID
     * @param tipo      Plan type (e.g., TREINO, DIETA)
     * @return List of matching health plans
     */
    List<PlanoSaude> findByUsuarioIdAndTipo(Long usuarioId, PlanoSaude.TipoPlano tipo);

    /**
     * Finds health plans by user ID and active status.
     *
     * @param usuarioId User's ID
     * @param ativo     Active status of the plan
     * @return List of matching health plans
     */
    List<PlanoSaude> findByUsuarioIdAndAtivo(Long usuarioId, boolean ativo);

    /**
     * Finds a health plan by its ID and user ID to ensure ownership.
     *
     * @param id        Plan ID
     * @param usuarioId User's ID
     * @return Optional containing the health plan if found
     */
    Optional<PlanoSaude> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Finds all health plans by user ID.
     *
     * @param usuarioId User's ID
     * @return List of all health plans for the user
     */
    List<PlanoSaude> findByUsuarioId(Long usuarioId);
}