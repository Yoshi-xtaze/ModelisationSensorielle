package td2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class MainClass extends JFrame{

	public MainClass() {
		super("Projet Modelisation - Logiciel de traitement d'image");
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
		try {
			System.setOut(new PrintStream(new File("sortie.txt")));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		new Modele();
	}

	public static void main(String[] args) {
		new MainClass();
	}

}
