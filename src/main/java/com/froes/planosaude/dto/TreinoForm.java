package com.froes.planosaude.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TreinoForm {

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotNull(message = "Data e hora são obrigatórias")
    private LocalDateTime data;

    // Getters and Setters
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
}
