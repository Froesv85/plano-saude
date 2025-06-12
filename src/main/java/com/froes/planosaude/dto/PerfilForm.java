package com.froes.planosaude.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PerfilForm {

    @NotNull(message = "Peso é obrigatório")
    @DecimalMin(value = "30.0", message = "Peso deve ser no mínimo 30 kg")
    @DecimalMax(value = "300.0", message = "Peso deve ser no máximo 300 kg")
    private Double peso;

    @NotNull(message = "Altura é obrigatória")
    @DecimalMin(value = "1.0", message = "Altura deve ser no mínimo 1,0 m")
    @DecimalMax(value = "2.5", message = "Altura deve ser no máximo 2,5 m")
    private Double altura;

    @NotNull(message = "Idade é obrigatória")
    @Min(value = 10, message = "Idade deve ser no mínimo 10 anos")
    @Max(value = 120, message = "Idade deve ser no máximo 120 anos")
    private Integer idade;

    // Getters and Setters
    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }
}