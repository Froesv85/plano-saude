package com.froes.planosaude.controller;

import com.froes.planosaude.model.Refeicao;
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
@RequestMapping("/api/refeicoes")
@CrossOrigin(origins = "*")
@Tag(name = "Refeições", description = "Endpoints para gerenciamento de refeições")
public class RefeicaoController {

    private static final Logger logger = LoggerFactory.getLogger(RefeicaoController.class);

    @Autowired
    private PlanoService planoService;

    @GetMapping
    @Operation(summary = "Listar todas as refeições", description = "Retorna uma lista com todas as refeições cadastradas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de refeições retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao buscar refeições")
    })
    public ResponseEntity<List<Refeicao>> getRefeicoes() {
        try {
            List<Refeicao> refeicoes = planoService.getRefeicoes();
            logger.info("Encontradas {} refeições", refeicoes.size());
            return ResponseEntity.ok(refeicoes);
        } catch (Exception e) {
            logger.error("Erro ao buscar refeições: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/por-data")
    @Operation(summary = "Buscar refeições por intervalo de datas", description = "Retorna refeições entre as datas de início e fim especificadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refeições encontradas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao buscar refeições por data")
    })
    public ResponseEntity<List<Refeicao>> getRefeicoesPorData(
            @Parameter(description = "Data de início no formato yyyy-MM-dd'T'HH:mm:ss", example = "2025-05-27T00:00:00")
            @RequestParam("start") String start,
            @Parameter(description = "Data de fim no formato yyyy-MM-dd'T'HH:mm:ss", example = "2025-05-28T00:00:00")
            @RequestParam("end") String end) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime startDate = LocalDateTime.parse(start, formatter);
            LocalDateTime endDate = LocalDateTime.parse(end, formatter);
            List<Refeicao> refeicoes = planoService.getRefeicoesPorData(startDate, endDate);
            logger.info("Refeições encontradas entre {} e {}: {}", startDate, endDate, refeicoes.size());
            return ResponseEntity.ok(refeicoes);
        } catch (Exception e) {
            logger.error("Erro ao buscar refeições por data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping
    @Operation(summary = "Adicionar uma nova refeição", description = "Cria uma nova refeição no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refeição adicionada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao adicionar refeição"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Refeicao> adicionarRefeicao(@RequestBody Refeicao refeicao) {
        try {
            Refeicao salvo = planoService.adicionarRefeicao(refeicao);
            logger.info("Refeição adicionada com sucesso: {}", salvo);
            return ResponseEntity.ok(salvo);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao adicionar refeição: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro inesperado ao adicionar refeição: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}