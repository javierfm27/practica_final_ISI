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
import java.util.ArrayList;
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
					+ "primary key(NombreActor,NombrePeli),"
					+ "foreign key(NombreActor) references actores(Nombre),"
					+ "foreign key(NombrePeli) references peliculas(Nombre))");
			
			//Una vez creadas vamos a introducir lo datos de nuestro fichero a la base de datos.
			//Hemos optado por suprimir la opcion de que fichero subir, por complejidad y falta de tiempo
			In fileIn = new In("./data/imdb-data/f1.txt");
			
			
			while (fileIn.hasNextLine()) {
				//Obtenemos por linea los datos del .txt0
				String line = fileIn.readLine();
				StringTokenizer lineTokens = new StringTokenizer(line,"/");
				//Primer token es la pelicula
				String film = lineTokens.nextToken();
				film = filmAnDate(film);
				while(lineTokens.hasMoreTokens()) {
					//Siguientes tokens -> Actores. Ahora guardamos los actores y sus relaciones con las peliculas
					addActor(film,lineTokens.nextToken());
				}
			}

			
			
			fileIn.close();
			resp.redirect("/");
		}catch(SQLException | IllegalArgumentException ex) {
			System.out.println(ex.getMessage());
			resp.redirect("/error");
		}
		return("Los datos han sido cargados");
	}
	
	//Inserta Actor en tabla Actores, e inserta la relacion de Pelicula y Actor
	public static void addActor(String film, String actor) throws SQLException {
		//Hay que comprobar que dicho actor no este en la base
		String searchActor = "SELECT * FROM actores WHERE Nombre=?";
		PreparedStatement pstatSearch = conn.prepareStatement(searchActor);
		pstatSearch.setString(1, actor);
		if(!pstatSearch.execute()) { 
			//Insertamos el actor
			String insertActor = "INSERT INTO actores(Nombre) VALUES (?)";
			PreparedStatement pstat = conn.prepareStatement(insertActor);
			pstat.setString(1, actor);
			pstat.executeUpdate();
		}
		//Insertamos la relacion
		String insertActuaEn = "INSERT INTO actuaEn(NombreActor, NombrePeli) VALUES (?,?)";
		PreparedStatement pstat2 = conn.prepareStatement(insertActuaEn);
		pstat2.setString(1, actor);
		pstat2.setString(2, film);
		pstat2.executeUpdate();
	}
	
	
	//Separa la pelicula en pelicula y Año de produccion y la inserta en la tabla, y devuelve la pelicula para usos futuros
	public static String filmAnDate(String film) throws SQLException {
		String[] filmSplit = film.split(" ");
		String date = "";
		for (String x : filmSplit) {
			if(x.matches(".\\d{4}.")) {
				date = x;
				break;
			}
			
		}
		String pelicula[] = film.split(".\\d{4}.");
		//Insertamos pelicula
		String insertFilm = "INSERT INTO peliculas(Nombre,fecha) VALUES (?,?)";
		PreparedStatement preparedStatem = conn.prepareStatement(insertFilm);
		preparedStatem.setString(1, pelicula[0]);
		preparedStatem.setString(2, date);
		preparedStatem.executeUpdate();
		return pelicula[0];
	}
	
	
	
	public static void main (String[] args) {
		port(getHerokuAssignedPort());
		try {
			//Inicio de Driver para base de datos
			conn = DriverManager.getConnection("jdbc:sqlite:database.db");	
			
			//Recursos de la APP
			get("/",Main::mainHTML);
			get("/data",Main::getData);
			
			//Mensajes De Error
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
