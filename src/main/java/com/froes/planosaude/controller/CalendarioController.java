package com.froes.planosaude.controller;

import com.froes.planosaude.model.Evento;
import com.froes.planosaude.model.Refeicao;
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
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Calendário", description = "API para gerenciamento de eventos e refeições no calendário")
public class CalendarioController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioController.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private PlanoService planoService;

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Listar eventos por intervalo de datas", description = "Retorna eventos do usuário autenticado entre as datas de início e fim")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato de data inválido ou erro de validação"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/eventos/por-data")
    public ResponseEntity<?> getEventosPorData(
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
            List<Evento> eventos = planoService.getEventosByUsuarioAndData(usuario.getId(), startDate, endDate);
            logger.info("Encontrados {} eventos para usuário ID {} entre {} e {}", eventos.size(), usuario.getId(), startDate, endDate);
            return ResponseEntity.ok(eventos);
        } catch (DateTimeParseException e) {
            logger.error("Erro ao parsear datas: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de data inválido"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao buscar eventos: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar eventos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Listar refeições por intervalo de datas", description = "Retorna refeições do usuário autenticado entre as datas de início e fim")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de refeições retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato de data inválido ou erro de validação"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/refeicoes/por-data")
    public ResponseEntity<?> getRefeicoesPorData(
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
            List<Refeicao> refeicoes = planoService.getRefeicoesByUsuarioAndDataRange(usuario.getId(), startDate, endDate);
            logger.info("Encontradas {} refeições para usuário ID {} entre {} e {}", refeicoes.size(), usuario.getId(), startDate, endDate);
            return ResponseEntity.ok(refeicoes);
        } catch (DateTimeParseException e) {
            logger.error("Erro ao parsear datas: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de data inválido"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao buscar refeições: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar refeições: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Criar um novo evento", description = "Adiciona um evento para o usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou formato de data incorreto"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/eventos")
    public ResponseEntity<?> criarEvento(
            @Parameter(description = "Dados do evento (title, start, end, description)", required = true)
            @RequestBody Map<String, Object> eventoData,
            Authentication authentication) {
        try {
            // Authentication check
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));

            // Validate title
            String title = (String) eventoData.get("title");
            if (title == null || title.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Título é obrigatório"));
            }

            // Parse dates
            LocalDateTime start = LocalDateTime.parse((String) eventoData.get("start"), formatter);
            LocalDateTime end = eventoData.get("end") != null ? LocalDateTime.parse((String) eventoData.get("end"), formatter) : null;
            String description = eventoData.get("description") != null ? (String) eventoData.get("description") : null;
            logger.info(description);

            // Create and save event
            Evento evento = new Evento();
            evento.setUsuario(usuario);
            evento.setTitle(title);
            evento.setStart(start);
            evento.setEnd(end);
            evento.setDescription(description);
            planoService.salvarEvento(evento);
            logger.info("Evento criado para usuário ID {}: {}", usuario.getId(), title);
            return ResponseEntity.ok(Map.of());
        } catch (DateTimeParseException e) {
            logger.error("Erro ao parsear datas: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de data inválido"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao criar evento: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar evento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }
    
    @PutMapping("/eventos/{id}")
    public ResponseEntity<?> atualizarEvento(
            @Parameter(description = "ID do evento a ser atualizado", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados do evento (title, start, end, description)", required = true)
            @RequestBody Map<String, Object> eventoData,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));

            String title = (String) eventoData.get("title");
            if (title == null || title.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Título é obrigatório"));
            }
            LocalDateTime start = LocalDateTime.parse((String) eventoData.get("start"), formatter);
            LocalDateTime end = eventoData.get("end") != null ? LocalDateTime.parse((String) eventoData.get("end"), formatter) : null;
            String description = eventoData.get("description") != null ? (String) eventoData.get("description") : null;

            return ResponseEntity.ok(Map.of());
        } catch (DateTimeParseException e) {
            logger.error("Erro ao parsear datas: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de data inválido"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao atualizar evento: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar evento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Excluir um evento", description = "Remove um evento do usuário autenticado com base no ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao excluir evento"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Evento não pertence ao usuário"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/eventos/{id}")
    public ResponseEntity<?> excluirEvento(
            @Parameter(description = "ID do evento a ser excluído", required = true)
            @PathVariable Long id,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
          
            logger.info("Evento excluído para usuário ID {}: ID {}", usuario.getId(), id);
            return ResponseEntity.ok(Map.of());
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao excluir evento: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir evento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Criar uma nova refeição", description = "Adiciona uma refeição para o usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refeição criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou formato de data incorreto"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/refeicoes")
    public ResponseEntity<?> criarRefeicao(
            @Parameter(description = "Dados da refeição (descricao, data)", required = true)
            @RequestBody Map<String, Object> refeicaoData,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));

            String descricao = (String) refeicaoData.get("descricao");
            if (descricao == null || descricao.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Descrição é obrigatória"));
            }
            LocalDateTime data = LocalDateTime.parse((String) refeicaoData.get("data"), formatter);

            Refeicao refeicao = new Refeicao();
            refeicao.setUsuario(usuario);
            refeicao.setDescricao(descricao);
            refeicao.setData(data);
            planoService.salvarRefeicao(refeicao);
            logger.info("Refeição criada para usuário ID {}: {}", usuario.getId(), descricao);
            return ResponseEntity.ok(Map.of());
        } catch (DateTimeParseException e) {
            logger.error("Erro ao parsear data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Formato de data inválido"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao criar refeição: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar refeição: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Atualizar uma refeição existente", description = "Atualiza uma refeição do usuário autenticado com base no ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refeição atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou formato de data incorreto"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Refeição não pertence ao usuário"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/refeicoes/{id}")
    public ResponseEntity<?> atualizarRefeicao(
            @Parameter(description = "ID da refeição a ser atualizada", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados da refeição (descricao, data)", required = true)
            @RequestBody Map<String, Object> refeicaoData,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não autenticado"));
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));

            String descricao = (String) refeicaoData.get("descricao");
            if (descricao == null || descricao.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Descrição é obrigatória"));
            }
            LocalDateTime data = LocalDateTime.parse((String) refeicaoData.get("data"), formatter);

            
            logger.info("Refeição atualizada para usuário ID {}: ID {}", usuario.getId(), id);
            return ResponseEntity.ok(Map.of());
        } catch (DateTimeParseException e) {
            logger.error("Erro ao atualizar refeição: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar refeição: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    
}