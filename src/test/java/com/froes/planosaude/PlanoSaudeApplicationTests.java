package com.froes.planosaude;

import com.froes.planosaude.model.Evento;
import com.froes.planosaude.model.Refeicao;
import com.froes.planosaude.model.Treino;
import com.froes.planosaude.model.Usuario;
import com.froes.planosaude.model.UsuarioPeso;
import com.froes.planosaude.service.PlanoService;
import com.froes.planosaude.service.UsuarioService;
import com.froes.planosaude.repository.UsuarioRepository;
import com.froes.planosaude.repository.EventoRepository;
import com.froes.planosaude.repository.RefeicaoRepository;

import org.glassfish.jaxb.runtime.v2.schemagen.xmlschema.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PlanoSaudeApplicationTests {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PlanoService planoService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private EventoRepository eventoRepository;

    @MockBean
    private RefeicaoRepository refeicaoRepository;

    @MockBean
    private PasswordEncoder passwordEncoder; // Mock para criptografia de senha

    @Test
    void contextLoads() {
        assertNotNull(usuarioService, "UsuarioService não foi injetado corretamente");
        assertNotNull(planoService, "PlanoService não foi injetado corretamente");
    }

    @Test
    void testSalvarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste Usuário");
        usuario.setEmail("teste@exemplo.com");
        usuario.setSenha("senha123");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome("Teste Usuário");
        usuarioSalvo.setEmail("teste@exemplo.com");
        usuarioSalvo.setSenha("senhaCriptografada");

        when(usuarioRepository.findByEmail("teste@exemplo.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCriptografada");
        when(usuarioRepository.save(usuario)).thenReturn(usuarioSalvo);

        usuarioService.salvarUsuario(usuario);

        assertNotNull(usuarioSalvo.getId(), "O ID do usuário deve ser gerado após salvamento");
        assertNotEquals("senha123", usuarioSalvo.getSenha(), "A senha deve ser criptografada");
    }

    @Test
    void testBuscarEventos() {
        String email = "teste@exemplo.com";
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);

        LocalDateTime start = LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now().plusYears(100);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(eventoRepository.findByUsuarioAndStartBetweenOrderByStartDesc(usuario, start, end))
                .thenReturn(Collections.emptyList());

        
    }

    @Test
    void testSalvarEvento() {
        String email = "teste@exemplo.com";
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);

        Evento evento = new Evento();
        evento.setTitle("Reunião");
        evento.setStart(LocalDateTime.now());
        evento.setUsuario(usuario);

        Evento eventoSalvo = new Evento();
        eventoSalvo.setId(1L);
        eventoSalvo.setTitle("Reunião");
        eventoSalvo.setStart(LocalDateTime.now());
        eventoSalvo.setUsuario(usuario);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(eventoRepository.save(evento)).thenReturn(eventoSalvo);

        Evento resultado = planoService.salvarEvento(evento);
        assertNotNull(resultado.getId(), "O ID do evento deve ser gerado após salvamento");
    }

    @Test
    void testGetRefeicoesHoje() {
        String email = "teste@exemplo.com";
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(refeicaoRepository.findByUsuarioAndDataBetweenOrderByDataDesc(usuario, startOfDay, endOfDay))
                .thenReturn(Collections.emptyList());

        
    }
}