package urjc.isi.practica_final_isi;

public class TestThings {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String pelicula = "101 Dalmatians (1996)";
		String[] peliculaSplit = pelicula.split(" ");
		String date = "";
		for (String x : peliculaSplit) {
			if(x.matches(".\\d{4}.")) {
				date = x;
				break;
			}
		}
		String[] film = pelicula.split(".\\d{4}.");
		System.out.println("Año: " + date + "Pelicula: " + film[0]);
	}

}
