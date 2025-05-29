package com.froes.planosaude.controller;

import com.froes.planosaude.model.Evento;
import com.froes.planosaude.service.PlanoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
@Tag(name = "Eventos", description = "Endpoints para gerenciamento de eventos")
public class EventoController {

    private static final Logger logger = LoggerFactory.getLogger(EventoController.class);

    @Autowired
    private PlanoService planoService;

    @GetMapping
    @Operation(summary = "Listar todos os eventos", description = "Retorna uma lista com todos os eventos cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao buscar eventos")
    })
    public ResponseEntity<List<Evento>> getEventos() {
        try {
            List<Evento> eventos = planoService.getEventos();
            logger.info("Encontrados {} eventos", eventos.size());
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            logger.error("Erro ao buscar eventos: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/por-data")
    @Operation(summary = "Buscar eventos por intervalo de datas", description = "Retorna eventos entre as datas de início e fim especificadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventos encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao buscar eventos por data")
    })
    public ResponseEntity<List<Evento>> getEventosPorData(
            @Parameter(description = "Data de início no formato yyyy-MM-dd'T'HH:mm:ss", example = "2025-05-27T00:00:00")
            @RequestParam("start") String start,
            @Parameter(description = "Data de fim no formato yyyy-MM-dd'T'HH:mm:ss", example = "2025-05-28T00:00:00")
            @RequestParam("end") String end) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime startDate = LocalDateTime.parse(start, formatter);
            LocalDateTime endDate = LocalDateTime.parse(end, formatter);

            List<Evento> eventos = planoService.getEventosPorData(startDate, endDate);
            logger.info("Eventos encontrados entre {} e {}: {}", startDate, endDate, eventos.size());
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            logger.error("Erro ao buscar eventos por data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping
    @Operation(summary = "Adicionar um novo evento", description = "Cria um novo evento no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao adicionar evento"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Evento> adicionarEvento(@RequestBody Evento evento) {
        try {
            Evento salvo = planoService.adicionarEvento(evento);
            logger.info("Evento adicionado com sucesso: {}", salvo);
            return ResponseEntity.ok(salvo);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao adicionar evento: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro inesperado ao adicionar evento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar um evento existente", description = "Atualiza um evento com base no ID fornecido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento editado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao editar evento"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Evento> editarEvento(@PathVariable Long id, @RequestBody Evento evento) {
        try {
            evento.setId(id);
            Evento salvo = planoService.editarEvento(evento);
            logger.info("Evento editado com sucesso: {}", salvo);
            return ResponseEntity.ok(salvo);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao editar evento: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro inesperado ao editar evento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um evento", description = "Remove um evento com base no ID fornecido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao remover evento"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> removerEvento(@PathVariable Long id) {
        try {
            planoService.removerEvento(id);
            logger.info("Evento removido com sucesso: ID {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao remover evento: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover evento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}