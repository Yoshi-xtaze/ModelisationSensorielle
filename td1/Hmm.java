package td1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Hmm {

	HashMap<String, ArrayList<String>> motsTest;
	HashMap<String, ArrayList<String>> motsLexique;
	String affichageDistLeven = "";
	String affichageMotReconnu = "";

	protected double pSub;
	protected double pIns;
	protected double pOmi;

	protected HashMap<String, HashMap<String, Double>> matriceTransition;

	public Hmm(String fichierTest, LecteurDonnees ld) throws IOException {
		motsTest = ld.motsTest;
		motsLexique = ld.motsLexique;
		lectureFichierModele(fichierTest);
		ecrireFichierModele("modele_discret_final.dat");
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

		matriceTransition = new HashMap<>();

		for (int i = 1; i < separated2.length; i++) {
			HashMap<String, Double> matricetmp = new HashMap<>();
			for (int j = 1; j < separated2.length; j++) {
				matricetmp.put(separated2[j], 0.);
			}
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
			} else if (min == haut) {
				j++;
				affichageDistLeven += " i(=>" + f2[j - 1] + ")";
			} else if (min == droite) {
				i++;
				affichageDistLeven += " o(=>" + f1[i - 1] + ")";
			} else
				System.out.println("erreur");
		}
		affichageDistLeven += "\n";
		return tmp[size1][size2];
	}

	public void reconnaissanceLevenshtein() {
		double distanceLevenshtein;
		double minDistanceLevenshtein;
		String motReconnu;
		@SuppressWarnings("unused")
		String phonemesMotReconnu = "";
		int nombreErreur = 0;
		int nombreCorrect = 0;
		System.out.println("\nPour chaque action : s = substitution o = omission i = insertion \n");
		for (String motTest : this.motsTest.keySet()) {
			ArrayList<String> phonemesMotTest = new ArrayList<>();
			phonemesMotTest = this.motsTest.get(motTest);
			for (int i = 0; i < phonemesMotTest.size(); i++) {
				motReconnu = new String();
				phonemesMotReconnu = new String();
				minDistanceLevenshtein = Integer.MAX_VALUE;
				for (String motLexique : this.motsLexique.keySet()) {
					ArrayList<String> phonemesMotLexique = new ArrayList<>();
					phonemesMotLexique = this.motsLexique.get(motLexique);
					for (int j = 0; j < phonemesMotLexique.size(); j++) {
						if (!phonemesMotTest.get(i).isEmpty() && phonemesMotTest.get(i).length() != 0) {
							distanceLevenshtein = distanceLevenshtein(motTest, phonemesMotTest.get(i), motLexique,
									phonemesMotLexique.get(j));
							if (distanceLevenshtein < minDistanceLevenshtein) {
								minDistanceLevenshtein = distanceLevenshtein;
								motReconnu = motLexique;
								phonemesMotReconnu = phonemesMotLexique.get(j);
								affichageMotReconnu = affichageDistLeven;
							}
						}

					}
				}
				System.out.println(affichageMotReconnu);
				if (!motReconnu.equals(motTest)) {
					nombreErreur++;
				} else {
					nombreCorrect++;
				}
			}
		}
		System.out.println();
		System.out.println("Nombre d'erreur = " + nombreErreur + " et nombre correct = " + nombreCorrect);
		double tauxReconnaissance = ((double) nombreCorrect / ((double) nombreCorrect + (double) nombreErreur)) * 100.0;
		System.out.println("Taux de reconnaissance = " + tauxReconnaissance + " %");
	}

}
