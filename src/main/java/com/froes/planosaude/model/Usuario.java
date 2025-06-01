package com.froes.planosaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório e não pode estar em branco")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório e não pode estar em branco")
    @Email(message = "O e-mail deve ser válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "A senha é obrigatória e não pode estar em branco")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    @Column(nullable = false)
    private String senha; // Criptografada com BCryptPasswordEncoder

    @Positive(message = "O peso deve ser um valor positivo")
    @Column(nullable = true)
    private Double peso;

    @Positive(message = "A altura deve ser um valor positivo")
    @Column(nullable = true)
    private Double altura;

    @Min(value = 0, message = "A idade deve ser maior ou igual a 0")
    @Column(nullable = true)
    private Integer idade;
}