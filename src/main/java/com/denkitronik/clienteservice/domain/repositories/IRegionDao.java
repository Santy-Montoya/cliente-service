package com.denkitronik.clienteservice.domain.repositories;

import com.denkitronik.clienteservice.domain.entities.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRegionDao extends JpaRepository<Region, Long> {
}