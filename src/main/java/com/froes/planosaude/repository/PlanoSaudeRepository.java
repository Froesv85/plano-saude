package com.froes.planosaude.repository;

import com.froes.planosaude.model.PlanoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanoSaudeRepository extends JpaRepository<PlanoSaude, Long> {
    List<PlanoSaude> findByUsuarioIdAndTipo(Long usuarioId, PlanoSaude.TipoPlano tipo);
    List<PlanoSaude> findByUsuarioIdAndAtivo(Long usuarioId, boolean ativo);
}
