package fr.atis_lab.physicalworld;

/**
 * Exception raised if a object with a wrong name is searched
 */
public class ObjectNameNotFoundException extends Exception {

	/**
	 * Raise an ObjectNameNotFoundException with a personalised message
	 */
	public ObjectNameNotFoundException(String message) {
		super(message);
	}

}
