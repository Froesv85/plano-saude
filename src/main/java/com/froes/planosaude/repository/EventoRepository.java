package com.froes.planosaude.repository;

import com.froes.planosaude.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    /**
     * Finds events by user ID within a start date range, ordered by start date descending.
     *
     * @param usuarioId User's ID
     * @param start     Start date and time
     * @param end       End date and time
     * @return List of matching events
     */
    List<Evento> findByUsuarioIdAndStartBetweenOrderByStartDesc(Long usuarioId, LocalDateTime start, LocalDateTime end);

    /**
     * Finds events by user ID for a specific date (ignoring time), ordered by start date.
     *
     * @param usuarioId User's ID
     * @param date      Date to filter events
     * @return List of matching events
     */
    @Query("SELECT e FROM Evento e WHERE e.usuario.id = :usuarioId AND DATE(e.start) = :date ORDER BY e.start")
    List<Evento> findByUsuarioIdAndStartDate(@Param("usuarioId") Long usuarioId, @Param("date") LocalDate date);

    /**
     * Finds an event by its ID and user ID to ensure ownership.
     *
     * @param id        Event ID
     * @param usuarioId User's ID
     * @return Optional containing the event if found
     */
    Optional<Evento> findByIdAndUsuarioId(Long id, Long usuarioId);
}