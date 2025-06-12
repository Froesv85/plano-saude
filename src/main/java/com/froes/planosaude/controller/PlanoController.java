package com.froes.planosaude.controller;

import com.froes.planosaude.dto.TreinoForm;
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

	private Evento plano;

	private Throwable e;

    @GetMapping("/calendario")
    @Operation(summary = "Carregar a página de calendário", description = "Renderiza a página de calendário com eventos, treinos e refeições")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de calendário carregada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao carregar dados do usuário"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String calendario(Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Carregando página de calendário para usuário ID: {}", usuario.getId());
            model.addAttribute("eventos", planoService.getEventosByUsuario(usuario.getId()));
            model.addAttribute("treinos", planoService.getTreinosByUsuario(usuario.getId()));
            model.addAttribute("refeicoes", planoService.getRefeicoes(usuario.getId()));
            logger.info("Página de calendário carregada com sucesso para usuário ID: {}", usuario.getId());
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
            @ApiResponse(responseCode = "200", description = "Formulário de evento carregado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao carregar evento"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String eventoForm(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            
            logger.debug("Formulário de evento carregado para usuário ID: {}, evento ID: {}", usuario.getId(), id);
            return "evento";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao carregar formulário de evento: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "evento";
        } catch (Exception e) {
            logger.error("Erro inesperado ao carregar formulário de evento: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar formulário de evento. Tente novamente.");
            return "evento";
        }
    }

    @PostMapping("/evento")
    @Operation(summary = "Salvar um evento", description = "Processa o formulário de evento e salva ou atualiza via serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o calendário após salvar o evento"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String salvarEvento(@Valid @ModelAttribute("evento") Evento evento, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar evento: {}", result.getAllErrors());
            return "evento";
        }
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando evento para usuário ID: {}", usuario.getId());
            evento.setUsuario(usuario);
            if (evento.getStart() != null && evento.getStart().isBefore(LocalDateTime.now()) && evento.getId() == null) {
                throw new IllegalArgumentException("Data de início não pode ser no passado para novos eventos.");
            }
            planoService.salvarEvento(evento);
            redirectAttributes.addFlashAttribute("mensagem", "Evento salvo com sucesso!");
            logger.info("Evento salvo com sucesso para usuário ID: {}", usuario.getId());
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
            @ApiResponse(responseCode = "400", description = "Erro ao remover o evento"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String removerEvento(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Removendo evento com ID {} para usuário ID: {}", id, usuario.getId());
            redirectAttributes.addFlashAttribute("mensagem", "Evento removido com sucesso!");
            logger.info("Evento removido com sucesso para usuário ID: {}", usuario.getId());
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
            @ApiResponse(responseCode = "200", description = "Formulário de treino carregado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao carregar treino"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String treinoForm(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            Treino treino = (id != null) ? planoService.buscarTreinoPorId(id, usuario.getId()) : new Treino();
            treino.setUsuario(usuario);
            model.addAttribute("treino", treino);
            logger.debug("Formulário de treino carregado para usuário ID: {}, treino ID: {}", usuario.getId(), id);
            return "treino";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao carregar formulário de treino: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "treino";
        } catch (Exception e) {
            logger.error("Erro inesperado ao carregar formulário de treino: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar formulário de treino. Tente novamente.");
            return "treino";
        }
    }

    @PostMapping("/treino")
    @Operation(summary = "Salvar um treino", description = "Processa o formulário de treino e salva ou atualiza via serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o dashboard após salvar o treino"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String salvarTreino(@Valid @ModelAttribute("treino") Treino treino, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar treino: {}", result.getAllErrors());
            return "treino";
        }
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando treino para usuário ID: {}", usuario.getId());
            treino.setUsuario(usuario);
            if (treino.getData() == null) {
                treino.setData(LocalDateTime.now());
            }
            if (treino.getData().isBefore(LocalDateTime.now()) && treino.getId() == null) {
                throw new IllegalArgumentException("Data do treino não pode ser no passado para novos registros.");
            }
            planoService.salvarTreino(treino);
            redirectAttributes.addFlashAttribute("mensagem", "Treino salvo com sucesso!");
            logger.info("Treino salvo com sucesso para usuário ID: {}", usuario.getId());
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
            @ApiResponse(responseCode = "400", description = "Erro ao remover o treino"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String removerTreino(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Removendo treino com ID {} para usuário ID: {}", id, usuario.getId());
            planoService.removerTreino(id, usuario.getId());
            redirectAttributes.addFlashAttribute("mensagem", "Treino removido com sucesso!");
            logger.info("Treino removido com sucesso para usuário ID: {}", usuario.getId());
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
            @ApiResponse(responseCode = "200", description = "Formulário de refeição carregado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao carregar refeição"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String refeicaoForm(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            
        
            logger.debug("Formulário de refeição carregado para usuário ID: {}, refeição ID: {}", usuario.getId(), id);
            return "alimentacao";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao carregar formulário de refeição: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "alimentacao";
        } catch (Exception e) {
            logger.error("Erro inesperado ao carregar formulário de refeição: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar formulário de refeição. Tente novamente.");
            return "alimentacao";
        }
    }

    @PostMapping("/alimentacao")
    @Operation(summary = "Salvar uma refeição", description = "Processa o formulário de refeição e salva ou atualiza via serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o dashboard após salvar a refeição"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao salvar"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String salvarRefeicao(@Valid @ModelAttribute("refeicao") Refeicao refeicao, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar refeição: {}", result.getAllErrors());
            return "alimentacao";
        }
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando refeição para usuário ID: {}", usuario.getId());
            refeicao.setUsuario(usuario);
            if (refeicao.getData() == null) {
                refeicao.setData(LocalDateTime.now());
            }
            if (refeicao.getData().isBefore(LocalDateTime.now()) && refeicao.getId() == null) {
                throw new IllegalArgumentException("Data da refeição não pode ser no passado para novos registros.");
            }
            planoService.salvarRefeicao(refeicao);
            redirectAttributes.addFlashAttribute("mensagem", "Refeição salva com sucesso!");
            logger.info("Refeição salva com sucesso para usuário ID: {}", usuario.getId());
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
            @ApiResponse(responseCode = "400", description = "Erro ao remover a refeição"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String removerRefeicao(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Removendo refeição com ID {} para usuário ID: {}", id, usuario.getId());
      
            redirectAttributes.addFlashAttribute("mensagem", "Refeição removida com sucesso!");
            logger.info("Refeição removida com sucesso para usuário ID: {}", usuario.getId());
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao remover refeição: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover refeição: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover refeição. Tente novamente.");
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/plano-treino")
    public String planoTreino(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Carregando página de plano de treino para usuário ID: {}", usuario.getId());
            
            // Adiciona o objeto TreinoForm para o formulário
            TreinoForm treinoForm = new TreinoForm();
            if (id != null) {
                PlanoSaude planoSaude = planoService.buscarPlanoPorId(id, usuario.getId());
                // Mapeia os campos de PlanoSaude para TreinoForm, se necessário
                treinoForm.setDescricao(planoSaude.getDescricao());
                treinoForm.setData(planoSaude.getDataInicio());
            }
            model.addAttribute("treinoForm", treinoForm);
            model.addAttribute("planos", planoService.getPlanosByUsuarioAndTipo(usuario.getId(), PlanoSaude.TipoPlano.TREINO));
            logger.info("Página de plano de treino carregada com sucesso para usuário ID: {}", usuario.getId());
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
    public String salvarPlanoTreino(@Valid @ModelAttribute("treinoForm") TreinoForm treinoForm, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar plano de treino: {}", result.getAllErrors());
            redirectAttributes.addFlashAttribute("erro", "Erro nos dados do plano de treino. Verifique os campos.");
            return "redirect:/plano-treino";
        }
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando plano de treino para usuário ID: {}", usuario.getId());
            
            // Mapeia TreinoForm para PlanoSaude
            PlanoSaude plano = new PlanoSaude();
            plano.setUsuario(usuario);
            plano.setTipo(PlanoSaude.TipoPlano.TREINO);
            plano.setDescricao(treinoForm.getDescricao());
            plano.setDataInicio(treinoForm.getData());
            // Opcional: defina dataFim, metaSemanal, etc., se necessário
            plano.setDataFim(null); // Ou ajuste conforme sua lógica
            plano.setMetaSemanal(null); // Ou ajuste conforme sua lógica
            plano.setNome("Treino " + treinoForm.getData().toString()); // Exemplo de nome
            plano.setAtivo(true);

            // Valida datas
            if (plano.getDataInicio() == null) {
                throw new IllegalArgumentException("Data de início é obrigatória.");
            }
            if (plano.getDataInicio().isBefore(LocalDateTime.now()) && plano.getId() == null) {
                throw new IllegalArgumentException("Data de início não pode ser no passado para novos planos.");
            }
            if (plano.getDataFim() != null && plano.getDataFim().isBefore(plano.getDataInicio())) {
                throw new IllegalArgumentException("Data de término não pode ser anterior à data de início.");
            }

            planoService.salvarPlano(plano);
            redirectAttributes.addFlashAttribute("mensagem", "Plano de treino salvo com sucesso!");
            logger.info("Plano de treino salvo com sucesso para usuário ID: {}", usuario.getId());
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
            @ApiResponse(responseCode = "400", description = "Erro ao remover o plano"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String removerPlanoTreino(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Removendo plano de treino com ID {} para usuário ID: {}", id, usuario.getId());
            planoService.removerPlano(id, usuario.getId());
            redirectAttributes.addFlashAttribute("mensagem", "Plano de treino removido com sucesso!");
            logger.info("Plano de treino removido com sucesso para usuário ID: {}", usuario.getId());
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
    public String planoDieta(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Carregando página de plano de dieta para usuário ID: {}", usuario.getId());

            // Cria ou carrega o planoSaude
            PlanoSaude planoSaude;
            if (id != null) {
                planoSaude = planoService.buscarPlanoPorId(id, usuario.getId());
            } else {
                planoSaude = new PlanoSaude();
                planoSaude.setDataInicio(LocalDateTime.now());
                planoSaude.setTipo(PlanoSaude.TipoPlano.DIETA);
                planoSaude.setAtivo(true);
            }
            model.addAttribute("planoSaude", planoSaude);
            model.addAttribute("planos", planoService.getPlanosByUsuarioAndTipo(usuario.getId(), PlanoSaude.TipoPlano.DIETA));
            logger.info("Página de plano de dieta carregada com sucesso para usuário ID: {}", usuario.getId());
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
    public String salvarPlanoDieta(@Valid @ModelAttribute("planoSaude") PlanoSaude planoSaude, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar plano de dieta: {}", result.getAllErrors());
            redirectAttributes.addFlashAttribute("erro", "Erro nos dados do plano de dieta. Verifique os campos.");
            return "redirect:/plano-dieta";
        }
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Salvando plano de dieta para usuário ID: {}", usuario.getId());

            planoSaude.setUsuario(usuario);
            planoSaude.setTipo(PlanoSaude.TipoPlano.DIETA);

            // Validações adicionais
            if (planoSaude.getDataInicio().isBefore(LocalDateTime.now()) && planoSaude.getId() == null) {
                throw new IllegalArgumentException("Data de início não pode ser no passado para novos planos.");
            }
            if (planoSaude.getDataFim() != null && planoSaude.getDataFim().isBefore(planoSaude.getDataInicio())) {
                throw new IllegalArgumentException("Data de término não pode ser anterior à data de início.");
            }

            planoService.salvarPlano(planoSaude);
            redirectAttributes.addFlashAttribute("mensagem", "Plano de dieta salvo com sucesso!");
            logger.info("Plano de dieta salvo com sucesso para usuário ID: {}", usuario.getId());
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
            @ApiResponse(responseCode = "400", description = "Erro ao remover o plano"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String removerPlanoDieta(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("Usuário não autenticado");
            }
            String email = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
            logger.debug("Removendo plano de dieta com ID {} para usuário ID: {}", id, usuario.getId());
            planoService.removerPlano(id, usuario.getId());
            redirectAttributes.addFlashAttribute("mensagem", "Plano de dieta removido com sucesso!");
            logger.info("Plano de dieta removido com sucesso para usuário ID: {}", usuario.getId());
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
}