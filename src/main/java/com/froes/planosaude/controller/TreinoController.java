package com.froes.planosaude.controller;

import com.froes.planosaude.model.Treino;
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
@RequestMapping("/api/treinos")
@CrossOrigin(origins = "*")
@Tag(name = "Treinos", description = "Endpoints para gerenciamento de treinos")
public class TreinoController {

    private static final Logger logger = LoggerFactory.getLogger(TreinoController.class);

    @Autowired
    private PlanoService planoService;

    @GetMapping
    @Operation(summary = "Listar todos os treinos", description = "Retorna uma lista com todos os treinos cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de treinos retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao buscar treinos")
    })
    public ResponseEntity<List<Treino>> getTreinos() {
        try {
            List<Treino> treinos = planoService.getTreinos();
            logger.info("Encontrados {} treinos", treinos.size());
            return ResponseEntity.ok(treinos);
        } catch (Exception e) {
            logger.error("Erro ao buscar treinos: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/por-data")
    @Operation(summary = "Buscar treinos por intervalo de datas", description = "Retorna treinos entre as datas de início e fim especificadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treinos encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao buscar treinos por data")
    })
    public ResponseEntity<List<Treino>> getTreinosPorData(
            @Parameter(description = "Data de início no formato yyyy-MM-dd'T'HH:mm:ss", example = "2025-05-27T00:00:00")
            @RequestParam("start") String start,
            @Parameter(description = "Data de fim no formato yyyy-MM-dd'T'HH:mm:ss", example = "2025-05-28T00:00:00")
            @RequestParam("end") String end) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime startDate = LocalDateTime.parse(start, formatter);
            LocalDateTime endDate = LocalDateTime.parse(end, formatter);
            List<Treino> treinos = planoService.getTreinosPorData(startDate, endDate);
            logger.info("Treinos encontrados entre {} e {}: {}", startDate, endDate, treinos.size());
            return ResponseEntity.ok(treinos);
        } catch (Exception e) {
            logger.error("Erro ao buscar treinos por data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping
    @Operation(summary = "Adicionar um novo treino", description = "Cria um novo treino no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treino adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao adicionar treino"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Treino> adicionarTreino(@RequestBody Treino treino) {
        try {
            Treino salvo = planoService.adicionarTreino(treino);
            logger.info("Treino adicionado com sucesso: {}", salvo);
            return ResponseEntity.ok(salvo);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao adicionar treino: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro inesperado ao adicionar treino: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}