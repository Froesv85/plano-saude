package com.froes.planosaude.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "start_date")
    private LocalDateTime start;

    @Column(name = "end_date")
    private LocalDateTime end;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

	@SuppressWarnings("unused")
	private String description;

	    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
	public void setDescription(String description) {this.description =description;}
}