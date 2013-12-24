package fr.atis_lab.physicalworld;

/**
 * Exception raised if you try to destroy an object during simulation step
 */
public class LockedWorldException extends Exception {

	/**
	 * Raise an LockedWorldException with a personalised message
	 */
	public LockedWorldException(String message) {
		super(message);
	}

}
