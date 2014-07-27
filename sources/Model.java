package sources;

import java.util.ArrayList;

// this class is a model like in MVC pattern. It is a singleton pattern.
// it contains all the data
public class Model
{
	// --- singleton pattern stuff ---
	private static Model INSTANCE = null;

	private Model() {
		players = new ArrayList<Player>();
	}

	public static Model getInstance() {
		if (INSTANCE == null)
			INSTANCE = new Model();
		return INSTANCE;
	}
	// --------------------------------

	private ArrayList<Player> players;
	public final int numberOfPlayerMax = 6;
	public int numberOfPlayer;

	public int getNumberOfPlayers() {
		return this.players.size();
	}
	public Player getPlayerNumber(int position) {
		return this.players.get(position);
	}
	public void addPlayer(Player player) {
		System.out.println("Added player");
		this.players.add(player);
	}
	public ArrayList<Player> getPlayers() {
		return this.players;
	}
}
