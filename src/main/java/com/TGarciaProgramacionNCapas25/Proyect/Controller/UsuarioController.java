package com.TGarciaProgramacionNCapas25.Proyect.Controller;

import com.TGarciaProgramacionNCapas25.Proyect.ML.Colonia;
import com.TGarciaProgramacionNCapas25.Proyect.ML.Direccion;
import com.TGarciaProgramacionNCapas25.Proyect.ML.ErrorCM;
import com.TGarciaProgramacionNCapas25.Proyect.ML.Estado;
import com.TGarciaProgramacionNCapas25.Proyect.ML.Municipio;
import com.TGarciaProgramacionNCapas25.Proyect.ML.Pais;
import com.TGarciaProgramacionNCapas25.Proyect.ML.Result;
import com.TGarciaProgramacionNCapas25.Proyect.ML.Rol;
import com.TGarciaProgramacionNCapas25.Proyect.ML.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;

import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("usuario")
public class UsuarioController {

    @GetMapping("/Login")
    public String loginView() {
        return "Login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/usuario/Login";
    }
    
    @GetMapping("/admin/create")
    public String adminCreateView(HttpSession session, Model model){
        String role = (String) session.getAttribute("userRole");
        String token = (String) session.getAttribute("jwtToken");
        
        if(token== null || role == null)
            return "redirect:/usuario/Login";
        if(!"ADMINISTRADOR".equalsIgnoreCase(role)&& !"Administrador".equalsIgnoreCase(role))
            return "redirect:/usuario";
        model.addAttribute("userRole", role);
        return "UsuarioAdminCreate";
    }
    
    @PostMapping("/admin/register")
    public String adminRegister(@RequestParam String nombre,@RequestParam String username,
                                @RequestParam String email, @RequestParam String password,
                                HttpSession session, Model model){
        
        String role = (String) session.getAttribute("userRole");
        String token = (String) session.getAttribute("jwtToken");
        if(token == null || role == null)
            return "redirect:/usuario/Login";
        
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "Bearer " + token);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> body = new HashMap<>();
            body.put("nombre", nombre);
            body.put("username", username);
            body.put("email", email);
            body.put("password", password);
            
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, httpHeaders);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:8081/auth/register", 
                                httpEntity,
                                Map.class);
            if(responseEntity.getStatusCode().is2xxSuccessful()){
                model.addAttribute("OK", "Usuario creado y correo enviado.");
            }else{
                model.addAttribute("Error", "No se puede crear el usuario");
            }
              
        }catch(Exception ex){
            model.addAttribute("Error", "Error: " + ex.getLocalizedMessage());
        }
        return "UsuarioAdminCreate";
    }
    
    @GetMapping("/confirmacion-pendiente")
    public String confirmacionPendiente(HttpSession session, Model model){
        Integer pendingId = (Integer) session.getAttribute("pendingUserId");
        if (pendingId == null)
            return "redirect:/usuario/Login";
        model.addAttribute("pendingUserId", pendingId);
        return "ConfirmacionPendiente";
    }
    
    

    @PostMapping("/Login")
    public String login(@RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> credentials = new HashMap<>();
        credentials.put("userName", username);
        credentials.put("password", password);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:8081/auth/login",
                    credentials,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                String token = (String) body.get("token");
                String role = (String) body.get("rol");
                Integer idUsuario = (Integer) body.get("idUsuario");

                session.setAttribute("jwtToken", token);
                session.setAttribute("userRole", role);
                session.setAttribute("idUsuario", idUsuario);

                if ("Lector".equals(role)) {
                    return "redirect:/usuario/action/" + idUsuario;
                }

                return "redirect:/usuario";
            } else {
                model.addAttribute("error", "Credenciales incorrectas");
                return "Login";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Error al iniciar sesión: " + e.getMessage());
            return "Login";
        }
    }

    @GetMapping
    public String index(Model model, HttpSession session) {
        String token = (String) session.getAttribute("jwtToken");
        String role = (String) session.getAttribute("userRole");

        if (token == null || role == null) {
            return "redirect:/usuario/Login";
        }

        model.addAttribute("userRole", role);
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Result<List<Usuario>>> responseEntity
                    = restTemplate.exchange(
                            "http://localhost:8081/api/usuario",
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<Result<List<Usuario>>>() {
                    }
                    );

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                Result result = responseEntity.getBody();
                model.addAttribute("usuarioBusqueda", new Usuario());
                model.addAttribute("usuarios", result.correct ? result.object : null);
            } else {
                model.addAttribute("error", "Error al obtener usuarios");
            }

        } catch (Exception e) {
            session.removeAttribute("jwtToken");
            return "redirect:/usuario/Login";
        }

        return "UsuarioIndex";
    }

