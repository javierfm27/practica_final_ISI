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
	
	//Aqui tendremos lo que siempre ira en nuestro html, como el titulo de la APP que se mantendra siempre, o podemos incluir
	//un elemento head con el elemento style, definiendo el fondo de la aplicacion por ejemplo
	private static final String HEAD = "<h1 style='color: red'>ANGELA JAVI APP</h1>";
	private static final String LINK_MAIN = "<span><a href='/'>Volver al Inicio</a></span>";
	
	
	//Metodo del main del HTML
	public static String mainHTML(Request req,Response resp) {
		return (HEAD
				+ "<br>"
				+ "<p> Aplicación Donde a partir de los datos de IMDB, podemos hacer varias cosas<p>"
				+ "<br>"
				+ "-> Para cargar los datos <a href='/data'> Haz click aqui</a>");
	}
	
	
	//Metodo que sirve el error
	public static String errorMessage(Request req,Response resp) {
		return(HEAD
				+ "<p> Ha habido un error con la carga de Datos " + LINK_MAIN + "</p>");
	}
	
	//Metodo que se encargara de crear una base de datos con los datos cargados
	public static String getData(Request req,Response resp){
		try {
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
					+ "fecha varchar(4),"
					+ "primary key(nombre,fecha))");
			stat.executeUpdate("create table actuaEn(NombreActor varchar(30),"
					+ "NombrePeli varchar(100),"
					+ "fechaPeli varchar(4),"
					+ "primary key(NombreActor,NombrePeli,fechaPeli),"
					+ "foreign key(NombreActor) references actores(Nombre),"
					+ "foreign key(NombrePeli) references peliculas(Nombre),"
					+ "foreign key(fechaPeli) references peliculas(fecha))");
			
			//Una vez creadas vamos a introducir lo datos de nuestro fichero a la base de datos.
			//Hemos optado por suprimir la opcion de que fichero subir, por complejidad y falta de tiempo
			In fileIn = new In("./data/imdb-data/cast.G.txt");
			
			
			
			//Obtenemos por linea los datos del .txt
			String line = fileIn.readLine();
			StringTokenizer lineTokens = new StringTokenizer(line,"/");
			//Primer token es la pelicula
			String film = lineTokens.nextToken();
			filmAnDate(film);

			
			
			fileIn.close();
			resp.redirect("/");
		}catch(SQLException | IllegalArgumentException ex) {
			resp.redirect("/error");
		}
		return("Los datos han sido cargados");
	}
	
	
	public static String[] filmAnDate(String film) {
		String[] filmSplit = film.split(" ");
		String date;
		for (String x : filmSplit) {
			if(x.matches(".\\d{4}.")) {
				date = x;
				break;
			}
			
		}
		String pelicula[] = film.split(".\\d{4}.");
		return [pelicula[0],date];
	}
	
	
	
	public static void main (String[] args) {
		port(getHerokuAssignedPort());
		try {
			//Inicio de Driver para base de datos
			conn = DriverManager.getConnection("jdbc:sqlite:database.db");	
			
			//Recursos de la APP
			get("/",Main::mainHTML);
			get("/data",Main::getData);
			get("/error",Main::errorMessage);
			
		}catch(SQLException | IllegalArgumentException ex){
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
	return 4567; // return default port if heroku-port isn't set (i.e. on localhost)
    }
}
