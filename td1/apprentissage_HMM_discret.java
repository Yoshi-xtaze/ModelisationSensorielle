package td1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class apprentissage_HMM_discret {
	public String fichierDest;
	public ArrayList<String> motsApp;
	public ArrayList<String> motsRef;
	public ArrayList<String> motsTest;
	String affichageDistLeven = "";
	protected HashMap<String, HashMap<String, Double>> matriceTransition;
	protected double pSub;
	protected double pIns;
	protected double pOmi;
	
	public int NSUB=0;
	public int NINS=0;
	public int NOMI=0;
	protected HashMap<String, HashMap<String, Double>> alignements;
	protected HashMap<String, Integer> insertions;
	
	
	public apprentissage_HMM_discret(String modeleinit, String donneesApp, String modeleapp){
		try {
			System.setOut(new PrintStream(new File("apprentissage_HMM_discret.txt")));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		motsApp = new ArrayList<>();
		motsRef = new ArrayList<>();
		motsTest = new ArrayList<>();
		
		this.fichierDest = modeleapp;
		try {
			lectureFichierModele(modeleinit);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Lecture des fichiers suivants : "+modeleinit+" et "+donneesApp+" et "+modeleapp);
		try {
			lectureApp(donneesApp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//initiliasation hasmap des alignements
	
		

		
		for(int i = 0; i<motsApp.size();i++){
			String motTest = motsTest.get(i);
			String motRef = motsRef.get(i);
			String[] f1 = motTest.split(" ");
			String[] f2 = motRef.split(" ");
			
			double a = levenshteinCalcul(f1, f2);
			//System.out.println(motTest+" ->"+motRef+" = "+a);
			
		}
		miseAJour();
		ecrireFichierModele(modeleapp);
	}
	
	
	
	public void ecrireFichierModele(String fichierModele) {
		String listePhoneme = "2;9;@;e;E;o;O;a;i;u;y;a~;o~;e~;H;w;j;R;l;p;t;k;b;d;g;f;s;S;v;z;Z;m;n;J";
		String[] ordre = listePhoneme.split(";");
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fichierModele)));
			pw.println("Psub;Pins;Pomi");
			pw.println(pSub + ";" + pIns + ";" + pOmi);
			pw.println("#Une ligne par symbole de reference; une colonne par symbole de test");
			pw.println("  ;2;9;@;e;E;o;O;a;i;u;y;a~;o~;e~;H;w;j;R;l;p;t;k;b;d;g;f;s;S;v;z;Z;m;n;J");
			for (int i = 0; i < ordre.length; i++) {
				pw.print(ordre[i]);
				for (int j = 0; j < ordre.length; j++) {
					pw.print(";" + matriceTransition.get(ordre[i]).get(ordre[j]));
				}
				pw.print("\n");
			}
			pw.println("Proba insertions...");
			pw.print("<ins>");
			for (int i = 0; i < ordre.length; i++) {
				pw.print(";" + matriceTransition.get(ordre[i]).get("<ins>"));
			}
			pw.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	private void miseAJour() {
		
		double somme = 0;
		for (String map : matriceTransition.keySet()) {
			
			for (String map3 : matriceTransition.keySet()) {
				
				somme += alignements.get(map3).get(map)+1;
				
			}
			
			for (String map2 : matriceTransition.get(map).keySet()) {
				double rapport = 0;
				if(alignements.get(map).containsKey(map2)){
					
					//if(alignements.get(map).get(map2) !=0.0){
						//System.out.println("aligne \""+map+"\" \""+map2+"\" "+alignements.get(map2).get(map));
						//System.out.println("somme "+somme);
						//System.out.println("-----------------------------");
					//}
					rapport = alignements.get(map).get(map2)+1/somme;
					if(rapport==0)rapport = 0.001;
					//System.out.println("j'ai "+matriceTransition.get(map).get(map2));
					//System.out.println("je met "+rapport);
					matriceTransition.get(map).put(map2,rapport);
					///;
				}else{
					//System.out.println("le phon�me "+map2+" est pas dedans");
				}
				
				
			}
		}
		double sommeinser = 0;
		for (String map : insertions.keySet()) {
			sommeinser += insertions.get(map)+1;
		}
		for (String map : insertions.keySet()) {
			double rapport = insertions.get(map)/sommeinser;
			matriceTransition.get(map).put("<ins>",rapport);
		}
		this.pSub = (double)((double)NSUB+1.)/(double)((double)NSUB+(double)NINS+(double)NOMI+3.);
		this.pIns = (double)((double)NINS+1.)/(double)((double)NSUB+(double)NINS+(double)NOMI+3.);
		this.pOmi = (double)((double)NOMI+1.)/(double)((double)NSUB+(double)NINS+(double)NOMI+3.);
	}

	private void lectureApp(String donneesApp) throws IOException {
		String st = "";
		String[] separated;
		BufferedReader br;
		String tab = "	";
		// @SuppressWarnings("unused")
		// Pattern pattern = Pattern.compile(tab);
		br = new BufferedReader(new FileReader(donneesApp));
		System.out.print("Lecture des donnees app... \n");

		while ((st = br.readLine()) != null) {
			
			separated = st.split(tab);
			String motinfo = separated[0];

			List<String> matchList = new ArrayList<String>();
			List<String> matchList2 = new ArrayList<String>();
			Pattern regex = Pattern.compile("\\[(.*?)\\]");
			Matcher ref = regex.matcher(separated[1]);
			Matcher test = regex.matcher(separated[2]);

			while (ref.find()) {// Finds Matching Pattern in String
				matchList.add(ref.group(1));// Fetching Group from String
			}
			while (test.find()) {// Finds Matching Pattern in String
				matchList2.add(test.group(1));// Fetching Group from String
			}
			
			String motref = matchList.get(0);
			String mottest = matchList2.get(0);
			this.motsApp.add(motinfo);
			this.motsRef.add(motref);
			this.motsTest.add(mottest);
		}
		System.out.println("Termine.");
		br.close();
	}

	public static void main(String[] args) {
		String modeleinit = args[0];
		String donneesApp = args[1];
		String modeleapp = args[2];
		for (int i = 0; i < 10; i++) {
			new apprentissage_HMM_discret(modeleinit, donneesApp, modeleapp);
		}
		
	}
	
	private double getCsub(String phonemetest, String phonemeref) {
		return -Math.log(pSub) - Math.log(matriceTransition.get(phonemetest).get(phonemeref));
	}

	private double getCins(String phoneme1test) {
		return -Math.log(pIns) - Math.log(matriceTransition.get(phoneme1test).get("<ins>"));
	}

	private double getComi() {
		return -Math.log(pOmi);
	}

	public double distanceLevenshtein(String mot1, String f1, String mot2, String f2) {
		affichageDistLeven = "";
		affichageDistLeven += mot1 + " [" + f1 + "]";
		affichageDistLeven += " => " + mot2 + " [" + f2 + "] ";

		String[] t1 = f1.split(" ");
		String[] t2 = f2.split(" ");
		return levenshteinCalcul(t1, t2);
	}

	
	public double levenshteinCalcul(String[] f1, String[] f2) {
		int size1 = f1.length;
		int size2 = f2.length;

		double[][] tmp = new double[size1 + 1][size2 + 1];

		for (int i = 0; i <= size1; i++) {
			tmp[i][0] = i;
		}
		for (int j = 0; j <= size2; j++) {
			tmp[0][j] = j;
		}
		for (int i = 1; i <= size1; i++) {
			for (int j = 1; j <= size2; j++) {
				double m = getCsub(f1[i - 1], f2[j - 1]);
				double omi = tmp[i - 1][j] + getComi();
				double inser = tmp[i][j - 1] + getCins(f1[i - 1]);
				double sub = tmp[i - 1][j - 1] + m;
				//System.out.println("Cout entre "+f1[i - 1]+" et "+f2[j - 1]+" :omi "+omi+" inser "+inser+" sub "+sub);
				tmp[i][j] = Math.min(Math.min(omi, inser), sub);
			}
		}

		if (tmp[size1][size2] > 0) {
			affichageDistLeven += "Erreur " + tmp[size1][size2] + " <=> ";
		} else {
			affichageDistLeven += "Correct " + tmp[size1][size2] + " <=> ";
		}

		int j = 0;
		int i = 0;
		boolean fini = false;
		while (!fini) {
			double haut = 0;
			double droite = 0;
			double diago = 0;
			if (i == size1 && j == size2)
				break;
			if (i < size1 && j < size2)
				diago = tmp[i + 1][j + 1];
			else
				diago = 500;
			if (i <= size1 && j < size2)
				haut = tmp[i][j + 1];
			else
				haut = 500;
			if (i < size1 && j <= size2)
				droite = tmp[i + 1][j];
			else
				droite = 500;

			double min = Math.min(haut, Math.min(diago, droite));
			if (min == diago) {
				j++;
				i++;
				affichageDistLeven += " s(" + f1[i - 1] + "=>" + f2[j - 1] + ")";
				alignements.get(f2[j-1]).put(f1[i-1],alignements.get(f2[j-1]).get(f1[i-1])+1);
				NSUB++;
			} else if (min == haut) {
				j++;
				affichageDistLeven += " i(=>" + f2[j - 1] + ")";
				insertions.put(f2[j-1], insertions.get(f2[j-1])+1);
				NINS++;
			} else if (min == droite) {
				i++;
				affichageDistLeven += " o(=>" + f1[i - 1] + ")";
				NOMI++;
			} else
				System.out.println("erreur");
		}
		affichageDistLeven += "\n";
		return tmp[size1][size2];
	}
	
	private void lectureFichierModele(String fichierModele) throws IOException {
		String st = "";
		String[] separated;
		BufferedReader br;
		String tab = ";";
		br = new BufferedReader(new FileReader(fichierModele));
		System.out.print("Lecture du fichier modele HMM... ");

		br.readLine();
		String valeurproba = br.readLine();
		separated = valeurproba.split(tab);
		String psub = separated[0];
		String pins = separated[1];
		String pomi = separated[2];
		this.pSub = Double.parseDouble(psub);
		this.pIns = Double.parseDouble(pins);
		this.pOmi = Double.parseDouble(pomi);
		br.readLine();
		String coltableau = br.readLine();
		String[] separated2 = coltableau.split(tab);

		
		insertions = new HashMap<String, Integer>();
		matriceTransition = new HashMap<>();
		alignements = new HashMap<>();
		for (int i = 1; i < separated2.length; i++) {
			insertions.put(separated2[i], 0);
			HashMap<String, Double> matricetmp = new HashMap<>();
			HashMap<String, Double> alignementsTmp = new HashMap<>();
			for (int j = 1; j < separated2.length; j++) {
				matricetmp.put(separated2[j], 0.);
				alignementsTmp.put(separated2[j], 0.);
			}
			alignements.put(separated2[i], alignementsTmp);
			matriceTransition.put(separated2[i], matricetmp);
		}

		while ((st = br.readLine()) != null) {
			separated = st.split(tab);
			for (int i = 1; i < separated.length; i++) {
				matriceTransition.get(separated2[i]).put(separated[0], Double.parseDouble(separated[i]));
			}
		}
		System.out.println("Termine.");
		br.close();
	}
	
	

}
