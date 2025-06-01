package com.froes.planosaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_peso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPeso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "O peso é obrigatório")
    private Double peso;

    @Column(nullable = false)
    @NotNull(message = "A data de registro é obrigatória")
    private LocalDateTime dataRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "O usuário é obrigatório")
    private Usuario usuario;

    public UsuarioPeso(Double peso) {
        this.peso = peso;
        this.dataRegistro = LocalDateTime.now();
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}