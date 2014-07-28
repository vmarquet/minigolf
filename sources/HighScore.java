package sources;

import java.util.Scanner;
import java.io.*;

public class HighScore
{
	public HighScore() {}

	public static int[] localHighScores;
	public static String[] localHighScoresNames; 
	public static int[] globalHighScores;
	public static String[] globalHighScoresNames; 
	
	// un peu sale parce que j'ai manqué de temps, à refaire proprement
	public static void getLocalHighScore() {
		Model model = Model.getInstance();

		localHighScores = new int[6];
		localHighScoresNames = new String[6];
		boolean[] tab = new boolean[model.numberOfPlayer];
		for (int i=0; i<model.numberOfPlayer; i++)
			tab[i] = false;  // we will put true when the player's score will be written in int[] highScores
		for (int i=0; i<model.numberOfPlayer; i++) {
			int jSave = -1;
			int min = 255;  // the best score is the minimal score (closest to par)
			for (int j=0; j<model.numberOfPlayer; j++) {
				if (tab[j] == true)  // score already in int[] highScores so we skip
			 		continue;
				if (model.getPlayerNumber(j).getTotalScore() < min ) {
					min = model.getPlayerNumber(j).getTotalScore();
					jSave = j;
				}
			}
			// now, we know who has the best score
			localHighScores[i] = min;
			localHighScoresNames[i] = model.getPlayerNumber(jSave).getName();
			tab[jSave] = true;
		}
	}

	// un peu sale parce que j'ai manqué de temps, à refaire proprement
	public static void getGlobalHighScore() {
		Model model = Model.getInstance();

		globalHighScores = new int[10];
		globalHighScoresNames = new String[10];
		try {
			FileReader file = new FileReader("./.score.txt");
			Scanner in = new Scanner(file);
			int j=0;
			for (int i=0; i<10; i++) {  // we keep 10 best scores
				String line = in.nextLine();
				String[] tab = line.split(":");
				while (localHighScores[j] < Integer.parseInt(tab[1]) && j<model.numberOfPlayer) {  // si on a un score local meilleur
					globalHighScoresNames[i] = localHighScoresNames[j];
					globalHighScores[i] = localHighScores[j];
					i++; j++;
				}
				if (i<10) {  // si on n'a pas atteint les 10 noms
					globalHighScoresNames[i] = tab[0];
					globalHighScores[i] = Integer.parseInt(tab[1]);
				}
			}
			in.close();
		}
		catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			System.out.println("file ./.score.txt not found");
			System.exit(-1);
		}

		// we record the high scores in the file:
		try {
			PrintWriter out = new PrintWriter("./.score.txt", "UTF-8");
			for (int i=0; i<10; i++) {
				out.println(globalHighScoresNames[i] + ":" + globalHighScores[i]);
			}
			out.close();
		}
		catch (IOException e) {
		
	System.out.println(e.getMessage());
			System.exit(-1);
		}
	}
}
