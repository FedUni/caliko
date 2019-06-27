package au.edu.federation.utils;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;

import au.edu.federation.caliko.FabrikChain;
import au.edu.federation.caliko.FabrikStructure;

/**
 * Utility class to serialize and de-serialize IK structures and chains.
 * 
 * @author alansley - 19/06/2019
 */
public class SerializationUtil
{
	
	private SerializationUtil() {
		// 
	}
	
	/**
	 * Serialized a FabrikChain to an OutputStream
	 * 
	 * @param 		<T>			The type of FabrikChain.
	 * @param 		chain		The FabrikChain to serialize.
	 * @param 		fos			The FileOutputStream to write the serialized data to.
	 *
	 * @throws		IOException Any problem writing the object to fail and we throw an exception.
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends FabrikChain> void serializeChain(final T chain, final FileOutputStream fos) throws IOException
	{
		try (ObjectOutputStream oos = new ObjectOutputStream(fos) )
		{
			oos.writeObject(chain);
			oos.flush();
		} // Auto-closed because of try-with-resources
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			throw ioe;
		}			
	}
	
	/**
	 * Unserializes a FabrikChain in binary format from an InputStream
	 * 
	 * @param 	<T>   	The type of FabrikChain.
	 * @param 	is 		The InputStream of the binary file to unserialize.
	 * @param 	clazz	The type of FabrikChain that must be unserialized.
	 *
	 * @return	The FabrikChain unmarshalled from the InputStream.
	 *
	 * @throws	Exception Any problem unserializing the FabrikChain and we bail.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends FabrikChain> T unserializeChain(final InputStream is, Class<T> clazz) throws Exception
	{		
		T chain = null;		
		ObjectInputStream ois = null;
		try
		{
			ois = new ObjectInputStream(is);
			chain = (T)ois.readObject();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}

		return chain;
	}

	/**
	 * Marshalls a FabrikStructure as XML to an OutputStream
	 * 
	 * @param	<T>			The type of FabrikStructure.
	 * @param	structure	The FabrikStructure to serialize.
	 * @param	fos			The FileOutputStream to write out the serialized data to.
	 *
	 * @throws	IOException Any problem marshalling the FabrikStructure and we bail.
	 */	
	@SuppressWarnings("rawtypes")
	public static <T extends FabrikStructure> void serializeStructure(final T structure, final FileOutputStream fos) throws IOException
	{	
		try (ObjectOutputStream oos = new ObjectOutputStream(fos) )
		{
			oos.writeObject(structure);
			oos.flush();
		} // Auto-closed because of try-with-resources
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			throw ioe;
		}				
	}
	
	

	/**
	 * Unserializes a FabrikStructure in binary form from an InputStream
	 * 
	 * @param	<T>			The type of FabrikStructure.
	 * @param	is			The InputStream of the binary data to unserialize from.
	 * @param	clazz		The type of FabrikStructure that must be unmarshalled
	 *
	 * @return				The FabrikStructure unserialized from the FileInputStream.
	 *
	 * @throws	Exception	Any problem unmarshalling the FabrikStructure and we bail.
	 */	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends FabrikStructure> T unserializeStructure(final InputStream is, Class<T> clazz) throws Exception
	{		
		T structure = null;		
		ObjectInputStream ois = null;
		try
		{
			ois = new ObjectInputStream(is);
			structure = (T)ois.readObject();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}

		return structure;
	}	
}
