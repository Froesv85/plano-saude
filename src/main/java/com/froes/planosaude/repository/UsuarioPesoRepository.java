package com.froes.planosaude.repository;

import com.froes.planosaude.model.UsuarioPeso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioPesoRepository extends JpaRepository<UsuarioPeso, Long> {
    /**
     * Finds weight records by user ID, ordered by registration date descending.
     *
     * @param usuarioId User's ID
     * @return List of weight records
     */
    List<UsuarioPeso> findByUsuarioIdOrderByDataRegistroDesc(Long usuarioId);

    /**
     * Finds weight records by user ID within a date range, ordered by registration date descending.
     *
     * @param usuarioId User's ID
     * @param start     Start date and time
     * @param end       End date and time
     * @return List of weight records
     */
    List<UsuarioPeso> findByUsuarioIdAndDataRegistroBetweenOrderByDataRegistroDesc(Long usuarioId, LocalDateTime start, LocalDateTime end);

    /**
     * Finds a weight record by its ID and user ID to ensure ownership.
     *
     * @param id        Weight record ID
     * @param usuarioId User's ID
     * @return Optional containing the weight record if found
     */
    Optional<UsuarioPeso> findByIdAndUsuarioId(Long id, Long usuarioId);
}