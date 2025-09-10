
package com.TGarciaProgramacionNCapas25.Proyect.ML;

import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public class Usuario {
    
    private int IdUsuario;
    private String Nombre;
    private String ApellidoMaterno;
    private String ApellidoPaterno;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date FechaNacimiento;
    private String Celular;
    public String fechaStr;
    
    private String UserName;
    private String Email;
    private String Password;
    private String Sexo;
    private String Telefono;
    private String CURP;
    
    public Rol Rol;
    public List<Direccion>Direcciones;
    private String Imagen;
    private int Status;

    
    
    
    
    public Usuario(){}

    
    public void setIdUsuario(int idUsuario){
        this.IdUsuario=idUsuario;   
    } 
    public int getIdUsuario(){
        return this.IdUsuario;
    }
    public Usuario(String Nombre, String ApellidoPaterno, String ApellidoMaterno, Date FechaNacimiento, String Celular, String UserName, 
            String Email, String Password, String Sexo, String Telefono, String CURP, String Imagen, int Status, Rol rol){
        this.Nombre=Nombre;
        this.ApellidoPaterno=ApellidoPaterno;
        this.ApellidoMaterno=ApellidoMaterno;
        this.FechaNacimiento=FechaNacimiento;
        this.Celular=Celular;
        this.UserName=UserName;
        this.Email=Email;
        this.Sexo=Sexo;
        this.Password=Password;
        this.Telefono=Telefono;
        this.CURP=CURP;
        this.Imagen=Imagen;
        this.Status= Status;
        this.Rol= rol;
    }
    public void setNombre(String nombre){
        this.Nombre=nombre;
    }
    public String getNombre(){
        return Nombre;
    }
    public void setApellidoMaterno(String apellidoMaterno){
        this.ApellidoMaterno=apellidoMaterno;
    }
    public String getApellidoMaterno(){
        return ApellidoMaterno;
    }
    public void setApellidoPaterno( String apellidoPaterno){
        this.ApellidoPaterno=apellidoPaterno;
    }
    public String getApellidoPaterno(){
        return ApellidoPaterno;
    }
    public void setFechaNacimiento(Date fechaNacimiento){
        this.FechaNacimiento = fechaNacimiento;  
    }

    public Date getFechaNacimiento(){
        return FechaNacimiento;
    }
    
    public String getCelular() {
        return Celular;
    }

    public void setCelular(String Celular) {
        this.Celular = Celular;
    }
    public String getUserName(){
        return UserName;
    }
    public void setUserName(String userName){
        this.UserName = userName;
    }
    public String getEmail(){
        return Email;
    }
    public void setEmail(String email){
        this.Email = email;
    }
    public String getPassword(){
        return Password;
    }
    public void setPassword(String password){
        this.Password = password;
    }
    public String getSexo(){
        return Sexo;
    }
    public void setSexo(String sexo){
        this.Sexo = sexo;
    }
    public String getTelefono(){
        return Telefono;
    }
    public void setTelefono(String telefono){
        this.Telefono = telefono;          
    }
    public String getCURP(){
        return CURP;
    }
    public void setCURP(String curp){
        this.CURP = curp;
    }

    public List<Direccion> getDirecciones() {
        return Direcciones;
    }

    public void setDirecciones(List<Direccion> Direcciones) {
        this.Direcciones = Direcciones;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int Status) {
        this.Status = Status;
    }

    

    public Rol getRol() {
        return Rol;
    }

    public void setRol(Rol Rol) {
        this.Rol = Rol;
    }

    public String getImagen() {
        return Imagen;
    }

    public void setImagen(String Imagen) {
        this.Imagen = Imagen;
    }

    

   
    
    
   
    
    
}
