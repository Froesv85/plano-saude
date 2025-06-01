package com.froes.planosaude.controller;

import com.froes.planosaude.model.Evento;
import com.froes.planosaude.model.PlanoSaude;
import com.froes.planosaude.model.Treino;
import com.froes.planosaude.model.Refeicao;
import com.froes.planosaude.model.Usuario;
import com.froes.planosaude.service.PlanoService;
import com.froes.planosaude.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    private UsuarioService usuarioService;

    @GetMapping("/calendario")
    @Operation(summary = "Carregar a página de calendário", description = "Renderiza a página de calendário com eventos, treinos e refeições")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de calendário carregada com sucesso")
    })
    public String calendario(Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.debug("Carregando página de calendário para usuário: {}", email);
            model.addAttribute("eventos", planoService.getEventos(email));
            model.addAttribute("treinos", planoService.getTreinos(email));
            model.addAttribute("refeicoes", planoService.getRefeicoes(email));
            logger.info("Página de calendário carregada com sucesso para usuário: {}", email);
            return "calendario";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao carregar calendário: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "calendario";
        } catch (Exception e) {
            logger.error("Erro inesperado ao carregar calendário: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar calendário. Tente novamente.");
            return "calendario";
        }
    }

    @GetMapping("/evento")
    @Operation(summary = "Exibir formulário de evento", description = "Renderiza a página para adicionar ou editar um evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulário de evento carregado com sucesso")
    })
    public String eventoForm(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
        Evento evento = (id != null) ? planoService.buscarEventoPorId(id) : new Evento();
        if (id != null && !evento.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Evento não pertence ao usuário autenticado.");
        }
        evento.setUsuario(usuario);
        model.addAttribute("evento", evento);
        return "evento";
    }

    @PostMapping("/evento")
    @Operation(summary = "Salvar um evento", description = "Processa o formulário de evento e salva ou atualiza via serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o calendário após salvar o evento"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar")
    })
    public String salvarEvento(@Valid @ModelAttribute("evento") Evento evento, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar evento: {}", result.getAllErrors());
            return "evento";
        }
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando evento para usuário: {}", email);
            evento.setUsuario(usuario);
            if (evento.getStart() != null && evento.getStart().isBefore(LocalDateTime.now()) && evento.getId() == null) {
                throw new IllegalArgumentException("Data de início não pode ser no passado para novos eventos.");
            }
            planoService.salvarEvento(evento);
            redirectAttributes.addFlashAttribute("mensagem", "Evento salvo com sucesso!");
            logger.info("Evento salvo com sucesso para usuário: {}", email);
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

    @GetMapping("/evento/deletar/{id}")
    @Operation(summary = "Remover um evento", description = "Remove um evento específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o calendário após remoção"),
            @ApiResponse(responseCode = "400", description = "Erro ao remover o evento")
    })
    public String removerEvento(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            Evento evento = planoService.buscarEventoPorId(id);
            if (!evento.getUsuario().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Evento não pertence ao usuário autenticado.");
            }
            logger.debug("Removendo evento com ID {} para usuário: {}", id, email);
            planoService.removerEvento(id, email);
            redirectAttributes.addFlashAttribute("mensagem", "Evento removido com sucesso!");
            logger.info("Evento removido com sucesso para usuário: {}", email);
            return "redirect:/calendario";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao remover evento: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/calendario";
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover evento: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover evento. Tente novamente.");
            return "redirect:/calendario";
        }
    }

    @GetMapping("/treino")
    @Operation(summary = "Exibir formulário de treino", description = "Renderiza a página para adicionar ou editar um treino")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulário de treino carregado com sucesso")
    })
    public String treinoForm(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
        Treino treino = (id != null) ? planoService.buscarTreinoPorId(id) : new Treino();
        if (id != null && !treino.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Treino não pertence ao usuário autenticado.");
        }
        treino.setUsuario(usuario);
        model.addAttribute("treino", treino);
        return "treino";
    }

    @PostMapping("/treino")
    @Operation(summary = "Salvar um treino", description = "Processa o formulário de treino e salva ou atualiza via serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o dashboard após salvar o treino"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar")
    })
    public String salvarTreino(@Valid @ModelAttribute("treino") Treino treino, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar treino: {}", result.getAllErrors());
            return "treino";
        }
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando treino para usuário: {}", email);
            treino.setUsuario(usuario);
            if (treino.getData() == null) {
                treino.setData(LocalDateTime.now());
            }
            if (treino.getData().isBefore(LocalDateTime.now()) && treino.getId() == null) {
                throw new IllegalArgumentException("Data do treino não pode ser no passado para novos registros.");
            }
            planoService.salvarTreino(treino);
            redirectAttributes.addFlashAttribute("mensagem", "Treino salvo com sucesso!");
            logger.info("Treino salvo com sucesso para usuário: {}", email);
            return "redirect:/dashboard";
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

    @GetMapping("/treino/deletar/{id}")
    @Operation(summary = "Remover um treino", description = "Remove um treino específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o dashboard após remoção"),
            @ApiResponse(responseCode = "400", description = "Erro ao remover o treino")
    })
    public String removerTreino(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            Treino treino = planoService.buscarTreinoPorId(id);
            if (!treino.getUsuario().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Treino não pertence ao usuário autenticado.");
            }
            logger.debug("Removendo treino com ID {} para usuário: {}", id, email);
            planoService.removerTreino(id, email);
            redirectAttributes.addFlashAttribute("mensagem", "Treino removido com sucesso!");
            logger.info("Treino removido com sucesso para usuário: {}", email);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao remover treino: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover treino: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover treino. Tente novamente.");
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/alimentacao")
    @Operation(summary = "Exibir formulário de refeição", description = "Renderiza a página para adicionar ou editar uma refeição")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulário de refeição carregado com sucesso")
    })
    public String refeicaoForm(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
        Refeicao refeicao = (id != null) ? planoService.buscarRefeicaoPorId(id) : new Refeicao();
        if (id != null && !refeicao.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Refeição não pertence ao usuário autenticado.");
        }
        refeicao.setUsuario(usuario);
        model.addAttribute("refeicao", refeicao);
        return "alimentacao";
    }

    @PostMapping("/alimentacao")
    @Operation(summary = "Salvar uma refeição", description = "Processa o formulário de refeição e salva ou atualiza via serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o dashboard após salvar a refeição"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar")
    })
    public String salvarRefeicao(@Valid @ModelAttribute("refeicao") Refeicao refeicao, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar refeição: {}", result.getAllErrors());
            return "alimentacao";
        }
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando refeição para usuário: {}", email);
            refeicao.setUsuario(usuario);
            if (refeicao.getData() == null) {
                refeicao.setData(LocalDateTime.now());
            }
            if (refeicao.getData().isBefore(LocalDateTime.now()) && refeicao.getId() == null) {
                throw new IllegalArgumentException("Data da refeição não pode ser no passado para novos registros.");
            }
            planoService.salvarRefeicao(refeicao);
            redirectAttributes.addFlashAttribute("mensagem", "Refeição salva com sucesso!");
            logger.info("Refeição salva com sucesso para usuário: {}", email);
            return "redirect:/dashboard";
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

    @GetMapping("/alimentacao/deletar/{id}")
    @Operation(summary = "Remover uma refeição", description = "Remove uma refeição específica pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o dashboard após remoção"),
            @ApiResponse(responseCode = "400", description = "Erro ao remover a refeição")
    })
    public String removerRefeicao(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            Refeicao refeicao = planoService.buscarRefeicaoPorId(id);
            if (!refeicao.getUsuario().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Refeição não pertence ao usuário autenticado.");
            }
            planoService.removerRefeicao(id, email);
            redirectAttributes.addFlashAttribute("mensagem", "Refeição removida com sucesso!");
            logger.info("Refeição removida com sucesso para usuário: {}", email);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao remover refeição: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover refeição: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover refeição. Tente novamente.");
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/plano-treino")
    @Operation(summary = "Carregar a página de plano de treino", description = "Renderiza a página com a lista de planos de treino do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de plano de treino carregada com sucesso")
    })
    public String planoTreino(Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.debug("Carregando página de plano de treino para usuário: {}", email);
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            model.addAttribute("planos", planoService.getPlanosByUsuarioAndTipo(usuario.getId(), PlanoSaude.TipoPlano.TREINO));
            logger.info("Página de plano de treino carregada com sucesso para usuário: {}", email);
            return "plano-treino";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao carregar plano de treino: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "plano-treino";
        } catch (Exception e) {
            logger.error("Erro inesperado ao carregar plano de treino: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar plano de treino. Tente novamente.");
            return "plano-treino";
        }
    }

    @PostMapping("/plano-treino/salvar")
    @Operation(summary = "Salvar um plano de treino", description = "Processa o formulário de plano de treino e salva ou atualiza via serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para a página de plano de treino após salvar"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar")
    })
    public String salvarPlanoTreino(@Valid @ModelAttribute("planoSaude") PlanoSaude plano, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar plano de treino: {}", result.getAllErrors());
            return "plano-treino";
        }
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando plano de treino para usuário: {}", email);
            plano.setUsuario(usuario);
            plano.setTipo(PlanoSaude.TipoPlano.TREINO);
            if (plano.getDataInicio() != null && plano.getDataInicio().isBefore(LocalDateTime.now()) && plano.getId() == null) {
                throw new IllegalArgumentException("Data de início não pode ser no passado para novos planos.");
            }
            if (plano.getDataFim() != null && plano.getDataFim().isBefore(plano.getDataInicio())) {
                throw new IllegalArgumentException("Data de término não pode ser anterior à data de início.");
            }
            planoService.salvarPlano(plano);
            redirectAttributes.addFlashAttribute("mensagem", "Plano de treino salvo com sucesso!");
            logger.info("Plano de treino salvo com sucesso para usuário: {}", email);
            return "redirect:/plano-treino";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao salvar plano de treino: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/plano-treino";
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar plano de treino: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar plano de treino. Tente novamente.");
            return "redirect:/plano-treino";
        }
    }

    @GetMapping("/plano-treino/deletar/{id}")
    @Operation(summary = "Remover um plano de treino", description = "Remove um plano de treino específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para a página de plano de treino após remoção"),
            @ApiResponse(responseCode = "400", description = "Erro ao remover o plano")
    })
    public String removerPlanoTreino(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            PlanoSaude plano = planoService.buscarPlanoPorId(id);
            if (!plano.getUsuario().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Plano não pertence ao usuário autenticado.");
            }
            logger.debug("Removendo plano de treino com ID {} para usuário: {}", id, email);
            planoService.removerPlano(id, email);
            redirectAttributes.addFlashAttribute("mensagem", "Plano de treino removido com sucesso!");
            logger.info("Plano de treino removido com sucesso para usuário: {}", email);
            return "redirect:/plano-treino";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao remover plano de treino: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/plano-treino";
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover plano de treino: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover plano de treino. Tente novamente.");
            return "redirect:/plano-treino";
        }
    }

    @GetMapping("/plano-dieta")
    @Operation(summary = "Carregar a página de plano de dieta", description = "Renderiza a página com a lista de planos de dieta do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de plano de dieta carregada com sucesso")
    })
    public String planoDieta(Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.debug("Carregando página de plano de dieta para usuário: {}", email);
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            model.addAttribute("planos", planoService.getPlanosByUsuarioAndTipo(usuario.getId(), PlanoSaude.TipoPlano.DIETA));
            logger.info("Página de plano de dieta carregada com sucesso para usuário: {}", email);
            return "plano-dieta";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao carregar plano de dieta: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "plano-dieta";
        } catch (Exception e) {
            logger.error("Erro inesperado ao carregar plano de dieta: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar plano de dieta. Tente novamente.");
            return "plano-dieta";
        }
    }

    @PostMapping("/plano-dieta/salvar")
    @Operation(summary = "Salvar um plano de dieta", description = "Processa o formulário de plano de dieta e salva ou atualiza via serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para a página de plano de dieta após salvar"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar")
    })
    public String salvarPlanoDieta(@Valid @ModelAttribute("planoSaude") PlanoSaude plano, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar plano de dieta: {}", result.getAllErrors());
            return "plano-dieta";
        }
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando plano de dieta para usuário: {}", email);
            plano.setUsuario(usuario);
            plano.setTipo(PlanoSaude.TipoPlano.DIETA);
            if (plano.getDataInicio() != null && plano.getDataInicio().isBefore(LocalDateTime.now()) && plano.getId() == null) {
                throw new IllegalArgumentException("Data de início não pode ser no passado para novos planos.");
            }
            if (plano.getDataFim() != null && plano.getDataFim().isBefore(plano.getDataInicio())) {
                throw new IllegalArgumentException("Data de término não pode ser anterior à data de início.");
            }
            planoService.salvarPlano(plano);
            redirectAttributes.addFlashAttribute("mensagem", "Plano de dieta salvo com sucesso!");
            logger.info("Plano de dieta salvo com sucesso para usuário: {}", email);
            return "redirect:/plano-dieta";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao salvar plano de dieta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/plano-dieta";
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar plano de dieta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar plano de dieta. Tente novamente.");
            return "redirect:/plano-dieta";
        }
    }

    @GetMapping("/plano-dieta/deletar/{id}")
    @Operation(summary = "Remover um plano de dieta", description = "Remove um plano de dieta específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para a página de plano de dieta após remoção"),
            @ApiResponse(responseCode = "400", description = "Erro ao remover o plano")
    })
    public String removerPlanoDieta(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            PlanoSaude plano = planoService.buscarPlanoPorId(id);
            if (!plano.getUsuario().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Plano não pertence ao usuário autenticado.");
            }
            logger.debug("Removendo plano de dieta com ID {} para usuário: {}", id, email);
            planoService.removerPlano(id, email);
            redirectAttributes.addFlashAttribute("mensagem", "Plano de dieta removido com sucesso!");
            logger.info("Plano de dieta removido com sucesso para usuário: {}", email);
            return "redirect:/plano-dieta";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao remover plano de dieta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/plano-dieta";
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover plano de dieta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover plano de dieta. Tente novamente.");
            return "redirect:/plano-dieta";
        }
    }

    @PostMapping("/dashboard/atualizar-perfil")
    @Operation(summary = "Atualizar perfil de saúde do usuário", description = "Processa o formulário de peso, altura e idade, calcula o IMC e atualiza o perfil do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o dashboard com o IMC calculado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao atualizar o perfil")
    })
    public String atualizarPerfil(
            @RequestParam("peso") Double peso,
            @RequestParam("altura") Double altura,
            @RequestParam("idade") Integer idade,
            RedirectAttributes redirectAttributes,
            Authentication authentication,
            Model model) {
        try {
            String email = authentication.getName();
            logger.debug("Atualizando perfil de saúde para usuário: {}", email);
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));

            // Validar entradas
            if (peso <= 0 || peso > 500) {
                throw new IllegalArgumentException("Peso deve ser um valor positivo e razoável (0 < peso ≤ 500 kg).");
            }
            if (altura <= 0 || altura > 3) {
                throw new IllegalArgumentException("Altura deve ser um valor positivo e razoável (0 < altura ≤ 3 m).");
            }
            if (idade <= 0 || idade > 150) {
                throw new IllegalArgumentException("Idade deve ser um valor positivo e razoável (0 < idade ≤ 150 anos).");
            }

            // Atualizar dados do usuário
            usuario.setPeso(peso);
            usuario.setAltura(altura);
            usuario.setIdade(idade);

            // Registrar peso no histórico
            planoService.registrarPeso(usuario, peso);

            // Calcular IMC
            double imc = peso / (altura * altura);
            String nivelImc = calcularNivelImc(imc);

            // Adicionar atributos ao modelo para exibição no dashboard
            redirectAttributes.addFlashAttribute("imc", imc);
            redirectAttributes.addFlashAttribute("nivelImc", nivelImc);
            redirectAttributes.addFlashAttribute("mensagem", "Perfil atualizado com sucesso!");

            logger.info("Perfil de saúde atualizado com sucesso para usuário: {}", email);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao atualizar perfil: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar perfil: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar perfil. Tente novamente.");
            return "redirect:/dashboard";
        }
    }

    private String calcularNivelImc(double imc) {
        if (imc < 18.5) {
            return "Abaixo do peso";
        } else if (imc >= 18.5 && imc < 25) {
            return "Peso normal";
        } else if (imc >= 25 && imc < 30) {
            return "Sobrepeso";
        } else {
            return "Obesidade";
        }
    }
}