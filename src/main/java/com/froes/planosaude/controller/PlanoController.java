package com.froes.planosaude.controller;

import com.froes.planosaude.model.Evento;
import com.froes.planosaude.model.Treino;
import com.froes.planosaude.model.Refeicao;
import com.froes.planosaude.service.PlanoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Controller
@Tag(name = "Plano MVC", description = "Endpoints MVC para renderizar páginas e formulários do sistema PlanoSaude")
public class PlanoController {

    private static final Logger logger = LoggerFactory.getLogger(PlanoController.class);

    @Autowired
    private PlanoService planoService;

    @Autowired
    private RestTemplate restTemplate; // Para fazer chamadas às APIs REST

    @GetMapping("/")
    @Operation(summary = "Carregar o dashboard", description = "Renderiza a página inicial com treinos, refeições e peso atual do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard carregado com sucesso")
    })
    public String dashboard(Model model) {
        try {
            logger.debug("Carregando dashboard");
            model.addAttribute("treinos", planoService.getTreinosHoje());
            model.addAttribute("refeicoes", planoService.getRefeicoesHoje());
            model.addAttribute("eventos", planoService.getEventos());
            model.addAttribute("pesoAtual", planoService.getPesoAtual());
            logger.info("Dashboard carregada com sucesso");
            return "dashboard";
        } catch (Exception e) {
            logger.error("Erro ao carregar dashboard: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar dashboard. Tente novamente.");
            return "dashboard";
        }
    }

    @GetMapping("/calendario")
    @Operation(summary = "Carregar a página de calendário", description = "Renderiza a página de calendário (os dados são carregados via API)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de calendário carregada com sucesso")
    })
    public String calendario(Model model) {
        try {
            logger.debug("Carregando página de calendário");
            logger.info("Página de calendário carregada com sucesso");
            return "calendario";
        } catch (Exception e) {
            logger.error("Erro ao carregar calendário: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar calendário. Tente novamente.");
            return "calendario";
        }
    }

    @GetMapping("/evento")
    @Operation(summary = "Exibir formulário de evento", description = "Renderiza a página para adicionar um novo evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulário de evento carregado com sucesso")
    })
    public String eventoForm(Model model) {
        model.addAttribute("evento", new Evento());
        return "evento";
    }

    @PostMapping("/evento")
    @Operation(summary = "Salvar um evento", description = "Processa o formulário de evento e salva via API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o calendário após salvar o evento"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar")
    })
    public String salvarEvento(@Valid @ModelAttribute("evento") Evento evento, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar evento: {}", result.getAllErrors());
            return "evento";
        }
        try {
            logger.debug("Salvando evento: {}", evento);
            // Chamar a API REST para salvar o evento
            ResponseEntity<Evento> response = restTemplate.postForEntity("http://localhost:8080/api/eventos", evento, Evento.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("mensagem", "Evento salvo com sucesso!");
                logger.info("Evento salvo com sucesso via API: {}", evento);
            } else {
                throw new IllegalStateException("Falha ao salvar evento via API: " + response.getStatusCode());
            }
            return "redirect:/calendario";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao salvar evento: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/evento";
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar evento: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar evento. Tente novamente.");
            return "redirect:/evento";
        }
    }

    @GetMapping("/treino")
    @Operation(summary = "Exibir formulário de treino", description = "Renderiza a página para adicionar um novo treino")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulário de treino carregado com sucesso")
    })
    public String treinoForm(Model model) {
        model.addAttribute("treino", new Treino());
        return "treino";
    }

    @PostMapping("/treino")
    @Operation(summary = "Salvar um treino", description = "Processa o formulário de treino e salva via API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o dashboard após salvar o treino"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar")
    })
    public String salvarTreino(@Valid @ModelAttribute("treino") Treino treino, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar treino: {}", result.getAllErrors());
            return "treino";
        }
        try {
            logger.debug("Salvando treino: {}", treino);
            if (treino.getData() == null) {
                treino.setData(LocalDateTime.now());
            }
            // Chamar a API REST para salvar o treino
            ResponseEntity<Treino> response = restTemplate.postForEntity("http://localhost:8080/api/treinos", treino, Treino.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("mensagem", "Treino salvo com sucesso!");
                logger.info("Treino salvo com sucesso via API: {}", treino);
            } else {
                throw new IllegalStateException("Falha ao salvar treino via API: " + response.getStatusCode());
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao salvar treino: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/treino";
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar treino: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar treino. Tente novamente.");
            return "redirect:/treino";
        }
    }

    @GetMapping("/alimentacao")
    @Operation(summary = "Exibir formulário de refeição", description = "Renderiza a página para adicionar uma nova refeição")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulário de refeição carregado com sucesso")
    })
    public String refeicaoForm(Model model) {
        model.addAttribute("refeicao", new Refeicao());
        return "alimentacao";
    }

    @PostMapping("/alimentacao")
    @Operation(summary = "Salvar uma refeição", description = "Processa o formulário de refeição e salva via API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o dashboard após salvar a refeição"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar")
    })
    public String salvarRefeicao(@Valid @ModelAttribute("refeicao") Refeicao refeicao, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar refeição: {}", result.getAllErrors());
            return "alimentacao";
        }
        try {
            logger.debug("Salvando refeição: {}", refeicao);
            if (refeicao.getData() == null) {
                refeicao.setData(LocalDateTime.now());
            }
            // Chamar a API REST para salvar a refeição
            ResponseEntity<Refeicao> response = restTemplate.postForEntity("http://localhost:8080/api/refeicoes", refeicao, Refeicao.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("mensagem", "Refeição salva com sucesso!");
                logger.info("Refeição salva com sucesso via API: {}", refeicao);
            } else {
                throw new IllegalStateException("Falha ao salvar refeição via API: " + response.getStatusCode());
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao salvar refeição: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/alimentacao";
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar refeição: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar refeição. Tente novamente.");
            return "redirect:/alimentacao";
        }
    }
}