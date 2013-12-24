package fr.atis_lab.physicalworld;

/**
 * Exception raised if a wrong vertice list is passed to the PhysicalObject constructor
 */
public class InvalidPolygonException extends Exception {

	/**
	 * Raise an InvalidPolygonException with a personalised message
	 */
	public InvalidPolygonException(String message) {
		super(message);
	}

}
