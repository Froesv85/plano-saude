package com.froes.planosaude.controller;

import com.froes.planosaude.model.Treino;
import com.froes.planosaude.model.Usuario;
import com.froes.planosaude.service.PlanoService;
import com.froes.planosaude.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/treinos")
@CrossOrigin(origins = "*")
@Tag(name = "Treinos", description = "Endpoints para gerenciamento de treinos")
public class TreinoController {

    private static final Logger logger = LoggerFactory.getLogger(TreinoController.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private PlanoService planoService;

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Listar todos os treinos do usuário", description = "Retorna uma lista com todos os treinos do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de treinos retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao buscar treinos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<?> getTreinos(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            List<Treino> treinos = planoService.getTreinosByUsuario(usuario.getId());
            logger.info("Encontrados {} treinos para usuário ID: {}", treinos.size(), usuario.getId());
            return ResponseEntity.ok(treinos);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao buscar treinos: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar treinos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Buscar treinos por intervalo de datas", description = "Retorna treinos do usuário autenticado entre as datas de início e fim especificadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treinos encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato de data inválido ou erro de validação"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/por-data")
    public ResponseEntity<?> getTreinosPorData(
            Authentication authentication,
            @Parameter(description = "Data de início (ISO 8601, ex.: 2025-06-01T00:00:00)", required = true)
            @RequestParam String start,
            @Parameter(description = "Data de fim (ISO 8601, ex.: 2025-06-02T00:00:00)", required = true)
            @RequestParam String end) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            LocalDateTime startDate = LocalDateTime.parse(start, formatter);
            LocalDateTime endDate = LocalDateTime.parse(end, formatter);
            List<Treino> treinos = planoService.getTreinosByUsuarioAndDataRange(usuario.getId(), startDate, endDate);
            logger.info("Encontrados {} treinos para usuário ID {} entre {} e {}", treinos.size(), usuario.getId(), startDate, endDate);
            return ResponseEntity.ok(treinos);
        } catch (DateTimeParseException e) {
            logger.error("Erro ao parsear datas: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de data inválido"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao buscar treinos: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar treinos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Adicionar um novo treino", description = "Cria um novo treino para o usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treino adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou formato de data incorreto"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<?> adicionarTreino(
            @Parameter(description = "Dados do treino (descricao, data)", required = true)
            @RequestBody Map<String, Object> treinoData,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));

            String descricao = (String) treinoData.get("descricao");
            if (descricao == null || descricao.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Descrição é obrigatória"));
            }
            LocalDateTime data = LocalDateTime.parse((String) treinoData.get("data"), formatter);

            Treino treino = new Treino();
            treino.setUsuario(usuario);
            treino.setDescricao(descricao);
            treino.setData(data);
            planoService.salvarTreino(treino);
            logger.info("Treino adicionado para usuário ID {}: {}", usuario.getId(), descricao);
            return ResponseEntity.ok(Map.of());
        } catch (DateTimeParseException e) {
            logger.error("Erro ao parsear data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de data inválido"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao adicionar treino: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao adicionar treino: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Editar um treino existente", description = "Atualiza um treino do usuário autenticado com base no ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treino editado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Treino não pertence ao usuário"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> editarTreino(
            @Parameter(description = "ID do treino a ser editado", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados do treino", required = true)
            @RequestBody Treino treino,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            treino.setId(id);
            treino.setUsuario(usuario);
            Treino salvo = planoService.salvarTreino(treino);
            logger.info("Treino editado para usuário ID {}: ID {}", usuario.getId(), id);
            return ResponseEntity.ok(salvo);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao editar treino: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao editar treino: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Remover um treino", description = "Remove um treino do usuário autenticado com base no ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Treino removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao remover treino"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Treino não pertence ao usuário"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerTreino(
            @Parameter(description = "ID do treino a ser removido", required = true)
            @PathVariable Long id,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            planoService.removerTreino(id, usuario.getId());
            logger.info("Treino removido para usuário ID {}: ID {}", usuario.getId(), id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao remover treino: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover treino: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }
}