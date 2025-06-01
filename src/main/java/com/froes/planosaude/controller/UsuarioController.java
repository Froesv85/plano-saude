package com.froes.planosaude.controller;

import com.froes.planosaude.model.Evento;
import com.froes.planosaude.model.Refeicao;
import com.froes.planosaude.model.Treino;
import com.froes.planosaude.model.Usuario;
import com.froes.planosaude.model.UsuarioPeso;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@Tag(name = "Usuário MVC", description = "Endpoints MVC para autenticação e gerenciamento de usuário")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PlanoService planoService;

    @GetMapping("/login")
    @Operation(summary = "Exibir página de login", description = "Renderiza a página de login para autenticação do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de login carregada com sucesso")
    })
    public String login() {
        logger.debug("Carregando página de login");
        return "login";
    }

    @GetMapping("/registrar")
    @Operation(summary = "Exibir formulário de registro", description = "Renderiza a página para registro de novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulário de registro carregado com sucesso")
    })
    public String registrarForm(Model model) {
        logger.debug("Carregando formulário de registro");
        model.addAttribute("usuario", new Usuario());
        return "registrar";
    }

    @PostMapping("/registrar")
    @Operation(summary = "Registrar novo usuário", description = "Processa o formulário de registro e cria um novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redireciona para o login após registro bem-sucedido"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou falha ao registrar")
    })
    public String registrar(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao registrar usuário: {}", result.getAllErrors());
            return "registrar";
        }
        try {
            logger.debug("Registrando novo usuário: {}", usuario.getEmail());
            usuarioService.salvarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensagem", "Registro realizado com sucesso! Faça login.");
            logger.info("Usuário registrado com sucesso: {}", usuario.getEmail());
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao registrar usuário: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "registrar";
        } catch (Exception e) {
            logger.error("Erro inesperado ao registrar usuário: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar. Tente novamente.");
            return "registrar";
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Carregar a página de dashboard", description = "Renderiza o dashboard com informações do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard carregado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao carregar dashboard")
    })
    public String dashboard(Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.debug("Carregando dashboard para usuário: {}", email);
            Usuario usuario = usuarioService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));

            List<Evento> eventos = planoService.getEventos(email);
            List<Treino> treinos = planoService.getTreinos(email);
            List<Refeicao> refeicoes = planoService.getRefeicoes(email);
            model.addAttribute("usuario", usuario);
            model.addAttribute("eventos", eventos);
            model.addAttribute("treinos", treinos);
            model.addAttribute("refeicoes", refeicoes);
            

            if (usuario.getPeso() != null && usuario.getAltura() != null) {
                double imc = usuario.getPeso() / (usuario.getAltura() * usuario.getAltura());
                String nivelImc = calcularNivelImc(imc);
                model.addAttribute("imc", imc);
                model.addAttribute("nivelImc", nivelImc);
            }

            logger.info("Dashboard carregado com sucesso para usuário: {}", email);
            return "dashboard";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao carregar dashboard: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "dashboard";
        } catch (Exception e) {
            logger.error("Erro inesperado ao carregar dashboard: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar dashboard. Tente novamente.");
            return "dashboard";
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