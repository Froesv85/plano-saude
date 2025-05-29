package com.froes.planosaude.repository;

import com.froes.planosaude.model.UsuarioPeso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioPesoRepository extends JpaRepository<UsuarioPeso, Long> {

    @Query("SELECT up FROM UsuarioPeso up ORDER BY up.dataRegistro DESC LIMIT 1")
    UsuarioPeso findLatestPeso();
}