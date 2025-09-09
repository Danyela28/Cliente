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
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("usuario")
public class UsuarioController {

    @GetMapping
    public String Index(Model model) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<List<Usuario>>> responseEntity = restTemplate.exchange("http://localhost:8081/usuarioapi",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<List<Usuario>>>() {
        });

        if (responseEntity.getStatusCode() == HttpStatusCode.valueOf(200)) {
            model.addAttribute("usuarioBusqueda", new Usuario());

            Result result = responseEntity.getBody();

            if (result.correct) {
                model.addAttribute("usuarios", result.object);
            } else {
                model.addAttribute("usuarios", null);
            }
        }

        return "UsuarioIndex";
    }

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
//
    @GetMapping("/action/{IdUsuario}")
    public String add(Model model, @PathVariable("IdUsuario") int IdUsuario) {

        if (IdUsuario == 0) {
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Result<List<Rol>>> responseRoles = restTemplate.exchange("http://localhost:8081/rolapi",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Rol>>>() {
            });

            ResponseEntity<Result<List<Pais>>> responsePaises = restTemplate.exchange("http://localhost:8081/paisapi",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
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

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange("http://localhost:8081/usuarioapi/" + IdUsuario,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Usuario>>() {
            });
            ResponseEntity<Result<List<Rol>>> responseRoles = restTemplate.exchange("http://localhost:8081/rolapi",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Rol>>>() {
            });

            ResponseEntity<Result<List<Pais>>> responsePaises = restTemplate.exchange("http://localhost:8081/paisapi",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
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
    }

    @GetMapping("formEditable")
    public String formEditable(
            @RequestParam int IdUsuario,
            @RequestParam(required = false) Integer IdDireccion,
            Model model) {

///---Editar Usuario------------
        if (IdDireccion == null) {
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange("http://localhost:8081/usuarioapi/" + IdUsuario,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Usuario>>() {
            });
            Result<Usuario> result = responseUsuario.getBody();
            if (result != null && result.correct) {
                Usuario usuario = result.object;
                if (usuario.getDirecciones() == null) {
                    usuario.setDirecciones(new ArrayList<>());
                }
                usuario.getDirecciones().add(new Direccion(-1));
            } else {
                Usuario usuario = new Usuario();
            }
            if (responseUsuario.getStatusCode() == HttpStatusCode.valueOf(200)) {
                Result<Usuario> resultUsuario = responseUsuario.getBody();
                if (resultUsuario != null && resultUsuario.correct) {
                    model.addAttribute("usuario", resultUsuario.object);
                }
            }

            //-----------Agregar Dirección---------------------
        } else if (IdDireccion == 0) {

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(IdUsuario);
            model.addAttribute("Usuario", usuario);

            // -----------Editar Dirección---------------------      
        } else {
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Result<Direccion>> responseDireccion = restTemplate.exchange("http://localhost:8081/direccionoapi/" + IdDireccion,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Direccion>>() {
            });
            Result<Direccion> resultDireccion = null;
            if (responseDireccion.getStatusCode() == HttpStatusCode.valueOf(200)) {
                resultDireccion = responseDireccion.getBody();
                if (resultDireccion != null && resultDireccion.correct) {
                    model.addAttribute("usuario", resultDireccion.object); //DUDA*************
                }
            }

            ResponseEntity<Result<List<Pais>>> responsePaises = restTemplate.exchange("http://localhost:8081/paisapi",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Pais>>>() {
            });
            if (responsePaises.getStatusCode() == HttpStatusCode.valueOf(200)) {
                Result<List<Pais>> resultPaises = responsePaises.getBody();
                if (resultPaises != null && resultPaises.correct) {
                    model.addAttribute("paises", resultPaises.object);
                }
            }
//  ESTADO
            
            ResponseEntity<Result<Estado>> responseEstados = restTemplate.exchange("http://localhost:8081/estadoapi/" + resultDireccion.object.colonia.Municipio.Estado.Pais.getIdPais(),
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Estado>>() {
            });
            if (responseEstados.getStatusCode() == HttpStatusCode.valueOf(200)) {
                Result<Estado> resultEstados = responseEstados.getBody();
                if (resultEstados != null && resultEstados.correct) {
                    model.addAttribute("estados", resultEstados.object);
                }
            }
//  MUNICIPIO
            ResponseEntity<Result<Municipio>> responseMunicipios = restTemplate.exchange("http://localhost:8081/municipioapi/" + resultDireccion.object.colonia.Municipio.Estado.getIdEstado(),
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Municipio>>() {
            });
            if (responseMunicipios.getStatusCode() == HttpStatusCode.valueOf(200)) {
                Result<Municipio> resultMunicipios = responseMunicipios.getBody();
                if (resultMunicipios != null && resultMunicipios.correct) {
                    model.addAttribute("municipios", resultMunicipios.object);
                }
            }
//  COLONIA
            ResponseEntity<Result<Colonia>> responseColonias = restTemplate.exchange("http://localhost:8081/coloniaapi/" + resultDireccion.object.colonia.Municipio.getIdMunicipio(),
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Colonia>>() {
            });
            if (responseColonias.getStatusCode() == HttpStatusCode.valueOf(200)) {
                Result<Colonia> resultColonias = responseColonias.getBody();
                if (resultColonias != null && resultColonias.correct) {
                    model.addAttribute("colonias", resultColonias.object);
                }
            }
//  ROLES
            ResponseEntity<Result<List<Rol>>> responseRoles = restTemplate.exchange("http://localhost:8081/rolapi",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<List<Rol>>>() {
            });
            if (responseRoles.getStatusCode() == HttpStatusCode.valueOf(200)) {
                Result<List<Rol>> resultRoles = responseRoles.getBody();
                if (resultRoles != null && resultRoles.correct) {
                    model.addAttribute("roles", resultRoles.object);
                }
            }
        }

            return "UsuarioForm";
        
    }

//    @PostMapping("add")
//    public String Add(@ModelAttribute("Usuario") Usuario usuario, BindingResult bindingResult,
//            Model model,
//            @RequestParam("imagenFile") MultipartFile imagen) {
//
//        if (bindingResult.hasErrors()) {
//            //llenar lo de paises
//            model.addAttribute("Usuario", usuario);
//            return "UsuarioForm";
//        } else {
//
//            if (usuario.getIdUsuario() > 0 && usuario.Direcciones.get(0).getIdDireccion() > 0) {
//                // Result result = direccionJPADAOImplementation.UpDate(usuario);
//                RestTemplate restTemplate = new RestTemplate();
//                ResponseEntity<Result<Direccion>> responseDireccion = restTemplate.exchange("http://localhost:8081/direccionapi/" + IdDireccion,
//                        HttpMethod.PUT,
//                        HttpEntity.EMPTY,
//                        new ParameterizedTypeReference<Result<Direccion>>() {
//                });
//                if (responseDireccion.getStatusCode() == HttpStatusCode.valueOf(200)) {
//                    Result<Direccion> resultDireccion = responseDireccion.getBody();
//                    if (resultDireccion != null && resultDireccion.correct) {
//                        model.addAttribute("usuario", resultDireccion.object); //DUDA*************
//                    }
//                }
//
//            } 
    @PostMapping("add")
    public String Add(@ModelAttribute("Usuario") Usuario usuario, BindingResult bindingResult,
                  Model model,
                  @RequestParam("imagenFile") MultipartFile imagen) {

    if (bindingResult.hasErrors()) {
        // Lógica para llenar los países si es necesario
        model.addAttribute("Usuario", usuario);
        return "UsuarioForm";
    } else {    
        if (usuario.getIdUsuario() > 0 && usuario.getDirecciones() != null && !usuario.getDirecciones().isEmpty()) {
            int IdDireccion = usuario.getDirecciones().get(0).getIdDireccion();

            RestTemplate restTemplate = new RestTemplate();
            
            // Se debe crear un objeto HttpEntity que contenga el cuerpo de la petición (la dirección a actualizar)
            HttpEntity<Direccion> requestEntity = new HttpEntity<>(usuario.getDirecciones().get(0));

            ResponseEntity<Result<Direccion>> responseDireccion = restTemplate.exchange(
                "http://localhost:8081/direccionapi/" + IdDireccion,
                HttpMethod.PUT,
                requestEntity, 
                new ParameterizedTypeReference<Result<Direccion>>() {}
            );

            if (responseDireccion.getStatusCode() == HttpStatusCode.valueOf(200)) {
                Result<Direccion> resultDireccion = responseDireccion.getBody();
                if (resultDireccion != null && resultDireccion.correct) {
                    model.addAttribute("usuario", usuario); 
                }
            } else {
                System.err.println("Error al actualizar la dirección. Código de estado: " + responseDireccion.getStatusCode());
                model.addAttribute("errorMessage", "Error al actualizar la dirección.");
            }
        
//        }else if (usuario.getIdUsuario() > 0 && usuario.Direcciones.get(0).getIdDireccion() == -1) {
//                if (imagen != null && imagen.getOriginalFilename() != "") {
//                    String nombre = imagen.getOriginalFilename();
//                    String extension = nombre.split("\\.")[1];
//                    if (extension.equals("jpg"))
//                            try {
//                        byte[] bytes = imagen.getBytes();
//                        String base64Image = Base64.getEncoder().encodeToString(bytes);
//                        usuario.setImagen(base64Image);
//                    } catch (Exception ex) {
//                        System.out.println("error");
//                    }
//                }
//
//                //Result result = usuarioJPADAOImplementation.UpDate(usuario);
//                RestTemplate restTemplate = new RestTemplate();
//
//                ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange("http://localhost:8081/usuarioapi/" + IdUsuario,
//                        HttpMethod.PUT,
//                        HttpEntity.EMPTY,
//                        new ParameterizedTypeReference<Result<Usuario>>() {
//                });
//                
//                if (responseUsuario.getStatusCode() == HttpStatusCode.valueOf(200)) {
//                    Result<Usuario> resultUsuario = responseUsuario.getBody();
//                    if (resultUsuario != null && resultUsuario.correct) {
//                        model.addAttribute("usuario", resultUsuario.object);
//                    }
//                }
//
           } else if (usuario.getIdUsuario() == 0 && usuario.Direcciones.get(0).getIdDireccion() == 0) {
                if (imagen != null && imagen.getOriginalFilename() != "") {
                    String nombre = imagen.getOriginalFilename();
                    String extension = nombre.split("\\.")[1];
                    if (extension.equals("jpg"))
                            try {
                        byte[] bytes = imagen.getBytes();
                        String base64Image = Base64.getEncoder().encodeToString(bytes);
                        usuario.setImagen(base64Image);
                    } catch (Exception ex) {
                        System.out.println("error");
                    }
                }

                //Result result = usuarioJPADAOImplementation.Add(usuario);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange("http://localhost:8081/usuarioapi",
                        HttpMethod.POST,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Result<Usuario>>() {
                });
                if (responseUsuario.getStatusCode() == HttpStatusCode.valueOf(200)) {
                    Result<Usuario> resultUsuario = responseUsuario.getBody();
                    if (resultUsuario != null && resultUsuario.correct) {
                        model.addAttribute("usuario", resultUsuario.object);
                    }
                }

            } else if (usuario.getIdUsuario() > 0 && usuario.Direcciones.get(0).getIdDireccion() == 0) {

                RestTemplate restTemplate = new RestTemplate();

                ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange("http://localhost:8081/direccionapi",
                        HttpMethod.POST,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Result<Usuario>>() {
                });
                if (responseUsuario.getStatusCode() == HttpStatusCode.valueOf(200)) {
                    Result<Usuario> result = responseUsuario.getBody();
                    if (result != null && result.correct) {

                        return "redirect:/usuario";
                    }
                }
            }
//        Result result = usuarioDAOImplementation.UsuarioDireccionAdd(usuario);
//        Result result = usuarioJPADAOImplementation.Add(usuario);
                return "redirect:/usuario";
            }
        }
    

    //ELIMINAR USUARIO
    @GetMapping("delete/{IdUsuario}")
    public String Delete(@PathVariable("IdUsuario") int IdUsuario) {

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Result<Usuario>> responseUsuario = restTemplate.exchange("http://localhost:8081/usuarioapi/" + IdUsuario,
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

        ResponseEntity<Result<Direccion>> responseDireccion = restTemplate.exchange("http://localhost:8081/direccionapi/" + IdDireccion,
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

//    
//
//    @GetMapping("getEstadosByIdPais/{IdPais}")
//    @ResponseBody
//    public Result EstadoByPais(@PathVariable int IdPais) {
//
//        return estadoDAOImplementation.EstadoByPais(IdPais);
//    }
//
//    @GetMapping("getMunicipiosByIdEstado/{IdEstado}")
//    @ResponseBody
//    public Result MunicipioByEstado(@PathVariable int IdEstado) {
//
//        return municipioDAOImplementation.MunicipioByEstado(IdEstado);
//    }
//
//    @GetMapping("getColoniasByIdMunicipio/{IdMunicipio}")
//    @ResponseBody
//    public Result ColoniaByMunicipio(@PathVariable int IdMunicipio) {
//
//        return coloniaJPADAOImplementation.ColoniaByMunicipio(IdMunicipio);
//    }
//
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
}
