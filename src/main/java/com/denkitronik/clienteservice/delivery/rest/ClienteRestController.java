package com.denkitronik.clienteservice.delivery.rest;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;
import com.denkitronik.clienteservice.domain.services.IClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/cliente-service")
@CrossOrigin(origins = {"http://localhost:4200"})
public class ClienteRestController {

    private final IClienteService clienteService;

    public ClienteRestController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.findAll());
    }

    @GetMapping("/clientes/regiones")
    public ResponseEntity<List<Region>> listarRegiones() {
        return ResponseEntity.ok(clienteService.findAllRegiones());
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> buscarCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.findById(id));
    }

    @PostMapping("/clientes")
    public ResponseEntity<Cliente> crearCliente(@Valid @RequestBody Cliente cliente) {
        Cliente nuevo = clienteService.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Long id,
                                                     @Valid @RequestBody Cliente cliente) {
        Cliente actualizado = clienteService.update(id, cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(actualizado);
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}