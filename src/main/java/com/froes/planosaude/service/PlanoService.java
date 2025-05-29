package com.froes.planosaude.service;

import com.froes.planosaude.model.Evento;
import com.froes.planosaude.model.Treino;
import com.froes.planosaude.model.Refeicao;
import com.froes.planosaude.model.UsuarioPeso;
import com.froes.planosaude.repository.EventoRepository;
import com.froes.planosaude.repository.TreinoRepository;
import com.froes.planosaude.repository.RefeicaoRepository;
import com.froes.planosaude.repository.UsuarioPesoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlanoService {

    private static final Logger logger = LoggerFactory.getLogger(PlanoService.class);

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private RefeicaoRepository refeicaoRepository;

    @Autowired
    private UsuarioPesoRepository usuarioPesoRepository;

    // Métodos para Eventos
    public List<Evento> getEventos() {
        return eventoRepository.findAll();
    }

    public List<Evento> getEventosPorData(LocalDateTime start, LocalDateTime end) {
        try {
            logger.debug("Buscando eventos entre {} e {}", start, end);
            List<Evento> eventos = eventoRepository.findByStartBetween(start, end);
            logger.debug("Encontrados {} eventos", eventos.size());
            return eventos;
        } catch (Exception e) {
            logger.error("Erro ao buscar eventos por data: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao buscar eventos por data", e);
        }
    }

    public Evento adicionarEvento(Evento evento) {
        return eventoRepository.save(evento);
    }

    public Evento editarEvento(Evento evento) {
        return eventoRepository.save(evento);
    }

    public void removerEvento(Long id) {
        eventoRepository.deleteById(id);
    }

    // Métodos para Treinos
    public List<Treino> getTreinos() {
        return treinoRepository.findAll();
    }

    public List<Treino> getTreinosPorData(LocalDateTime start, LocalDateTime end) {
        try {
            logger.debug("Buscando treinos entre {} e {}", start, end);
            List<Treino> treinos = treinoRepository.findByDataBetween(start, end);
            logger.debug("Encontrados {} treinos", treinos.size());
            return treinos;
        } catch (Exception e) {
            logger.error("Erro ao buscar treinos por data: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao buscar treinos por data", e);
        }
    }

    public List<Treino> getTreinosHoje() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(23, 59, 59);
        return getTreinosPorData(inicioDia, fimDia);
    }

    public Treino adicionarTreino(Treino treino) {
        return treinoRepository.save(treino);
    }

    // Métodos para Refeições
    public List<Refeicao> getRefeicoes() {
        return refeicaoRepository.findAll();
    }

    public List<Refeicao> getRefeicoesPorData(LocalDateTime start, LocalDateTime end) {
        try {
            logger.debug("Buscando refeições entre {} e {}", start, end);
            List<Refeicao> refeicoes = refeicaoRepository.findByDataBetween(start, end);
            logger.debug("Encontradas {} refeições", refeicoes.size());
            return refeicoes;
        } catch (Exception e) {
            logger.error("Erro ao buscar refeições por data: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao buscar refeições por data", e);
        }
    }

    public List<Refeicao> getRefeicoesHoje() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(23, 59, 59);
        return getRefeicoesPorData(inicioDia, fimDia);
    }

    public Refeicao adicionarRefeicao(Refeicao refeicao) {
        return refeicaoRepository.save(refeicao);
    }

    // Métodos para Peso
    public Double getPesoAtual() {
        UsuarioPeso ultimoPeso = usuarioPesoRepository.findLatestPeso();
        return ultimoPeso != null ? ultimoPeso.getPeso() : 85.0; // Valor padrão se não houver registro
    }

    public UsuarioPeso adicionarPeso(Double peso) {
        UsuarioPeso usuarioPeso = new UsuarioPeso(peso);
        return usuarioPesoRepository.save(usuarioPeso);
    }
}