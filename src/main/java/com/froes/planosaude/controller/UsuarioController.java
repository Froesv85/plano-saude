package com.froes.planosaude.controller;

import com.froes.planosaude.dto.PerfilForm;
import com.froes.planosaude.model.Evento;
import com.froes.planosaude.model.Refeicao;
import com.froes.planosaude.model.Treino;
import com.froes.planosaude.model.Usuario;
import com.froes.planosaude.service.PlanoService;
import com.froes.planosaude.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// DTO for registration form
class RegisterForm {
    @NotBlank(message = "O nome é obrigatório")
    private String nome;
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    private String email;
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String password;
    @NotBlank(message = "A confirmação da senha é obrigatória")
    private String confirmPassword;

    // Getters and Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}

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
    @Operation(summary = "Exibir formulário de registro", description = "Renderiza o formulário de registro de novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulário de registro carregado com sucesso")
    })
    public String registrarForm(Model model) {
        logger.debug("Carregando formulário de registro");
        model.addAttribute("registerForm", new RegisterForm());
        return "registrar";
    }

    @PostMapping("/registrar")
    public String registrar(@Valid @ModelAttribute("registerForm") RegisterForm registerForm, BindingResult result,
                            RedirectAttributes redirectAttributes, Model model) {
        logger.info("Recebido formulário: nome={}, email={}, password=****, confirmPassword=****",
                    registerForm.getNome(), registerForm.getEmail());
        if (result.hasErrors()) {
            logger.warn("Erros de validação: {}", result.getAllErrors());
            model.addAttribute("erro", "Por favor, corrija os erros no formulário.");
            return "registrar";
        }
        if (!registerForm.getPassword().equals(registerForm.getConfirmPassword())) {
            logger.warn("Senhas não coincidem para o e-mail: {}", registerForm.getEmail());
            model.addAttribute("erro", "As senhas não coincidem.");
            return "registrar";
        }
        try {
            logger.debug("Mapeando para Usuario: {}", registerForm.getEmail());
            Usuario usuario = new Usuario();
            usuario.setNome(registerForm.getNome());
            usuario.setEmail(registerForm.getEmail());
            usuario.setSenha(registerForm.getPassword());
            logger.debug("Chamando registrarUsuario para: {}", usuario.getEmail());
            usuarioService.registrarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensagem", "Registro realizado com sucesso! Faça login.");
            logger.info("Usuário registrado com sucesso: {}", registerForm.getEmail());
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao registrar usuário: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "registrar";
        } catch (Exception e) {
            logger.error("Erro inesperado ao registrar usuário: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao registrar. Tente novamente.");
            return "registrar";
        }
    }
    
    @GetMapping("/dashboard")
    @Operation(summary = "Carregar o dashboard do usuário", description = "Renderiza a página principal com informações do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard carregado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String dashboard(Model model, Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.debug("Carregando dashboard para usuário: {}", email);

            Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                logger.error("Usuário não encontrado: {}", email);
                return "redirect:/login";
            }
            Usuario usuario = usuarioOpt.get();

            LocalDate hoje = LocalDate.now();
            List<Evento> eventos = planoService.getEventosByUsuarioAndData(usuario.getId(), hoje);
            List<Treino> treinos = planoService.getTreinosByUsuarioAndData(usuario.getId(), hoje);
            List<Refeicao> refeicoes = planoService.getRefeicoesByUsuarioAndData(usuario.getId(), hoje);

            Double imc = null;
            String nivelImc = null;
            if (usuario.getPeso() != null && usuario.getAltura() != null && usuario.getAltura() > 0) {
                imc = usuario.getPeso() / (usuario.getAltura() * usuario.getAltura());
                nivelImc = calcularNivelImc(imc);
            }

            if (!model.containsAttribute("perfilForm")) {
                PerfilForm perfilForm = new PerfilForm();
                perfilForm.setPeso(usuario.getPeso());
                perfilForm.setAltura(usuario.getAltura());
                perfilForm.setIdade(usuario.getIdade());
                model.addAttribute("perfilForm", perfilForm);
            }

            model.addAttribute("usuario", usuario);
            model.addAttribute("eventos", eventos);
            model.addAttribute("treinos", treinos);
            model.addAttribute("refeicoes", refeicoes);
            model.addAttribute("imc", imc);
            model.addAttribute("nivelImc", nivelImc);

            logger.info("Dashboard carregado com sucesso para usuário: {}", email);
            return "dashboard";
        } catch (Exception e) {
            logger.error("Erro ao carregar dashboard: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao carregar dashboard. Tente novamente.");
            return "dashboard";
        }
    }

    @PostMapping("/dashboard/atualizar-perfil")
    @Operation(summary = "Atualizar perfil do usuário", description = "Atualiza peso, altura e idade do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Perfil atualizado com sucesso, redireciona para dashboard"),
            @ApiResponse(responseCode = "400", description = "Erro nos dados fornecidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public String atualizarPerfil(@Valid @ModelAttribute("perfilForm") PerfilForm perfilForm,
                                  BindingResult result,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            logger.debug("Atualizando perfil para usuário: {}", email);

            if (result.hasErrors()) {
                logger.debug("Erros de validação encontrados: {}", result.getAllErrors());
                Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
                if (usuarioOpt.isEmpty()) {
                    model.addAttribute("erro", "Usuário não encontrado.");
                    return "dashboard";
                }
                Usuario usuario = usuarioOpt.get();
                model.addAttribute("usuario", usuario);
                return "dashboard";
            }

            Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                model.addAttribute("erro", "Usuário não encontrado.");
                return "dashboard";
            }

            Usuario usuario = usuarioOpt.get();
            usuario.setPeso(perfilForm.getPeso());
            usuario.setAltura(perfilForm.getAltura());
            usuario.setIdade(perfilForm.getIdade());

            usuarioService.salvarUsuario(usuario);

            // Register weight history
            if (perfilForm.getPeso() != null && perfilForm.getPeso() > 0) {
                try {
                    planoService.registrarPeso(usuario.getId(), perfilForm.getPeso());
                } catch (IllegalArgumentException e) {
                    logger.error("Erro ao registrar peso no histórico: {}", e.getMessage(), e);
                    redirectAttributes.addFlashAttribute("erro", "Perfil atualizado, mas falha ao registrar histórico de peso: " + e.getMessage());
                }
            }

            // Calculate BMI
            double imc = perfilForm.getPeso() / (perfilForm.getAltura() * perfilForm.getAltura());
            String nivelImc = calcularNivelImc(imc);

            redirectAttributes.addFlashAttribute("imc", imc);
            redirectAttributes.addFlashAttribute("nivelImc", nivelImc);
            redirectAttributes.addFlashAttribute("mensagem", "Perfil atualizado com sucesso!");
            logger.info("Perfil atualizado com sucesso para usuário: {}", email);

            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao atualizar perfil: {}", e.getMessage(), e);
            model.addAttribute("erro", e.getMessage());
            return "dashboard";
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar perfil: {}", e.getMessage(), e);
            model.addAttribute("erro", "Erro ao atualizar perfil. Tente novamente.");
            return "dashboard";
        }
    }

    @GetMapping("/dashboard/bmi")
    @ResponseBody
    @Operation(summary = "Obter dados de IMC", description = "Retorna o IMC e nível do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados de IMC retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao calcular IMC")
    })
    public Map<String, Object> getBmi(Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.debug("Obtendo IMC para usuário: {}", email);
            Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                throw new IllegalArgumentException("Usuário não encontrado: " + email);
            }
            Usuario usuario = usuarioOpt.get();
            Map<String, Object> response = new HashMap<>();
            if (usuario.getPeso() != null && usuario.getAltura() != null && usuario.getAltura() > 0) {
                double imc = usuario.getPeso() / (usuario.getAltura() * usuario.getAltura());
                response.put("imc", imc);
                response.put("nivelImc", calcularNivelImc(imc));
                response.put("idade", usuario.getIdade() != null ? usuario.getIdade() : 0);
            } else {
                response.put("error", "Peso ou altura não definidos");
            }
            return response;
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao obter IMC: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("erro", e.getMessage());
            return response;
        }
    }

    private String calcularNivelImc(Double imc) {
        if (imc == null) {
            return "Não calculado";
        }
        if (imc < 18.5) {
            return "Abaixo do peso";
        } else if (imc < 25) {
            return "Peso normal";
        } else if (imc < 30) {
            return "Sobrepeso";
        }
        return "Obesidade";
    }
}