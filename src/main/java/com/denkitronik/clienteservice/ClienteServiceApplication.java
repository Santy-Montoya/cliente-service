package com.denkitronik.clienteservice;

import com.denkitronik.clienteservice.domain.entities.Region;
import com.denkitronik.clienteservice.domain.repositories.IRegionDao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClienteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClienteServiceApplication.class, args);
    }

    // Si no hay regiones (por ejemplo, en tests con import.sql vacío), crea una por defecto
    @Bean
    CommandLineRunner cargarRegionPorDefecto(IRegionDao regionDao) {
        return args -> {
            if (regionDao.count() == 0) {
                Region region = new Region();
                region.setNombre("América del Sur");
                regionDao.save(region);
            }
        };
    }
}