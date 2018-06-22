package urjc.isi.practica_final_isi;

import static spark.Spark.*;
import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import java.util.StringTokenizer;

import javax.servlet.MultipartConfigElement;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {

	 
	private static Connection conn;	
	
	
	//Metodo del main del HTML
	public static String mainHTML(Request req,Response resp) {
		return ("<h1 style='color: red'>ANGELA JAVI APP</h1>"
				+ "<br>"
				+ "<p> Aplicación Donde a partir de los datos de IMDB, podemos hacer varias cosas<p>"
				+ "<br>"
				+ "-> Para cargar los datos <a href='/data'> Haz click aqui</a>");
	}
	
	
	
	//Metodo que se encargara de crear una base de datos con los datos cargados
	public static String getData(Request req,Response resp) throws SQLException {
		System.out.println("Hola");
		//Preparamos SQL para crear nuestra Base de Datos
		Statement stat = conn.createStatement();
		
		//Antes de nada borramos las tablas, usadas anteriormente por si las tenemos guardadas de otras
		//conexiones
		stat.executeUpdate("drop table if exists actores");
		stat.executeUpdate("drop table if exists peliculas");
		stat.executeUpdate("drop table if exists actuaEn");
		
		//Procedemos a crear nuestra base de Datos, basandonos en un modelo de Entidad Relacion
		//Observando el diagrama creado para nuestra APP, tendremos las siguientes Tablas
		stat.executeUpdate("create table actores(Nombre varchar(30),"
				+ "primary key(Nombre))");
		stat.executeUpdate("create table peliculas(Nombre varchar(100),"
				+ "fecha INT,"
				+ "primary key(nombre,fecha))");
		stat.executeUpdate("create table actuaEn(NombreActor varchar(30),"
				+ "NombrePeli varchar(100),"
				+ "fechaPeli INT,"
				+ "primary key(NombreActor,NombrePeli,fechaPeli),"
				+ "foreign key(NombreActor) references actores(Nombre),"
				+ "foreign key(NombrePeli) references peliculas(Nombre),"
				+ "foreign key(fechaPeli) references peliculas(fecha))");
		return ("Tablas Creadas");
	}
	
	
	
	
	
	
	
	
	public static void main (String[] args) {
		port(getHerokuAssignedPort());
		try {
			//Inicio de Driver para base de datos
			conn = DriverManager.getConnection("jdbc:sqlite:database.db");	
			
			//Recursos de la APP
			get("/",Main::mainHTML);
			get("/data",Main::getData);
			
		}catch(SQLException ex){
			System.out.println("Ha habido un error");
			System.out.println(ex.getMessage());
		};
	}
	
	
	
	//Esencial para Web
    static int getHerokuAssignedPort() {
	ProcessBuilder processBuilder = new ProcessBuilder();
	if (processBuilder.environment().get("PORT") != null) {
	    return Integer.parseInt(processBuilder.environment().get("PORT"));
	}
	return 4560; // return default port if heroku-port isn't set (i.e. on localhost)
    }
}