//    @GetMapping
//    public String Index(Model model) {
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        ResponseEntity<Result<List<Usuario>>> responseEntity = restTemplate.exchange("http://localhost:8081/api/usuario",
//                HttpMethod.GET,
//                HttpEntity.EMPTY,
//                new ParameterizedTypeReference<Result<List<Usuario>>>() {
//        });
//
//        if (responseEntity.getStatusCode() == HttpStatusCode.valueOf(200)) {
//            model.addAttribute("usuarioBusqueda", new Usuario());
//
//            Result result = responseEntity.getBody();
//
//            System.out.print(responseEntity.getBody());
//
//            if (result.correct) {
//                model.addAttribute("usuarios", result.object);
//            } else {
//                model.addAttribute("usuarios", null);
//            }
//        }
//
//        return "UsuarioIndex";
//    }
//    @PostMapping
//    public String Index(Model model, @ModelAttribute("usuarioBusqueda")Usuario usuarioBusqueda){
//        
//        Result result = usuarioDAOImplementation.UsuarioDireccionGetAll(usuarioBusqueda);
//        
//        model.addAttribute("usuarios", result.objects);
//        model.addAttribute("roles", rolDAOImplementation.GetAll().objects);
//        
//        return "UsuarioIndex";
//    }
    @GetMapping("/action/{idUsuario}")
    public String add(Model model, @PathVariable("idUsuario") int IdUsuario, HttpSession session) {

        //recuperamos token
        String token = (String) session.getAttribute("jwtToken");
        String role = (String) session.getAttribute("userRole");
        if (token == null) {
            return "redirect:/usuario/Login";
        }

        model.addAttribute("userRole", role);

        //Autorizaciones con el token el la cabeceras
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            if (IdUsuario == 0) {

                ResponseEntity<Result<List<Rol>>> responseRoles = restTemplate.exchange("http://localhost:8081/api/Rol",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<List<Rol>>>() {
                });

                ResponseEntity<Result<List<Pais>>> responsePaises = restTemplate.exchange("http://localhost:8081/api/pais",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<List<Pais>>>() {
                });

                if (responseRoles.getStatusCode() == HttpStatusCode.valueOf(200)) {
                    Result<List<Rol>> resultRoles = responseRoles.getBody();
                    if (resultRoles != null && resultRoles.correct) {
                        model.addAttribute("roles", resultRoles.object);
                    }
                }
                if (responsePaises.getStatusCode() == HttpStatusCode.valueOf(200)) {
                    Result<List<Pais>> resultPaises = responsePaises.getBody();
                    if (resultPaises != null && resultPaises.correct) {
                        model.addAttribute("paises", resultPaises.object);
                    }
                }
                Usuario usuario = new Usuario();

                model.addAttribute("usuario", usuario);

                return "UsuarioForm";

            } else {
                ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange("http://localhost:8081/api/usuario/" + IdUsuario,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<Usuario>>() {
                });

                ResponseEntity<Result<List<Rol>>> responseRoles = restTemplate.exchange("http://localhost:8081/api/Rol",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<List<Rol>>>() {
                });

                ResponseEntity<Result<List<Pais>>> responsePaises = restTemplate.exchange("http://localhost:8081/api/pais",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<List<Pais>>>() {
                });

                if (responseUsuario.getStatusCode() == HttpStatusCode.valueOf(200)) {
                    Result<Usuario> resultUsuario = responseUsuario.getBody();
                    if (resultUsuario != null && resultUsuario.correct) {
                        model.addAttribute("usuario", resultUsuario.object);
                    }
                }

                if (responseRoles.getStatusCode() == HttpStatusCode.valueOf(200)) {
                    Result<List<Rol>> resultRoles = responseRoles.getBody();
                    if (resultRoles != null && resultRoles.correct) {
                        model.addAttribute("roles", resultRoles.object);
                    }
                }
                if (responsePaises.getStatusCode() == HttpStatusCode.valueOf(200)) {
                    Result<List<Pais>> resultPaises = responsePaises.getBody();
                    if (resultPaises != null && resultPaises.correct) {
                        model.addAttribute("paises", resultPaises.object);
                    }
                }
                return "UsuarioDetail";
            }
        } catch (HttpClientErrorException ex) {
            System.out.println("Error HTTP: " + ex.getStatusCode());
            System.out.println("Respuesta del backend: " + ex.getResponseBodyAsString());
            session.removeAttribute("jwtToken");
            return "redirect:/usuario/Login";
        }

    }

    @GetMapping("formEditable")
    public String formEditable(
            @RequestParam int IdUsuario,
            @RequestParam(required = false) Integer IdDireccion,
            Model model, HttpSession session) {

        String token = (String) session.getAttribute("jwtToken");
        String role = (String) session.getAttribute("userRole");

        if (token == null || role == null) {
            return "redirect:/usuario/Login";
        }
        model.addAttribute("userRole", role);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer" + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
///---Editar Usuario------------
        if (IdDireccion == null || IdDireccion == -1) {
            try {
                ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange(
                        "http://localhost:8081/api/usuario/" + IdUsuario,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<Usuario>>() {
                }
                );

                if (responseUsuario.getStatusCode() == HttpStatus.OK) {
                    Result<Usuario> resultUsuario = responseUsuario.getBody();
                    if (resultUsuario != null && resultUsuario.correct) {
                        Usuario usuario = resultUsuario.object;

                        // REEMPLAZAR todas las direcciones con una que tenga ID = -1
                        usuario.setDirecciones(new ArrayList<>());
                        Direccion direccionUsuario = new Direccion();
                        direccionUsuario.setIdDireccion(-1);
                        usuario.getDirecciones().add(direccionUsuario);

                        model.addAttribute("usuario", usuario);
                    }
                }
            } catch (Exception e) {
                // Manejar error
                model.addAttribute("error", "Error al obtener usuario: " + e.getMessage());
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(IdUsuario);

                // Crear lista con una dirección que tenga ID -1
                usuario.setDirecciones(new ArrayList<>());
                Direccion direccionVacia = new Direccion();
                direccionVacia.setIdDireccion(-1);
                usuario.getDirecciones().add(direccionVacia);

                model.addAttribute("usuario", usuario);
            }

            // Cargar datos adicionales
            cargarDatosAdicionales(model, null, restTemplate, session);

            //-----------Agregar Dirección---------------------
        } else if (IdDireccion == 0) {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(IdUsuario);
            usuario.setDirecciones(new ArrayList<>());
            usuario.getDirecciones().add(new Direccion(0));
            model.addAttribute("usuario", usuario);

            // Llamar al método auxiliar pasando null como dirección
            cargarDatosAdicionales(model, null, restTemplate, session);

            //-----------Editar Dirección---------------------      
        } else {
            try {
                ResponseEntity<Result<Direccion>> responseDireccion = restTemplate.exchange(
                        "http://localhost:8081/api/Direccion/" + IdDireccion,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<Direccion>>() {
                }
                );

                if (responseDireccion.getStatusCode() == HttpStatus.OK) {
                    Result<Direccion> resultDireccion = responseDireccion.getBody();
                    if (resultDireccion != null && resultDireccion.correct) {
                        Usuario usuario = new Usuario();
                        usuario.setIdUsuario(IdUsuario);
                        usuario.setDirecciones(new ArrayList<>());
                        usuario.getDirecciones().add(resultDireccion.object);
                        model.addAttribute("usuario", usuario);

                        // Cargar datos adicionales (paises, etc.)
                        cargarDatosAdicionales(model, resultDireccion.object, restTemplate, session);
                    }
                }
            } catch (Exception e) {
                model.addAttribute("error", "Error al obtener dirección");
                return "redirect:/usuario";
            }
        }

        return "UsuarioForm";
    }

