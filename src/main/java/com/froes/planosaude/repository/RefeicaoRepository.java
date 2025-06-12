package com.froes.planosaude.repository;

import com.froes.planosaude.model.Refeicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefeicaoRepository extends JpaRepository<Refeicao, Long> {
    /**
     * Finds meals by user ID within a date range, ordered by date descending.
     *
     * @param usuarioId User's ID
     * @param start     Start date and time
     * @param end       End date and time
     * @return List of matching meals
     */
    List<Refeicao> findByUsuarioIdAndDataBetweenOrderByDataDesc(Long usuarioId, LocalDateTime start, LocalDateTime end);

    /**
     * Finds meals by user ID for a specific date (ignoring time), ordered by date.
     *
     * @param usuarioId User's ID
     * @param date      Date to filter meals
     * @return List of matching meals
     */
    @Query("SELECT r FROM Refeicao r WHERE r.usuario.id = :usuarioId AND DATE(r.data) = :date ORDER BY r.data")
    List<Refeicao> findByUsuarioIdAndData(@Param("usuarioId") Long usuarioId, @Param("date") LocalDate date);

    /**
     * Finds a meal by its ID and user ID to ensure ownership.
     *
     * @param id        Meal ID
     * @param usuarioId User's ID
     * @return Optional containing the meal if found
     */
    Optional<Refeicao> findByIdAndUsuarioId(Long id, Long usuarioId);
}