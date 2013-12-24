package fr.atis_lab.physicalworld;

import java.io.*;

public final class Serializer {

    /**
     * Private constructor. Serializer has no need to be instancied
     */
    private Serializer() {}

    /**
     * Use an ObjectOutputStream to write a Serializable object into a file.
     *
     * It is a static method, so you need to call it from the class.
     * Ex : Serializer.saveToFile("car.bak", c);
     *
     * @param  filename The name of the file where to save the seralized object
     * @param  object the object to serialize
     * @throws Potential I/O exception
     * @see Serializable
     * @see ObjectOutputStream
     */
    public static void saveToFile(String filename, Serializable object) throws IOException { // Potential I/O exception : FileNotFoundException, InvalidClassException, etc.
        FileOutputStream file = new FileOutputStream(filename); // OutputStream to the file
        ObjectOutputStream oos = new ObjectOutputStream(file); // Serializer
        oos.writeObject(object); // Write the serializable object to the stream
        oos.flush(); // Flush the stream (i.e transmitting all the bytes accumulated in the buffer)
        oos.close(); // Close the stream and the file
    }

    /**
     * Use an ObjectInputStream to load a Serializable object from a file.
     *
     * Warning : this method is generic, you need to specify the type of the object
     * during the call.
     * Ex : Car c = Serializer.<Car>loadFromFile("car.bak");
     *
     * @param  filename The name of the file where to save the seralized object
     * @return  object the object deserialized.
     * @throws Potential I/O exception
     * @see Serializable
     * @see ObjectInputStream
     */
    @SuppressWarnings("unchecked") // Remove the warning provoked by the unchecked cast
    public static <T> T loadFromFile(String filename) throws ClassNotFoundException, IOException {
        FileInputStream file = new FileInputStream(filename); //InputStream, reading from the file
        ObjectInputStream ois = new ObjectInputStream(file); // Deserializer
        return (T) ois.readObject(); //Read the serialized object and cast it to the wanted type
    }
}