// Método para cargar datos adicionales
    private void cargarDatosAdicionales(Model model, Direccion direccion, RestTemplate restTemplate, HttpSession session) {

        String token = (String) session.getAttribute("jwtToken");
        String role = (String) session.getAttribute("userRole");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // Cargar países (siempre)
            ResponseEntity<Result<List<Pais>>> responsePaises = restTemplate.exchange(
                    "http://localhost:8081/api/pais",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Result<List<Pais>>>() {
            }
            );
            if (responsePaises.getStatusCode() == HttpStatus.OK
                    && responsePaises.getBody() != null
                    && responsePaises.getBody().correct) {
                model.addAttribute("paises", responsePaises.getBody().object);
            } else {
                model.addAttribute("paises", Collections.emptyList());
            }

            // Cargar estados (solo si hay una dirección válida con todos los datos necesarios)
            if (direccion != null && direccion.getColonia() != null
                    && direccion.getColonia().getMunicipio() != null
                    && direccion.getColonia().getMunicipio().getEstado() != null
                    && direccion.getColonia().getMunicipio().getEstado().getPais() != null) {

                int idPais = direccion.getColonia().getMunicipio().getEstado().getPais().getIdPais();

                ResponseEntity<Result<List<Estado>>> responseEstados = restTemplate.exchange(
                        "http://localhost:8081/api/Estado/" + idPais,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<List<Estado>>>() {
                }
                );
                if (responseEstados.getStatusCode() == HttpStatus.OK
                        && responseEstados.getBody() != null
                        && responseEstados.getBody().correct) {
                    model.addAttribute("estados", responseEstados.getBody().object);
                } else {
                    model.addAttribute("estados", Collections.emptyList());
                }
            } else {
                // Para nueva dirección o dirección sin datos completos, cargar estados vacíos
                model.addAttribute("estados", Collections.emptyList());
            }

            // Cargar municipios (solo si hay estado)
            if (direccion != null && direccion.getColonia() != null
                    && direccion.getColonia().getMunicipio() != null
                    && direccion.getColonia().getMunicipio().getEstado() != null) {

                int idEstado = direccion.getColonia().getMunicipio().getEstado().getIdEstado();

                ResponseEntity<Result<List<Municipio>>> responseMunicipios = restTemplate.exchange(
                        "http://localhost:8081/api/Municipio/" + idEstado,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<List<Municipio>>>() {
                }
                );
                if (responseMunicipios.getStatusCode() == HttpStatus.OK
                        && responseMunicipios.getBody() != null
                        && responseMunicipios.getBody().correct) {
                    model.addAttribute("municipios", responseMunicipios.getBody().object);
                } else {
                    model.addAttribute("municipios", Collections.emptyList());
                }
            } else {
                model.addAttribute("municipios", Collections.emptyList());
            }

            // Cargar colonias (solo si hay municipio)
            if (direccion != null && direccion.getColonia() != null
                    && direccion.getColonia().getMunicipio() != null) {

                int idMunicipio = direccion.getColonia().getMunicipio().getIdMunicipio();

                ResponseEntity<Result<List<Colonia>>> responseColonias = restTemplate.exchange(
                        "http://localhost:8081/api/colonia/" + idMunicipio,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<Result<List<Colonia>>>() {
                }
                );
                if (responseColonias.getStatusCode() == HttpStatus.OK
                        && responseColonias.getBody() != null
                        && responseColonias.getBody().correct) {
                    model.addAttribute("colonias", responseColonias.getBody().object);
                } else {
                    model.addAttribute("colonias", Collections.emptyList());
                }
            } else {
                model.addAttribute("colonias", Collections.emptyList());
            }

            // Cargar roles (siempre)
            ResponseEntity<Result<List<Rol>>> responseRoles = restTemplate.exchange(
                    "http://localhost:8081/api/Rol",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Result<List<Rol>>>() {
            }
            );
            if (responseRoles.getStatusCode() == HttpStatus.OK
                    && responseRoles.getBody() != null
                    && responseRoles.getBody().correct) {
                model.addAttribute("roles", responseRoles.getBody().object);
            } else {
                model.addAttribute("roles", Collections.emptyList());
            }

        } catch (Exception e) {
            System.err.println("Error al cargar datos adicionales: " + e.getMessage());
            // Inicializar listas vacías para evitar errores en la vista
            model.addAttribute("paises", Collections.emptyList());
            model.addAttribute("estados", Collections.emptyList());
            model.addAttribute("municipios", Collections.emptyList());
            model.addAttribute("colonias", Collections.emptyList());
            model.addAttribute("roles", Collections.emptyList());
        }
    }

    @PostMapping("add")
    public String Add(@ModelAttribute("usuario") Usuario usuario,
            BindingResult bindingResult,
            Model model,
            HttpSession session,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagen) {


        String token = (String) session.getAttribute("jwtToken");
        String role = (String) session.getAttribute("userRole");

        if (token == null) {
            return "redirect:/usuario/Login";
        }
        model.addAttribute("userRole", role);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();

        // Validaciones de formulario
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            return "UsuarioForm";
        }

        try {

            // EDITAR DIRECCIÓN EXISTENTE

            if (usuario.getIdUsuario() > 0
                    && usuario.getDirecciones() != null
                    && !usuario.getDirecciones().isEmpty()
                    && usuario.getDirecciones().get(0).getIdDireccion() > 0) {

                int idDireccion = usuario.getDirecciones().get(0).getIdDireccion();

                HttpEntity<Direccion> entity = new HttpEntity<>(usuario.getDirecciones().get(0), headers);

                ResponseEntity<Result<Direccion>> responseDireccion = restTemplate.exchange(
                        "http://localhost:8081/api/Direccion/" + idDireccion,
                        HttpMethod.PUT,
                        entity,
                        new ParameterizedTypeReference<Result<Direccion>>() {
                }
                );

                if (responseDireccion.getStatusCode() == HttpStatus.OK) {
                    Result<Direccion> resultDireccion = responseDireccion.getBody();
                    if (resultDireccion != null && resultDireccion.correct) {
                        System.out.println("Dirección actualizada correctamente");
                    }
                }
            } 
            // EDITAR USUARIO

            else if (usuario.getIdUsuario() > 0
                    && usuario.getDirecciones() != null
                    && !usuario.getDirecciones().isEmpty()
                    && usuario.getDirecciones().get(0).getIdDireccion() == -1) {

                if (imagen != null && !imagen.isEmpty()) {
                    String nombre = imagen.getOriginalFilename();
                    String extension = nombre.split("\\.")[1];
                    if (extension.equalsIgnoreCase("jpg")) {
                        byte[] bytes = imagen.getBytes();
                        String base64Image = Base64.getEncoder().encodeToString(bytes);
                        usuario.setImagen(base64Image);
                    }
                }

                HttpEntity<Usuario> entity = new HttpEntity<>(usuario, headers);

                ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange(
                        "http://localhost:8081/api/usuario/" + usuario.getIdUsuario(),
                        HttpMethod.PUT,
                        entity,
                        new ParameterizedTypeReference<Result<Usuario>>() {
                }
                );

                if (responseUsuario.getStatusCode() == HttpStatus.OK) {
                    Result<Usuario> resultUsuario = responseUsuario.getBody();
                    if (resultUsuario != null && resultUsuario.correct) {
                        model.addAttribute("usuario", resultUsuario.object);
                        System.out.println("Usuario actualizado correctamente");
                    }
                }
            } 
            //  AGREGAR NUEVO USUARIO + DIRECCIÓN

            else if (usuario.getIdUsuario() == 0
                    && usuario.getDirecciones() != null
                    && !usuario.getDirecciones().isEmpty()
                    && usuario.getDirecciones().get(0).getIdDireccion() == 0) {

                if (imagen != null && !imagen.isEmpty()) {
                    String nombre = imagen.getOriginalFilename();
                    String extension = nombre.split("\\.")[1];
                    if (extension.equalsIgnoreCase("jpg")) {
                        byte[] bytes = imagen.getBytes();
                        String base64Image = Base64.getEncoder().encodeToString(bytes);
                        usuario.setImagen(base64Image);
                    }
                }

                HttpEntity<Usuario> entity = new HttpEntity<>(usuario, headers);

                ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange(
                        "http://localhost:8081/api/usuario",
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<Result<Usuario>>() {
                }
                );

                if (responseUsuario.getStatusCode() == HttpStatus.OK) {
                    Result<Usuario> resultUsuario = responseUsuario.getBody();
                    if (resultUsuario != null && resultUsuario.correct) {
                        System.out.println("Usuario agregado correctamente");
                    }
                }
            } 
            // AGREGAR DIRECCIÓN A UN USUARIO EXISTENTE

            else if (usuario.getIdUsuario() > 0
                    && usuario.getDirecciones() != null
                    && !usuario.getDirecciones().isEmpty()
                    && usuario.getDirecciones().get(0).getIdDireccion() == 0) {

                Usuario usuarioConDireccion = new Usuario();
                usuarioConDireccion.setIdUsuario(usuario.getIdUsuario());
                usuarioConDireccion.setDirecciones(usuario.getDirecciones());

                HttpEntity<Usuario> entity = new HttpEntity<>(usuarioConDireccion, headers);

                ResponseEntity<Result<Direccion>> responseDireccion = restTemplate.exchange(
                        "http://localhost:8081/api/Direccion",
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<Result<Direccion>>() {
                }
                );

                if (responseDireccion.getStatusCode() == HttpStatus.OK) {
                    Result<Direccion> resultDireccion = responseDireccion.getBody();
                    if (resultDireccion != null && resultDireccion.correct) {
                        System.out.println("Dirección agregada correctamente");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error al procesar la operación: " + e.getMessage());
            return "UsuarioForm";
        }

        return "redirect:/usuario";
    }

    //ELIMINAR USUARIO
    @GetMapping("delete/{IdUsuario}")
    public String Delete(@PathVariable("IdUsuario") int IdUsuario) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange("http://localhost:8081/api/usuario/" + IdUsuario,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Usuario>>() {
        });
        if (responseUsuario.getStatusCode() == HttpStatusCode.valueOf(200)) {
            return "redirect:/usuario";
        } else {

            System.out.println("Error al eliminar el usuario. Código de estado: " + responseUsuario.getStatusCode());
            return "redirect:/Usuario";
        }
    }

    //ELIMINAR DIRECCION
    @GetMapping("deleteDirection/{IdDireccion}/{IdUsuario}")
    public String Delete(@PathVariable("IdUsuario") int IdUsuario,
            @PathVariable("IdDireccion") int IdDireccion) {
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<Direccion>> responseDireccion = restTemplate.exchange("http://localhost:8081/api/Direccion" + IdUsuario + "/direcciones/" + IdDireccion,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Direccion>>() {
        });
        if (responseDireccion.getStatusCode() == HttpStatusCode.valueOf(200)) {
            return "redirect:/usuario";
        } else {

            System.out.println("Error al eliminar la direccion. Código de estado: " + responseDireccion.getStatusCode());
            return "redirect:/Usuario";
        }
    }

    RestTemplate restTemplate;

    @GetMapping("cargamasiva")
    public String CargaMasiva(Model model) {
        model.addAttribute("listarErrores", new ArrayList<>()); // lista vacía
        model.addAttribute("archivoCorrecto", null); // no hay archivo aún
        return "CargaMasiva";
    }

    @PostMapping("cargamasiva")
    public String CargaMasiva(@RequestParam("archivo") MultipartFile file,
            Model model, HttpSession session) {
        try {
            String urlServidor = "http://localhost:8081/usuarioapi/cargamasiva";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("archivo", new MultipartInputStreamFileResource(
//                    file.getInputStream(), file.getOriginalFilename()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(urlServidor, requestEntity, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                model.addAttribute("listarErrores", response.getBody().get("errores") != null
                        ? response.getBody().get("errores") : new ArrayList<>());
                model.addAttribute("archivoCorrecto", response.getBody().get("archivoCorrecto"));
                session.setAttribute("serverPath", response.getBody().get("path"));
            } else {
                model.addAttribute("listarErrores", new ArrayList<>());
                model.addAttribute("archivoCorrecto", false);
                model.addAttribute("mensaje", "Error al procesar archivo en servidor");
            }

        } catch (Exception e) {
            model.addAttribute("listarErrores", new ArrayList<>());
            model.addAttribute("archivoCorrecto", false);
            model.addAttribute("mensaje", "Error: " + e.getMessage());
        }

        return "CargaMasiva";
    }
}
//   
//    @GetMapping("cargamasiva/procesar")
//    public String CargaMasiva(HttpSession session) {
//
//
//}

