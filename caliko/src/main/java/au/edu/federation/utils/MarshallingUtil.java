package au.edu.federation.utils;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import au.edu.federation.caliko.FabrikChain3D;

public class MarshallingUtil {
	
	private MarshallingUtil() {
		// 
	}
	
	public static void marshall(final FabrikChain3D chain, final OutputStream os) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(FabrikChain3D.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		m.marshal(chain, os);
	}

}
