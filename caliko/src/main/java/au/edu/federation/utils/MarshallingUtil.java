package au.edu.federation.utils;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import au.edu.federation.caliko.FabrikChain;
import au.edu.federation.caliko.FabrikStructure;

/**
 * @author jsalvo
 */
public class MarshallingUtil {
	
	private MarshallingUtil() {
		// 
	}
	
	@SuppressWarnings("rawtypes")
	public static <T extends FabrikChain> void marshall(final T chain, final OutputStream os) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(chain.getClass());
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		m.marshal(chain, os);
	}

	@SuppressWarnings("rawtypes")
	public static <T extends FabrikStructure> void marshall(final T chain, final OutputStream os) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(chain.getClass());
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		m.marshal(chain, os);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends FabrikChain> T unmarshallChain(final InputStream is, Class<T> clazz) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller m = context.createUnmarshaller();
		
		T chain = (T) m.unmarshal(is);
		chain.updateChainLength();
		return chain;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends FabrikStructure> T unmarshallStructure(final InputStream is, Class<T> clazz) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller m = context.createUnmarshaller();
		
		T structure = (T) m.unmarshal(is);
		for(int i = 0; i < structure.getNumChains(); i++) {
			structure.getChain(i).updateChainLength();
		}
		return structure;
	}	
}
