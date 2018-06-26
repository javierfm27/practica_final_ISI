package urjc.isi.practica_final_isi;

import static spark.Spark.*;
import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
	private static final String HEAD = "<head><style>footer{position: absolute; bottom: 0; margin-bottom: 3em;} </style></head><h1 style='color: red'>ANGELA JAVI APP</h1>";
	private static final String LINK_MAIN = "<span><a href='/main'>Volver al Inicio</a></span>";
	private static final String FOOTER = "<footer>© Developed by j.fernandezmor@alumnos.urjc.es & a.vargasa@alumnos.urjc.es</footer>";
	
	
	//Metodo del main del HTML
	public static String mainHTML(Request req,Response resp) {
		return (HEAD
				+ "<br>"
				+ "<p> Aplicación Donde a partir de los datos de IMDB, se ofrece: <p>"
				+ "<br>"
				+ "<ul>"
				+ "<li><a href='/buscaActor'>Buscar un actor.</a></li>"
				+ "<li><a href='/buscaPelicula'>Buscar una pelicula.</a></li>"
				+ "<li><a href='/buscaAño'>Buscar por año.</a></li>"
				+ "</ul>"
				+ "-> Para cargar los datos <a href='/data'> Haz click aqui</a>")
				+ FOOTER;
	}
	
	//Metodo del main con los datos cargados
		public static String mainData(Request req,Response resp) {
			return (HEAD
					+ "<br>"
					+ "<p> Aplicación Donde a partir de los datos de IMDB, se ofrece: <p>"
					+ "<br>"
					+ "<ul>"
					+ "<li><a href='/buscaActor'>Buscar un actor.</a></li>"
					+ "<li><a href='/buscaPelicula'>Buscar una pelicula.</a></li>"
					+ "<li><a href='/buscaAño'>Buscar por año.</a></li>"
					+ "</ul>")
					+ FOOTER;
		}
	
	//Metodo que sirve el error
	public static String errorMessage(Request req,Response resp) {
		return(HEAD
				+ "<p> Ha habido un error con la carga de Datos " + LINK_MAIN + "</p>" + FOOTER);
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
			resp.redirect("/main");
		}catch(SQLException | IllegalArgumentException ex) {
			System.out.println(ex.getMessage());
			resp.redirect("/error");
		}
		return("Los datos han sido cargados");
	}
	
	//Inserta Actor en tabla Actores, e inserta la relacion de Pelicula y Actor
	public static void addActor(String film, String actor) throws SQLException {
		//Hay que comprobar que dicho actor no este en la base
		String searchActor = "SELECT COUNT (*) AS 'Cuenta' FROM actores WHERE Nombre=?";
		PreparedStatement pstatSearch = conn.prepareStatement(searchActor);
		pstatSearch.setString(1, actor);
		ResultSet st = pstatSearch.executeQuery();
		Integer nactor = st.getInt("Cuenta");
		
		if(nactor == 0) {
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
	
	//Metodo que devuelve el menu de Buscar Actor y procesa la busqueda del actor
	public static String searchActorHTML(Request req,Response resp)throws SQLException {
		String html = (HEAD 
				+ "<h3>Encuentra a tu actor favorito</h3>"
				+ "<p><form action='/buscaActor' method='post'>"
				+ "Introduce tu actor favorito (Apellido, Nombre): <input type='text' name='actor'>" 
				+ "<input type='submit' value='Enviar'>"
				+ "</form></p>" + LINK_MAIN);
		if(req.requestMethod().equals(("GET"))) {
			html += FOOTER;
		}else{
			//Buscamos cuantos actores hay
			String actor = req.queryParams("actor");
			
			//Primero Se Mostrara si hay mas de una actor que coincide con nuestra busqueda
			String actorsCountQuery = "SELECT COUNT(*) AS 'Cuenta' FROM actores WHERE Nombre LIKE '%" + actor + "%'" ;
			PreparedStatement preparedStat = conn.prepareStatement(actorsCountQuery);
			ResultSet st = preparedStat.executeQuery();
			Integer nactors = st.getInt("Cuenta");
			
			if (nactors > 1) {
				String actorsQuery = "SELECT * FROM actores WHERE Nombre LIKE '%" + actor + "%'";
				PreparedStatement preparedStat2 = conn.prepareStatement(actorsQuery);
				ResultSet st2 = preparedStat2.executeQuery();
				String listadoActores = "<hr><p>Estos Actores coinciden con tu búsqueda. ¿Cuál es el/la que desea buscar?"
						+ "<ul>";
				while(st2.next()) {
					listadoActores += "<li>" + st2.getString("Nombre") + "</li>"; 
				}
				listadoActores += "</ul></p>";
				html += listadoActores;
				html += FOOTER;
			}else {
				String ActorCastQuery = "SELECT NombrePeli FROM actuaEn WHERE NombreActor LIKE '%" + actor + "%'";
				PreparedStatement preparedStat3 = conn.prepareStatement(ActorCastQuery);
				ResultSet st3 = preparedStat3.executeQuery();
				String peliculas = "<hr><p>Estos son las películas en las que aparece '" + actor + "' :"
						+ "<ul>";
				while(st3.next()) {
					peliculas += "<li>" + st3.getString("NombrePeli") + "</li>";
				}
				peliculas += "</ul></p>";
				html += peliculas;
				html += FOOTER;
			}			
		}
		return(html);
	}
	
	//Metodo que devuelve el menu de Buscar Año y procesa la busqueda de peliculas estrenadas ese año
	public static String searchYearHTML(Request req,Response resp)throws SQLException {
		String html = (HEAD 
				+ "<h3>¿Te acuerdas del año de estreno de tu película y no de su nombre? Introduce el año y encuéntrala </h3>"
				+ "<p><form action='/buscaAño' method='post'>"
				+ "Introduce el año de estreno: <input type='text' name='año'>" 
				+ "<input type='submit' value='Enviar'>"
				+ "</form></p>" + LINK_MAIN);
		if(req.requestMethod().equals(("GET"))) {
			html += FOOTER;
			return (html);
		}else{
			//Obtenemos el año
			String año = req.queryParams("año");
			
			try {
			//Buscamos las películas estrenadas en ese año
			String countYear = "SELECT COUNT (*) AS 'Cuenta' FROM peliculas WHERE fecha LIKE '%" + año + "%'";
			PreparedStatement preparedStat2 = conn.prepareStatement(countYear);
			ResultSet st2 = preparedStat2.executeQuery();
			Integer nfilms = st2.getInt("Cuenta");
			if (nfilms > 0) {
				String selectYear = "SELECT Nombre FROM peliculas WHERE fecha LIKE '%" + año + "%'";
				PreparedStatement preparedStat = conn.prepareStatement(selectYear);
				ResultSet st = preparedStat.executeQuery();
				String listadoPelis = "<hr><p>Estas películas fueron estrenadas en " + año + "<ul>";
				while(st.next()) {
					listadoPelis += "<li>" + st.getString("Nombre") + "</li>"; 
				}
				listadoPelis += "</ul></p>";
				html += listadoPelis;
				html += FOOTER;
			}else {
				html += "<hr>Este año no se encuentra en la base de datos. Por favor, pruebe con un nuevo año de búsqueda.";
				html += FOOTER;
			}
			return(html);
			}catch (Exception e) {
				System.out.println(e.getMessage());
				return "ERROR";
			}
		}

	}

	
	//Metodo informativo para cuando no existe el dato que se introduce
	public static String errorPost(Request req,Response resp) {
		return(HEAD
				+ "<p>Tu búsqueda no se encuentra en nuestra base de datos.</p>"
				+ LINK_MAIN);
	}
	
	//Metodo que muestra menu de buscar Pelicula, y procesa sus peticiones
	public static String searchFilm(Request req,Response resp) throws SQLException {
		String html = HEAD 
				+ "<h3>Encuentra tu pelicula y quien formo parte de ella </h3>"
				+ "<p><form action='/buscaPelicula' method='post'>"
				+ "Introduce Pelicula: <input type='text' name='pelicula'>" 
				+ "<input type='submit' value='Enviar'>"
				+ "</form></p>" + LINK_MAIN;
		if(req.requestMethod().equals("GET")) {
			html += FOOTER;
			return(html);
		}else {
			//Buscamos cuantass peliculas coincide con la peticion
			String pelicula = req.queryParams("pelicula");
			
			//Primero Se Mostrara si hay mas de una pelicula que coincide con nuestra busqueda
			String filmsCountQuery = "SELECT COUNT(*) AS 'Cuenta' FROM peliculas WHERE Nombre LIKE '%" + pelicula + "%'" ;
			PreparedStatement preparedStat = conn.prepareStatement(filmsCountQuery);
			ResultSet st = preparedStat.executeQuery();
			Integer nfilms = st.getInt("Cuenta");
			if (nfilms > 1) {
				String filmsQuery = "SELECT * FROM peliculas WHERE Nombre LIKE '%" + pelicula + "%'";
				PreparedStatement preparedStat2 = conn.prepareStatement(filmsQuery);
				ResultSet st2 = preparedStat2.executeQuery();
				String listadoFilms = "<hr><p>Estas peliculas coinciden con tu busqueda. ¿Cual es la que desea buscar?"
						+ "<ul>";
				while(st2.next()) {
					listadoFilms += "<li>" + st2.getString("Nombre") + "</li>"; 
				}
				listadoFilms += "</ul></p>";
				html += listadoFilms;
			}else {
				String filmCastQuery = "SELECT NombreActor FROM actuaEn WHERE NombrePeli LIKE '%" + pelicula + "%'";
				PreparedStatement preparedStat3 = conn.prepareStatement(filmCastQuery);
				ResultSet st3 = preparedStat3.executeQuery();
				String casting = "<hr><p>Estos son los actores que forman parte de '" + pelicula + "' :"
						+ "<ul>";
				while(st3.next()) {
					casting += "<li>" + st3.getString("NombreActor") + "</li>";
				}
				casting += "</ul></p>";
				html += casting;
			}
			html += FOOTER;
			return(html);
		}
	}
	
	public static void main (String[] args) {
		port(getHerokuAssignedPort());
		try {
			//Inicio de Driver para base de datos
			conn = DriverManager.getConnection("jdbc:sqlite:database.db");	
			
			//Recursos de la APP
			get("/",Main::mainHTML);
			get("/main",Main::mainData);
			get("/data",Main::getData);
			get("/buscaActor",Main::searchActorHTML);
			post("/buscaActor", Main::searchActorHTML);
			get("/buscaPelicula",Main::searchFilm);
			post("/buscaPelicula",Main::searchFilm);
			get("/buscaAño", Main::searchYearHTML);
			post("/buscaAño", Main::searchYearHTML);

			
			//Mensajes De Error
			get("/error",Main::errorMessage);
			get("/errorSearch",Main::errorPost);
			
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
	return 4560; // return default port if heroku-port isn't set (i.e. on localhost)
    }
}
