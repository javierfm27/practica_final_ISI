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

	//Con 
	private static Connection conn;	
	
	public static void main (String[] args) {
		port(getHerokuAssignedPort());
		
		//A partir del Main, donde procesaremos toda la aplicacion-> peticiones, posts, etc 
		//Aqui mostraremos las opciones de nuestra aplicacion
		get("/",(req,res) -> "Por ejemplo, vamos a probar con subir una base de datos"
				+ "<form action='/upload' method='post' enctype='multipart/form-data'>" 
			    + "    <input type='file' name='uploaded_films_file' accept='.txt'>"
			    + "    <button>Upload file</button>" + "</form>");
	
		post("/upload",(req,res)-> req.attributes());
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
