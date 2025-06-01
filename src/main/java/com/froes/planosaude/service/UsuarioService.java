package com.froes.planosaude.service;

import com.froes.planosaude.model.Usuario;
import com.froes.planosaude.repository.UsuarioRepository;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getSenha(),
                List.of() // Adicione roles/autoridades se necessário
        );
    }

    public Optional<Usuario> findByEmail(String email) {
        logger.debug("Buscando usuário por email: {}", email);
        return usuarioRepository.findByEmail(email);
    }

    public void registrarUsuario(Usuario usuario) {
        logger.debug("Registrando usuário: {}", usuario.getEmail());
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já está em uso: " + usuario.getEmail());
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuarioRepository.save(usuario);
        logger.info("Usuário registrado com sucesso: {}", usuario.getEmail());
    }

	public void salvarUsuario(@Valid Usuario usuario) {
		// TODO Auto-generated method stub
		
	}

    
}