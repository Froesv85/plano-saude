package com.froes.planosaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título não pode exceder 255 caracteres")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "Data de início é obrigatória")
    @Column(nullable = false)
    private LocalDateTime start;

    @Column
    private LocalDateTime end;

    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    @Column
    private String description;

    @NotNull(message = "Usuário é obrigatório")
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    /**
     * Validates that end date is after start date, if provided.
     */
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (end != null && start != null && end.isBefore(start)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início.");
        }
    }
}