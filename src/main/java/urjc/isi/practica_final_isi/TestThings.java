package urjc.isi.practica_final_isi;

public class TestThings {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String pelicula = "101 Dalmatians (1996)";
		String [] particion = pelicula.split("[^(0-9)]");
		System.out.println(particion[0]);
	}

}
