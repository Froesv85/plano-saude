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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing health plan-related operations, including events, meals, workouts, weight history, and health plans.
 */
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
     * Retrieves a user by ID, throwing an exception if not found.
     *
     * @param usuarioId User's ID
     * @return Usuario entity
     * @throws IllegalArgumentException if user not found
     */
    private Usuario getUsuarioById(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        logger.debug("Buscando usuário por ID: {}", usuarioId);
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuarioId));
    }

    /**
     * Validates date range, ensuring start is before or equal to end.
     *
     * @param start Start date
     * @param end   End date
     * @throws IllegalArgumentException if start is after end
     */
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Data de início deve ser anterior ou igual à data de fim.");
        }
    }

    // Eventos
    /**
     * Fetches all events for a user within a default date range (past and future year).
     *
     * @param usuarioId User's ID
     * @return List of events
     */
    public List<Evento> getEventosByUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now().plusYears(1);
        validateDateRange(start, end);
        logger.debug("Buscando eventos para usuário ID {} entre {} e {}", usuarioId, start, end);
        return eventoRepository.findByUsuarioIdAndStartBetweenOrderByStartDesc(usuarioId, start, end);
    }

    /**
     * Fetches events for a user within a specific date range.
     *
     * @param usuarioId User's ID
     * @param start     Start date
     * @param end       End date
     * @return List of events
     */
    public List<Evento> getEventosByUsuarioAndData(Long usuarioId, LocalDateTime start, LocalDateTime end) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        validateDateRange(start, end);
        logger.debug("Buscando eventos para usuário ID {} entre {} e {}", usuarioId, start, end);
        return eventoRepository.findByUsuarioIdAndStartBetweenOrderByStartDesc(usuarioId, start, end);
    }

    /**
     * Fetches events for a user on a specific date (ignoring time).
     *
     * @param usuarioId User's ID
     * @param data      Date to filter events
     * @return List of events
     */
    public List<Evento> getEventosByUsuarioAndData(Long usuarioId, LocalDate data) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória.");
        }
        logger.debug("Buscando eventos para usuário ID {} na data {}", usuarioId, data);
        return eventoRepository.findByUsuarioIdAndStartDate(usuarioId, data);
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
        logger.info("Salvando evento para usuário ID {}: {}", evento.getUsuario().getId(), evento.getTitle());
        return eventoRepository.save(evento);
    }

    /**
     * Finds an event by its ID and user ID to ensure ownership.
     *
     * @param id        Event ID
     * @param usuarioId User's ID
     * @return Event entity
     * @throws IllegalArgumentException if not found or unauthorized
     */
    public Evento buscarEventoPorId(Long id, Long usuarioId) {
        if (id == null || usuarioId == null) {
            throw new IllegalArgumentException("ID do evento e usuário são obrigatórios.");
        }
        logger.debug("Buscando evento ID {} para usuário ID {}", id, usuarioId);
        return eventoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado ou não pertence ao usuário: " + id));
    }

    /**
     * Deletes an event, ensuring it belongs to the authenticated user.
     *
     * @param id        Event ID
     * @param usuarioId User's ID
     */
    @Transactional
    public void removerEvento(Long id, Long usuarioId) {
        Evento evento = buscarEventoPorId(id, usuarioId);
        eventoRepository.delete(evento);
        logger.info("Evento removido com sucesso: ID {}", id);
    }

    // Refeições
    /**
     * Fetches all meals for a user within a default date range.
     *
     * @param usuarioId User's ID
     * @return List of meals
     */
    public List<Refeicao> getRefeicoes(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now().plusYears(1);
        validateDateRange(start, end);
        logger.debug("Buscando refeições para usuário ID {} entre {} e {}", usuarioId, start, end);
        return refeicaoRepository.findByUsuarioIdAndDataBetweenOrderByDataDesc(usuarioId, start, end);
    }

    /**
     * Fetches meals for a user within a specific date range.
     *
     * @param usuarioId User's ID
     * @param start     Start date
     * @param end       End date
     * @return List of meals
     */
    public List<Refeicao> getRefeicoesByUsuarioAndDataRange(Long usuarioId, LocalDateTime start, LocalDateTime end) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        validateDateRange(start, end);
        logger.debug("Buscando refeições para usuário ID {} entre {} e {}", usuarioId, start, end);
        return refeicaoRepository.findByUsuarioIdAndDataBetweenOrderByDataDesc(usuarioId, start, end);
    }

    /**
     * Fetches meals for a user on a specific date (ignoring time).
     *
     * @param usuarioId User's ID
     * @param data      Date to filter meals
     * @return List of meals
     */
    public List<Refeicao> getRefeicoesByUsuarioAndData(Long usuarioId, LocalDate data) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória.");
        }
        logger.debug("Buscando refeições para usuário ID {} na data {}", usuarioId, data);
        return refeicaoRepository.findByUsuarioIdAndData(usuarioId, data);
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
        logger.info("Salvando refeição para usuário ID {}: {}", refeicao.getUsuario().getId(), refeicao.getDescricao());
        return refeicaoRepository.save(refeicao);
    }

    /**
     * Finds a meal by its ID and user ID to ensure ownership.
     *
     * @param id        Meal ID
     * @param usuarioId User's ID
     * @return Meal entity
     * @throws IllegalArgumentException if not found or unauthorized
     */
    public Refeicao buscarRefeicaoPorId(Long id, Long usuarioId) {
        if (id == null || usuarioId == null) {
            throw new IllegalArgumentException("ID da refeição e usuário são obrigatórios.");
        }
        logger.debug("Buscando refeição ID {} para usuário ID {}", id, usuarioId);
        return refeicaoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Refeição não encontrada ou não pertence ao usuário: " + id));
    }

    /**
     * Deletes a meal, ensuring it belongs to the authenticated user.
     *
     * @param id        Meal ID
     * @param usuarioId User's ID
     */
    @Transactional
    public void removerRefeicao(Long id, Long usuarioId) {
        Refeicao refeicao = buscarRefeicaoPorId(id, usuarioId);
        refeicaoRepository.delete(refeicao);
        logger.info("Refeição removida com sucesso: ID {}", id);
    }

    // Treinos
    /**
     * Fetches all workouts for a user.
     *
     * @param usuarioId User's ID
     * @return List of workouts
     */
    public List<Treino> getTreinosByUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        logger.debug("Buscando todos os treinos para usuário ID {}", usuarioId);
        return treinoRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Fetches workouts for a user on a specific date (ignoring time).
     *
     * @param usuarioId User's ID
     * @param data      Date to filter workouts
     * @return List of workouts
     */
    public List<Treino> getTreinosByUsuarioAndData(Long usuarioId, LocalDate data) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória.");
        }
        logger.debug("Buscando treinos para usuário ID {} na data {}", usuarioId, data);
        return treinoRepository.findByUsuarioIdAndData(usuarioId, data);
    }

    /**
     * Fetches workouts for a user within a specific date range.
     *
     * @param usuarioId User's ID
     * @param start     Start date
     * @param end       End date
     * @return List of workouts
     */
    public List<Treino> getTreinosByUsuarioAndDataRange(Long usuarioId, LocalDateTime start, LocalDateTime end) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        validateDateRange(start, end);
        logger.debug("Buscando treinos para usuário ID {} entre {} e {}", usuarioId, start, end);
        return treinoRepository.findByUsuarioIdAndDataBetweenOrderByDataDesc(usuarioId, start, end);
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
        logger.info("Salvando treino para usuário ID {}: {}", treino.getUsuario().getId(), treino.getDescricao());
        return treinoRepository.save(treino);
    }

    /**
     * Finds a workout by its ID and user ID to ensure ownership.
     *
     * @param id        Workout ID
     * @param usuarioId User's ID
     * @return Workout entity
     * @throws IllegalArgumentException if not found or unauthorized
     */
    public Treino buscarTreinoPorId(Long id, Long usuarioId) {
        if (id == null || usuarioId == null) {
            throw new IllegalArgumentException("ID do treino e usuário são obrigatórios.");
        }
        logger.debug("Buscando treino ID {} para usuário ID {}", id, usuarioId);
        return treinoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Treino não encontrado ou não pertence ao usuário: " + id));
    }

    /**
     * Deletes a workout, ensuring it belongs to the authenticated user.
     *
     * @param id        Workout ID
     * @param usuarioId User's ID
     */
    @Transactional
    public void removerTreino(Long id, Long usuarioId) {
        Treino treino = buscarTreinoPorId(id, usuarioId);
        treinoRepository.delete(treino);
        logger.info("Treino removido com sucesso: ID {}", id);
    }

    // Histórico de Peso
    /**
     * Fetches weight history for a user.
     *
     * @param usuarioId User's ID
     * @return List of weight records
     */
    public List<UsuarioPeso> getHistoricoPeso(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        logger.debug("Buscando histórico de peso para usuário ID {}", usuarioId);
        return usuarioPesoRepository.findByUsuarioIdOrderByDataRegistroDesc(usuarioId);
    }

    /**
     * Fetches weight history for a user within a specific date range.
     *
     * @param usuarioId User's ID
     * @param start     Start date
     * @param end       End date
     * @return List of weight records
     */
    public List<UsuarioPeso> getHistoricoPesoPorData(Long usuarioId, LocalDateTime start, LocalDateTime end) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        validateDateRange(start, end);
        logger.debug("Buscando histórico de peso para usuário ID {} entre {} e {}", usuarioId, start, end);
        return usuarioPesoRepository.findByUsuarioIdAndDataRegistroBetweenOrderByDataRegistroDesc(usuarioId, start, end);
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
        logger.info("Salvando peso para usuário ID {}: {} kg", usuarioPeso.getUsuario().getId(), usuarioPeso.getPeso());
        return usuarioPesoRepository.save(usuarioPeso);
    }

    /**
     * Deletes a weight record, ensuring it belongs to the authenticated user.
     *
     * @param id        Weight record ID
     * @param usuarioId User's ID
     */
    @Transactional
    public void removerPeso(Long id, Long usuarioId) {
        if (id == null || usuarioId == null) {
            throw new IllegalArgumentException("ID do registro de peso e usuário são obrigatórios.");
        }
        UsuarioPeso peso = usuarioPesoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Registro de peso não encontrado ou não pertence ao usuário: " + id));
        usuarioPesoRepository.delete(peso);
        logger.info("Registro de peso removido com sucesso: ID {}", id);
    }

    /**
     * Registers a user's weight, creating a new weight record.
     *
     * @param usuarioId User's ID
     * @param peso      Weight value
     */
    public void registrarPeso(Long usuarioId, Double peso) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        if (peso == null || peso <= MIN_PESO || peso > MAX_PESO) {
            throw new IllegalArgumentException(String.format("Peso deve estar entre %.1f e %.1f kg.", MIN_PESO, MAX_PESO));
        }
        Usuario usuario = getUsuarioById(usuarioId);
        UsuarioPeso usuarioPeso = new UsuarioPeso();
        usuarioPeso.setUsuario(usuario);
        usuarioPeso.setPeso(peso);
        usuarioPeso.setDataRegistro(LocalDateTime.now());
        usuarioPesoRepository.save(usuarioPeso);
        logger.info("Peso registrado com sucesso para usuário ID {}: {} kg", usuarioId, peso);
    }

    // Planos de Saúde
    /**
     * Fetches health plans for a user by type.
     *
     * @param usuarioId User's ID
     * @param tipo      Plan type
     * @return List of health plans
     */
    public List<PlanoSaude> getPlanosByUsuarioAndTipo(Long usuarioId, PlanoSaude.TipoPlano tipo) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do plano é obrigatório.");
        }
        logger.debug("Buscando planos do tipo {} para usuário ID: {}", tipo, usuarioId);
        return planoSaudeRepository.findByUsuarioIdAndTipo(usuarioId, tipo);
    }

    /**
     * Finds a health plan by its ID and user ID to ensure ownership.
     *
     * @param id        Plan ID
     * @param usuarioId User's ID
     * @return Health plan entity
     * @throws IllegalArgumentException if not found or unauthorized
     */
    public PlanoSaude buscarPlanoPorId(Long id, Long usuarioId) {
        if (id == null || usuarioId == null) {
            throw new IllegalArgumentException("ID do plano e usuário são obrigatórios.");
        }
        logger.debug("Buscando plano ID {} para usuário ID {}", id, usuarioId);
        return planoSaudeRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado ou não pertence ao usuário: " + id));
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
        logger.info("Salvando plano para usuário ID {}: {}", plano.getUsuario().getId(), plano.getTipo());
        return planoSaudeRepository.save(plano);
    }

    /**
     * Deletes a health plan, ensuring it belongs to the authenticated user.
     *
     * @param id        Plan ID
     * @param usuarioId User's ID
     */
    @Transactional
    public void removerPlano(Long id, Long usuarioId) {
        PlanoSaude plano = buscarPlanoPorId(id, usuarioId);
        planoSaudeRepository.delete(plano);
        logger.info("Plano removido com sucesso: ID {}", id);
    }
}