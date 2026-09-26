package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.AuthResponseDTO;
import pe.edu.upc.agrocrew.dto.LoginRequestDTO;
import pe.edu.upc.agrocrew.dto.RegistroRequestDTO;
import pe.edu.upc.agrocrew.dto.UsuarioResponseDTO;

public interface AuthService {

    UsuarioResponseDTO registrar(RegistroRequestDTO dto);

    AuthResponseDTO login(LoginRequestDTO dto);
}
