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
				+ "<p>Debe subir una colección de datos para seguir: "
				+ "<form action='/upload' method='post' enctype='multipart/form-data'>" 
			    + "    <input type='file' name='uploaded_films_file' accept='.txt'>"
			    + "    <button>Upload file</button>" + "</form></p>");
	}
	
	
	
	//Metodo que se encargara de crear una base de datos con los datos cargados
	public static String loadDatabase(Request req,Response resp) {
		return (req.contentType());
	}
	
	
	
	
	
	
	
	
	public static void main (String[] args) {
		port(getHerokuAssignedPort());
		
		try {
			//Inicio de Driver para base de datos
			conn = DriverManager.getConnection("jdbc:sqlite:database.db");
			
		}catch(SQLException ex){
			System.out.println(ex.getMessage());
		}
		
		get("/",Main::mainHTML);
		post("/upload",Main::loadDatabase);
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
