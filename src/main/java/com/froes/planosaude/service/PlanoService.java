package com.froes.planosaude.service;

import com.froes.planosaude.model.Evento;
import com.froes.planosaude.model.PlanoSaude;
import com.froes.planosaude.model.Refeicao;
import com.froes.planosaude.model.Treino;
import com.froes.planosaude.model.Usuario;
import com.froes.planosaude.model.UsuarioPeso;
import com.froes.planosaude.repository.EventoRepository;
import com.froes.planosaude.repository.PlanoSaudeRepository;
import com.froes.planosaude.repository.RefeicaoRepository;
import com.froes.planosaude.repository.TreinoRepository;
import com.froes.planosaude.repository.UsuarioPesoRepository;
import com.froes.planosaude.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlanoService {

    private static final Logger logger = LoggerFactory.getLogger(PlanoService.class);
    private static final double MAX_PESO = 500.0; // Configurable weight limit
    private static final double MIN_PESO = 0.0;  // Configurable weight minimum

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private RefeicaoRepository refeicaoRepository;

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioPesoRepository usuarioPesoRepository;

    @Autowired
    private PlanoSaudeRepository planoSaudeRepository;

    // Utility Methods
    /**
     * Retrieves a user by email, throwing an exception if not found.
     *
     * @param email User's email
     * @return Usuario entity
     * @throws IllegalArgumentException if user not found
     */
    private Usuario getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
    }

    /**
     * Validates date range, ensuring start is before end.
     *
     * @param start Start date
     * @param end   End date
     * @throws IllegalArgumentException if start is after end
     */
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim.");
        }
    }

    // Eventos
    /**
     * Fetches all events for a user within a default broad date range.
     *
     * @param email User's email
     * @return List of events
     */
    public List<Evento> getEventos(String email) {
        Usuario usuario = getUsuarioByEmail(email);
        LocalDateTime start = LocalDateTime.now().minusYears(1); // Configurable range
        LocalDateTime end = LocalDateTime.now().plusYears(1);
        validateDateRange(start, end);
        return eventoRepository.findByUsuarioAndStartBetweenOrderByStartDesc(usuario, start, end);
    }

    /**
     * Fetches events for a user within a specific date range.
     *
     * @param email User's email
     * @param start Start date
     * @param end   End date
     * @return List of events
     */
    public List<Evento> getEventosPorData(String email, LocalDateTime start, LocalDateTime end) {
        Usuario usuario = getUsuarioByEmail(email);
        validateDateRange(start, end);
        return eventoRepository.findByUsuarioAndStartBetweenOrderByStartDesc(usuario, start, end);
    }

    /**
     * Saves an event, validating its fields and user association.
     *
     * @param evento Event to save
     * @return Saved event
     * @throws IllegalArgumentException if validation fails
     */
    public Evento salvarEvento(Evento evento) {
        if (evento == null || evento.getUsuario() == null) {
            throw new IllegalArgumentException("Evento e usuário são obrigatórios.");
        }
        if (evento.getTitle() == null || evento.getTitle().isBlank()) {
            throw new IllegalArgumentException("Título do evento é obrigatório.");
        }
        if (evento.getStart() == null) {
            throw new IllegalArgumentException("Data de início do evento é obrigatória.");
        }
        validateDateRange(evento.getStart(), evento.getEnd());
        return eventoRepository.save(evento);
    }

    /**
     * Finds an event by its ID.
     *
     * @param id Event ID
     * @return Event entity
     * @throws IllegalArgumentException if not found
     */
    public Evento buscarEventoPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado: " + id));
    }

    /**
     * Deletes an event, ensuring it belongs to the authenticated user.
     *
     * @param id    Event ID
     * @param email User's email
     */
    @Transactional
    public void removerEvento(Long id, String email) {
        Evento evento = buscarEventoPorId(id);
        Usuario usuario = getUsuarioByEmail(email);
        if (!evento.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Evento não pertence ao usuário autenticado.");
        }
        eventoRepository.deleteById(id);
        logger.info("Evento removido com sucesso: ID {}", id);
    }

    // Refeições
    /**
     * Fetches all meals for a user within a default broad date range.
     *
     * @param email User's email
     * @return List of meals
     */
    public List<Refeicao> getRefeicoes(String email) {
        Usuario usuario = getUsuarioByEmail(email);
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now().plusYears(1);
        validateDateRange(start, end);
        return refeicaoRepository.findByUsuarioAndDataBetweenOrderByDataDesc(usuario, start, end);
    }

    /**
     * Fetches meals for a user within a specific date range.
     *
     * @param email User's email
     * @param start Start date
     * @param end   End date
     * @return List of meals
     */
    public List<Refeicao> getRefeicoesPorData(String email, LocalDateTime start, LocalDateTime end) {
        Usuario usuario = getUsuarioByEmail(email);
        validateDateRange(start, end);
        return refeicaoRepository.findByUsuarioAndDataBetweenOrderByDataDesc(usuario, start, end);
    }

    /**
     * Saves a meal, validating its fields and user association.
     *
     * @param refeicao Meal to save
     * @return Saved meal
     * @throws IllegalArgumentException if validation fails
     */
    public Refeicao salvarRefeicao(Refeicao refeicao) {
        if (refeicao == null || refeicao.getUsuario() == null) {
            throw new IllegalArgumentException("Refeição e usuário são obrigatórios.");
        }
        if (refeicao.getDescricao() == null || refeicao.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição da refeição é obrigatória.");
        }
        if (refeicao.getData() == null) {
            throw new IllegalArgumentException("Data da refeição é obrigatória.");
        }
        return refeicaoRepository.save(refeicao);
    }

    /**
     * Finds a meal by its ID.
     *
     * @param id Meal ID
     * @return Meal entity
     * @throws IllegalArgumentException if not found
     */
    public Refeicao buscarRefeicaoPorId(Long id) {
        return refeicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Refeição não encontrada: " + id));
    }

    /**
     * Deletes a meal, ensuring it belongs to the authenticated user.
     *
     * @param id    Meal ID
     * @param email User's email
     */
    @Transactional
    public void removerRefeicao(Long id, String email) {
        Refeicao refeicao = buscarRefeicaoPorId(id);
        Usuario usuario = getUsuarioByEmail(email);
        if (!refeicao.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Refeição não pertence ao usuário autenticado.");
        }
        refeicaoRepository.deleteById(id);
        logger.info("Refeição removida com sucesso: ID {}", id);
    }

    // Treinos
    /**
     * Fetches all workouts for a user within a default broad date range.
     *
     * @param email User's email
     * @return List of workouts
     */
    public List<Treino> getTreinos(String email) {
        Usuario usuario = getUsuarioByEmail(email);
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now().plusYears(1);
        validateDateRange(start, end);
        return treinoRepository.findByUsuarioAndDataBetweenOrderByDataDesc(usuario, start, end);
    }

    /**
     * Fetches workouts for a user within a specific date range.
     *
     * @param email User's email
     * @param start Start date
     * @param end   End date
     * @return List of workouts
     */
    public List<Treino> getTreinosPorData(String email, LocalDateTime start, LocalDateTime end) {
        Usuario usuario = getUsuarioByEmail(email);
        validateDateRange(start, end);
        return treinoRepository.findByUsuarioAndDataBetweenOrderByDataDesc(usuario, start, end);
    }

    /**
     * Saves a workout, validating its fields and user association.
     *
     * @param treino Workout to save
     * @return Saved workout
     * @throws IllegalArgumentException if validation fails
     */
    public Treino salvarTreino(Treino treino) {
        if (treino == null || treino.getUsuario() == null) {
            throw new IllegalArgumentException("Treino e usuário são obrigatórios.");
        }
        if (treino.getDescricao() == null || treino.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição do treino é obrigatória.");
        }
        if (treino.getData() == null) {
            throw new IllegalArgumentException("Data do treino é obrigatória.");
        }
        return treinoRepository.save(treino);
    }

    /**
     * Finds a workout by its ID.
     *
     * @param id Workout ID
     * @return Workout entity
     * @throws IllegalArgumentException if not found
     */
    public Treino buscarTreinoPorId(Long id) {
        return treinoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Treino não encontrado: " + id));
    }

    /**
     * Deletes a workout, ensuring it belongs to the authenticated user.
     *
     * @param id    Workout ID
     * @param email User's email
     */
    @Transactional
    public void removerTreino(Long id, String email) {
        Treino treino = buscarTreinoPorId(id);
        Usuario usuario = getUsuarioByEmail(email);
        if (!treino.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Treino não pertence ao usuário autenticado.");
        }
        treinoRepository.deleteById(id);
        logger.info("Treino removido com sucesso: ID {}", id);
    }

    // Histórico de Peso (UsuarioPeso)
    /**
     * Fetches weight history for a user.
     *
     * @param email User's email
     * @return List of weight records
     */
    public List<UsuarioPeso> getHistoricoPeso(String email) {
        Usuario usuario = getUsuarioByEmail(email);
        return usuarioPesoRepository.findByUsuarioOrderByDataRegistroDesc(usuario);
    }

    /**
     * Fetches weight history for a user within a specific date range.
     *
     * @param email User's email
     * @param start Start date
     * @param end   End date
     * @return List of weight records
     */
    public List<UsuarioPeso> getHistoricoPesoPorData(String email, LocalDateTime start, LocalDateTime end) {
        Usuario usuario = getUsuarioByEmail(email);
        validateDateRange(start, end);
        return usuarioPesoRepository.findByUsuarioAndDataRegistroBetweenOrderByDataRegistroDesc(usuario, start, end);
    }

    /**
     * Saves a weight record, validating its fields and user association.
     *
     * @param usuarioPeso Weight record to save
     * @return Saved weight record
     * @throws IllegalArgumentException if validation fails
     */
    public UsuarioPeso salvarPeso(UsuarioPeso usuarioPeso) {
        if (usuarioPeso == null || usuarioPeso.getUsuario() == null) {
            throw new IllegalArgumentException("Registro de peso e usuário são obrigatórios.");
        }
        if (usuarioPeso.getPeso() <= MIN_PESO || usuarioPeso.getPeso() > MAX_PESO) {
            throw new IllegalArgumentException(String.format("Peso deve estar entre %.1f e %.1f kg.", MIN_PESO, MAX_PESO));
        }
        if (usuarioPeso.getDataRegistro() == null) {
            usuarioPeso.setDataRegistro(LocalDateTime.now());
        }
        return usuarioPesoRepository.save(usuarioPeso);
    }

    /**
     * Deletes a weight record, ensuring it belongs to the authenticated user.
     *
     * @param id    Weight record ID
     * @param email User's email
     */
    @Transactional
    public void removerPeso(Long id, String email) {
        UsuarioPeso peso = usuarioPesoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro de peso não encontrado: " + id));
        Usuario usuario = getUsuarioByEmail(email);
        if (!peso.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Registro de peso não pertence ao usuário autenticado.");
        }
        usuarioPesoRepository.deleteById(id);
        logger.info("Registro de peso removido com sucesso: ID {}", id);
    }

    // Planos de Saúde (PlanoSaude)
    /**
     * Fetches health plans for a user by type.
     *
     * @param usuarioId User ID
     * @param tipo      Plan type
     * @return List of health plans
     */
    public List<PlanoSaude> getPlanosByUsuarioAndTipo(Long usuarioId, PlanoSaude.TipoPlano tipo) {
        logger.debug("Buscando planos do tipo {} para usuário ID: {}", tipo, usuarioId);
        return planoSaudeRepository.findByUsuarioIdAndTipo(usuarioId, tipo);
    }

    /**
     * Finds a health plan by its ID.
     *
     * @param id Plan ID
     * @return Health plan entity
     * @throws IllegalArgumentException if not found
     */
    public PlanoSaude buscarPlanoPorId(Long id) {
        return planoSaudeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado: " + id));
    }

    /**
     * Saves a health plan, validating its fields and user association.
     *
     * @param plano Health plan to save
     * @return Saved health plan
     * @throws IllegalArgumentException if validation fails
     */
    public PlanoSaude salvarPlano(PlanoSaude plano) {
        if (plano == null || plano.getUsuario() == null) {
            throw new IllegalArgumentException("Plano e usuário são obrigatórios.");
        }
        if (plano.getTipo() == null) {
            throw new IllegalArgumentException("Tipo do plano é obrigatório.");
        }
        return planoSaudeRepository.save(plano);
    }

    /**
     * Deletes a health plan, ensuring it belongs to the authenticated user.
     *
     * @param id    Plan ID
     * @param email User's email
     */
    @Transactional
    public void removerPlano(Long id, String email) {
        PlanoSaude plano = buscarPlanoPorId(id);
        Usuario usuario = getUsuarioByEmail(email);
        if (!plano.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Plano não pertence ao usuário autenticado.");
        }
        planoSaudeRepository.deleteById(id);
        logger.info("Plano removido com sucesso: ID {}", id);
    }

    /**
     * Registers a user's weight, creating a new weight record.
     *
     * @param usuario User entity
     * @param peso    Weight value
     */
    public void registrarPeso(Usuario usuario, Double peso) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário é obrigatório para registrar o peso.");
        }
        if (peso == null || peso <= MIN_PESO || peso > MAX_PESO) {
            throw new IllegalArgumentException(String.format("Peso deve estar entre %.1f e %.1f kg.", MIN_PESO, MAX_PESO));
        }
        UsuarioPeso usuarioPeso = new UsuarioPeso();
        usuarioPeso.setUsuario(usuario);
        usuarioPeso.setPeso(peso);
        usuarioPeso.setDataRegistro(LocalDateTime.now());
        usuarioPesoRepository.save(usuarioPeso);
        logger.info("Peso registrado com sucesso para usuário ID {}: {} kg", usuario.getId(), peso);
    }
}