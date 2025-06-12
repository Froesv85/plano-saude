package com.froes.planosaude.repository;

import com.froes.planosaude.model.Treino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Treino} entities.
 */
@Repository
public interface TreinoRepository extends JpaRepository<Treino, Long> {

    /**
     * Finds all workouts for a given user.
     *
     * @param long1 the ID of the user
     * @return a list of {@link Treino} entities associated with the user
     */
    List<Treino> findByUsuarioId(Long long1);

    /**
     * Finds workouts for a given user on a specific date (ignoring time).
     *
     * @param usuarioId the ID of the user
     * @param data      the date to filter workouts
     * @return a list of {@link Treino} entities for the user on the specified date
     */
    @Query("SELECT t FROM Treino t WHERE t.usuario.id = :usuarioId AND FUNCTION('DATE', t.data) = :data")
    List<Treino> findByUsuarioIdAndData(@Param("usuarioId") Long usuarioId, @Param("data") LocalDate data);

    /**
     * Finds workouts for a given user within a date range, ordered by date descending.
     *
     * @param usuarioId the ID of the user
     * @param start     the start of the date range (inclusive)
     * @param end       the end of the date range (inclusive)
     * @return a list of {@link Treino} entities within the date range, ordered by data descending
     */
    @Query("SELECT t FROM Treino t WHERE t.usuario.id = :usuarioId AND t.data BETWEEN :start AND :end ORDER BY t.data DESC")
    List<Treino> findByUsuarioIdAndDataBetweenOrderByDataDesc(@Param("usuarioId") Long usuarioId,
                                                              @Param("start") LocalDateTime start,
                                                              @Param("end") LocalDateTime end);

    /**
     * Finds a workout by its ID and user ID to ensure ownership.
     *
     * @param id        the ID of the workout
     * @param usuarioId the ID of the user
     * @return an {@link Optional} containing the {@link Treino} if found, or empty if not
     */
    Optional<Treino> findByIdAndUsuarioId(Long id, Long usuarioId);
}