//    @GetMapping("cargamasiva")
//    public String CargaMasiva() {
//        return "CargaMasiva";
//    }
//
//    @PostMapping("cargamasiva")
//    public String CargaMasiva(@RequestParam("archivo") MultipartFile file, Model model, HttpSession session) {
//
//        String root = System.getProperty("user.dir");
//        String rutaArchivo = "/src/main/resources/archivos/";
//        String fechaSubida = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmSS"));
//        String rutaFinal = root + rutaArchivo + fechaSubida + file.getOriginalFilename();
//
//        try {
//            file.transferTo(new File(rutaFinal));
//        } catch (Exception ex) {
//            System.out.println(ex.getLocalizedMessage());
//        }
//        if (file.getOriginalFilename().split("\\.")[1].equals("txt")) {
//            List<Usuario> usuarios = ProcesarTXT(new File(rutaFinal));
//            List<ErrorCM> errores = ValidarDatos(usuarios);
//
//            if (errores.isEmpty()) {
//                model.addAttribute("listarErrores", errores);
//                model.addAttribute("archivoCorrecto", true);
//                session.setAttribute("path", rutaFinal); //no regresa a la vista
//
//            } else {
//                model.addAttribute("listarErrores", errores);
//                model.addAttribute("archivoCorrecto", false);
//            }
//        } else {
//            //excel
//            List<Usuario> usuarios = ProcesarExcel(new File(rutaFinal));
//            List<ErrorCM> errores = ValidarDatos(usuarios);
//
//            if (errores.isEmpty()) {
//                model.addAttribute("listarErrores", errores);
//                model.addAttribute("archivoCorrecto", true);
//                session.setAttribute("path", rutaFinal);
//            } else {
//                model.addAttribute("listarErrores", errores);
//                model.addAttribute("archivoCorrecto", false);
//            }
//        }
//        return "CargaMasiva";
//    }
//
//    @GetMapping("cargamasiva/procesar")
//    public String CargaMasiva(HttpSession session) {
//        try {
//
//            String ruta = session.getAttribute("path").toString();
//
//            List<Usuario> usuarios;
//
//            if (ruta.split("\\.")[1].equals("txt")) {
//                System.out.println("soy un txt");
//                usuarios = ProcesarTXT(new File(ruta));
//            } else {
//                usuarios = ProcesarExcel(new File(ruta));
//            }
//
//            for (Usuario usuario : usuarios) {
//                usuarioDAOImplementation.UsuarioDireccionAdd(usuario);
//            }
//
//            session.removeAttribute("path");
//
//        } catch (Exception ex) {
//            System.out.println(ex.getLocalizedMessage());
//        }
//
//        return "redirect:/usuario";
//
//    }
//
//    private List<Usuario> ProcesarTXT(File file) {
//        try {
//            System.out.println("test");
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
//
//            String linea = "";
//            List<Usuario> usuarios = new ArrayList<>();
//            while ((linea = bufferedReader.readLine()) != null) {
//                System.out.println(linea);
//                String[] campos = linea.split("\\|");
//                Usuario usuario = new Usuario();
//                usuario.setNombre(campos[0]);
//                usuario.setApellidoPaterno(campos[1]);
//                usuario.setApellidoMaterno(campos[2]);
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//                Date fecha = campos[3] == "" ? null : format.parse(campos[3]);
//                usuario.setFechaNacimiento(fecha);
//                usuario.setCelular(campos[4]);
//                usuario.setTelefono(campos[5]);
//                usuario.setCURP(campos[6]);
//                usuario.setUserName(campos[7]);
//                usuario.setEmail(campos[8]);
//                usuario.setPassword(campos[9]);
//                usuario.setSexo(campos[10]);
//                usuario.Rol = new Rol();
//                usuario.Rol.setIdRol(Integer.parseInt(campos[11]));
//                usuario.Direcciones = new ArrayList();
//                Direccion direccion = new Direccion();
//                direccion.setIdDireccion(Integer.parseInt(campos[12]));
//                direccion.setCalle(campos[13]);
//                direccion.setNumeroInterior(campos[14]);
//                direccion.setNumeroExterior(campos[15]);
//                direccion.colonia = new Colonia();
//                direccion.colonia.setIdColonia(Integer.parseInt(campos[16]));
//
//                usuarios.add(usuario);
//                usuario.Direcciones.add(direccion);
//            }
//            System.out.println(usuarios.size());
//            return usuarios;
//        } catch (Exception ex) {
//            System.out.println(ex.getLocalizedMessage());
//            return null;
//        }
//    }
//
//    private List<Usuario> ProcesarExcel(File file) {
//
//        List<Usuario> usuarios = new ArrayList<>();
//        try {
//            XSSFWorkbook workbook = new XSSFWorkbook(file);
//            Sheet sheet = workbook.getSheetAt(0);
//            for (Row row : sheet) {
//                Usuario usuario = new Usuario();
//                usuario.setNombre(row.getCell(0) != null ? row.getCell(0).toString() : "");
//                usuario.setApellidoPaterno(row.getCell(1).toString());
//                usuario.setApellidoMaterno(row.getCell(2).toString());
//                DataFormatter dataFormatter = new DataFormatter();
//                usuario.setCelular(row.getCell(4) != null ? dataFormatter.formatCellValue(row.getCell(4)) : "");
//                usuario.setTelefono(row.getCell(5) != null ? dataFormatter.formatCellValue(row.getCell(5)) : "");
//
//                usuario.Rol = new Rol();
//                usuario.Rol.setIdRol(row.getCell(4) != null ? (int) row.getCell(3).getNumericCellValue() : 0);
//
//                usuarios.add(usuario);
//
//            }
//
//            return usuarios;
//        } catch (Exception ex) {
//            return null;
//        }
//    }
//
//    private List<ErrorCM> ValidarDatos(List<Usuario> usuarios) {
//
//        List<ErrorCM> errores = new ArrayList<>();
//
//        int linea = 1;
//        for (Usuario usuario : usuarios) {
//            System.out.println("test validar");
//            if (usuario.getNombre() == null || usuario.getNombre() == "") {
//                errores.add(new ErrorCM(linea, usuario.getNombre(), "Campo obligatorio"));
//            }
//            if (usuario.getApellidoPaterno() == null || usuario.getApellidoPaterno() == "") {
//                errores.add(new ErrorCM(linea, usuario.getApellidoPaterno(), "Apellido Paterno es Obligatorio"));
//            }
//            if (usuario.getApellidoMaterno() == null || usuario.getApellidoMaterno() == "") {
//                errores.add(new ErrorCM(linea, usuario.getApellidoMaterno(), "Apellido Materno es Obligatorio"));
//            }
//            if (usuario.getFechaNacimiento() == null || usuario.getFechaNacimiento().equals("")) {
//                errores.add(new ErrorCM(linea, "fecha vacia", "El campo Fecha de Nacimiento es obligatorio"));
//            }
//            if (usuario.getCelular() == null || usuario.getCelular().isEmpty()) {
//                errores.add(new ErrorCM(linea, "celular vacio", "El campo Número de Celular es obligatorio"));
//            } else if (!usuario.getCelular().matches("\\d{10}")) {
//                errores.add(new ErrorCM(linea, usuario.getCelular(), "El campo Número de Celular debe contener exactamente 10 dígitos numéricos"));
//            }
//            if (usuario.getTelefono() == null || usuario.getTelefono().isEmpty()) {
//                errores.add(new ErrorCM(linea, "telefono vacio", "El campo Número de Teléfono es obligatorio"));
//            } else if (!usuario.getTelefono().matches("\\d{10}")) {
//                errores.add(new ErrorCM(linea, usuario.getTelefono(), "El campo Número de Teléfono debe contener exactamente 10 dígitos numéricos"));
//            }
//            if (usuario.getCURP() == null || usuario.getCURP().isEmpty()) {
//                errores.add(new ErrorCM(linea, "curp vacia", "El campo CURP es obligatorio"));
//            } else if (!usuario.getCURP().matches("^[A-Z]{4}\\d{6}[H,M][A-Z]{5}[A-Za-z0-9]{2}$")) {
//                errores.add(new ErrorCM(linea, usuario.getCURP(), "El campo CURP debe contener exactamente 18 caracteres y seguir el formato correcto"));
//            }
//            if (usuario.getUserName() == null || usuario.getUserName() == "") {
//                errores.add(new ErrorCM(linea, "Sin Usuario", "Este campo es obligatorio"));
//            }
//            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
//                errores.add(new ErrorCM(linea, "email vacio", "El campo Email es obligatorio"));
//            } else if (!usuario.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
//                errores.add(new ErrorCM(linea, usuario.getEmail(), "El campo Email debe tener el formato correcto (ej. usuario@dominio.com)"));
//            }
//            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
//                errores.add(new ErrorCM(linea, "password vacia", "El campo Contraseña es obligatorio"));
//            } else if (!usuario.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$")) {
//                errores.add(new ErrorCM(linea, usuario.getPassword(), "La contraseña debe tener al menos 8 caracteres, incluyendo una mayúscula, una minúscula, un número y un carácter especial"));
//            }
//            if (usuario.getSexo() == null || usuario.getSexo().isEmpty()) {
//                errores.add(new ErrorCM(linea, "sexo vacio", "El campo Sexo es obligatorio"));
//            } else if (!usuario.getSexo().equalsIgnoreCase("M") && !usuario.getSexo().equalsIgnoreCase("H")) {
//                errores.add(new ErrorCM(linea, usuario.getSexo(), "El campo Sexo debe ser 'M' para Mujer o 'H' para Hombre"));
//            }
//            if (usuario.Rol.getIdRol() == 0) {
//                errores.add(new ErrorCM(linea, usuario.Rol.getIdRol() + "", "Numero de Rol no valido "));
//            }
//
//            linea++;
//        }
//
//        return errores;
//    }

