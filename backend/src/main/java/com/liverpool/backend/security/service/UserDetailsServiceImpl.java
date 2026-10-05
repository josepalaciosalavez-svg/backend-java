package com.liverpool.backend.security.service;

import com.liverpool.backend.model.Cliente;
import com.liverpool.backend.model.Usuario;
import com.liverpool.backend.repository.ClienteRepository;
import com.liverpool.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Buscar en colección de usuarios del sistema
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            return User.builder()
                    .username(usuario.getEmail())
                    .password(usuario.getPassword())
                    .authorities(
                            usuario.getRoles().stream()
                                    .map(rol -> new SimpleGrantedAuthority(rol.name()))
                                    .collect(Collectors.toList())
                    )
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(usuario.getStatus() == Usuario.StatusUsuario.INACTIVO)
                    .build();
        }

        // 2. Si no es usuario administrativo/operativo, buscar en clientes
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o cliente no encontrado con email: " + email));

        Set<Usuario.Rol> roles = cliente.getRoles() != null && !cliente.getRoles().isEmpty()
                ? cliente.getRoles()
                : Collections.singleton(Usuario.Rol.ROLE_CLIENTE);

        return User.builder()
                .username(cliente.getEmail())
                .password(cliente.getPassword() != null ? cliente.getPassword() : "")
                .authorities(
                        roles.stream()
                                .map(rol -> new SimpleGrantedAuthority(rol.name()))
                                .collect(Collectors.toList())
                )
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(cliente.getStatus() == Cliente.StatusCliente.INACTIVO)
                .build();
    }
}
