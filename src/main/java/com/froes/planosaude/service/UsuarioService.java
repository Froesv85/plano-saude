package com.froes.planosaude.service;

import com.froes.planosaude.model.Usuario;
import com.froes.planosaude.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Carregando usuário por email: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado: {}", email);
                    return new UsernameNotFoundException("Usuário não encontrado: " + email);
                });
        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getSenha(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // Default role
        );
    }

    public Optional<Usuario> findByEmail(String email) {
        logger.debug("Buscando usuário por email: {}", email);
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public void registrarUsuario(Usuario usuario) {
        logger.info("Iniciando registro do usuário: {}", usuario.getEmail());
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("E-mail já está em uso: " + usuario.getEmail());
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        Usuario saved = usuarioRepository.save(usuario);
        logger.info("Usuário salvo com ID: {}", saved.getId());
    }

    @Transactional
    public void salvarUsuario(Usuario usuario) {
        logger.info("Iniciando atualização do usuário: {}", usuario.getEmail());
        if (usuario.getId() == null) {
            logger.error("ID do usuário é necessário para atualização: {}", usuario.getEmail());
            throw new IllegalArgumentException("ID do usuário é necessário para atualização.");
        }
        Optional<Usuario> existingUsuario = usuarioRepository.findById(usuario.getId());
        if (existingUsuario.isEmpty()) {
            logger.error("Usuário não encontrado com ID: {}", usuario.getId());
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + usuario.getId());
        }
        try {
            // Preserva a senha existente
            usuario.setSenha(existingUsuario.get().getSenha());
            usuarioRepository.save(usuario);
            logger.info("Usuário atualizado com sucesso: {}", usuario.getEmail());
        } catch (Exception e) {
            logger.error("Erro ao atualizar usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao atualizar usuário: " + e.getMessage(), e);
        }
    }
}