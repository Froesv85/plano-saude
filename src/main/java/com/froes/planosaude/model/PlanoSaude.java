package com.froes.planosaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plano_saude")
@Getter
@Setter
@NoArgsConstructor
public class PlanoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "Usuário é obrigatório")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Tipo é obrigatório")
    private TipoPlano tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @Column(name = "data_inicio", nullable = false)
    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "meta_semanal", columnDefinition = "TEXT")
    private String metaSemanal;

    @Column(nullable = false)
    private boolean ativo = true;

    public enum TipoPlano {
        TREINO, DIETA
    }
}