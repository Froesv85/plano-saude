package com.froes.planosaude.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_peso")
public class UsuarioPeso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double peso;

    @Column(nullable = false)
    private LocalDateTime dataRegistro;

    // Construtores
    public UsuarioPeso() {
        this.dataRegistro = LocalDateTime.now();
    }

    public UsuarioPeso(Double peso) {
        this.peso = peso;
        this.dataRegistro = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    @Override
    public String toString() {
        return "UsuarioPeso{" +
                "id=" + id +
                ", peso=" + peso +
                ", dataRegistro=" + dataRegistro +
                '}';
    }
}