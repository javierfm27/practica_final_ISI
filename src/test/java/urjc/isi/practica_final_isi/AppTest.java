package urjc.isi.practica_final_isi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import org.junit.*;
import spark.Request;
import spark.Response;
/**
 * Unit test for simple App.
 */
public class AppTest {
	
	private static Connection conn;
	
	
	@Before
	public void initDB() throws SQLException{
		conn = DriverManager.getConnection("jdbc:sqlite:database.db");	
	}
	
	
    /**
     * Búsqueda de Película ""
     */
	@Test 
    public void nullFilm() throws SQLException{
		String pelicula = "";
		String query = "SELECT Nombre FROM peliculas";
		PreparedStatement st = conn.prepareStatement(query);
		ResultSet objetoResult = st.executeQuery();
		String listadoFilms = "<hr><p>Estas películas coinciden con tu búsqueda. ¿Cuál es la que desea buscar?"
				+ "<ul>";
		while(objetoResult.next()) {
			listadoFilms += "<li>" + objetoResult.getString("Nombre") + "</li>"; 
		}
		listadoFilms += "</ul></p>";
		assertEquals(listadoFilms,Main.selectFilm(pelicula, conn));
	}
	
	
	/**
	 * Fichero Incorrecto
	 */
	@Test(expected=IllegalArgumentException.class)
	public void notFile() {
		String filePath = "resources/data/f4.txt";
		In file = new In(filePath);
	}
	
	/**
	 * Búsqueda de elementos que no se encuentra en la base de datos
	 */
	@Test 
	public void searchNotFilm() throws SQLException{
		String peliculaNueva = "pepito";
		assertEquals("<p>'" + peliculaNueva + "' no se encuentra en la base de datos. Introduzca una nueva<p>",Main.selectFilm(peliculaNueva,conn));
	}
	
	@Test
	public void searchNotYear() throws SQLException {
		String año = "2020";
		String html = "<hr>Este año no se encuentra en la base de datos. Por favor, pruebe con un nuevo año de búsqueda.";
		assertEquals(html,Main.selectYear(año, conn));
	}
	
	@Test
	public void searchNotActor() throws SQLException {
		String actor = "Angela Javier Morata Vargas";
		String htmlExpected = "<hr>'" + actor + "' no se encuentra en la base de datos.Introduzca uno válido/a.";
		assertEquals(htmlExpected,Main.selectActor(actor, conn));

	}
	
	/**
	 * Tests de insertar elementos vacios 
	 */
	@Test(expected=SQLException.class)
	public void insertEmptyDate() throws SQLException {
		String pelicula = "Titanic"; //SIN FECHA
		Main.filmAnDate(pelicula,conn);
	}
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void insertEmptyFilm() throws SQLException {
		String pelicula = "(1995)"; //SIN PELICULA
		Main.filmAnDate(pelicula,conn);
	}
	@Test(expected=SQLException.class)
	public void insertEmptyActor() throws SQLException{
		String actor = "";
		String film = "Titanic";
		Main.addActor(film, actor,conn);
	}
	
	/**
	 * Test de Request = Null and Response = Null
	 */
	@Test(expected=NullPointerException.class)
	public void reqAndRespNull() throws SQLException{
		Request req = null;
		Response resp = null;
		Main.searchYearHTML(req, resp);
	}
}
