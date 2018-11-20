package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.sol1.jaxb.Rns;
import org.xml.sax.SAXException;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class RnsReaderFactory extends it.polito.dp2.RNS.RnsReaderFactory {

  private RnsLib reader;
  private Rns rns;

  public static void main (String[] args) {
    System.setProperty("it.polito.dp2.RNS.sol1.RnsInfo.file", "output.xml");
    RnsReaderFactory factory = new RnsReaderFactory();

    try {
      factory.newRnsReader();
    } catch (RnsReaderException e) {
      System.out.println("Caught RnsReaderException");
      e.printStackTrace();
    }
  }

  @Override
  public RnsReader newRnsReader () throws RnsReaderException {
    String xmlOutput = System.getProperty("it.polito.dp2.RNS.sol1.RnsInfo.file");

    if (xmlOutput == null)
      throw new RnsReaderException("System property 'it.polito.dp2.RNS.sol1.RnsInfo.file' must be set");

    unmarshallRns(xmlOutput);

    return reader;
  }

  private void unmarshallRns (String fileName) throws RnsReaderException {

    try {
      // Instantiate JAXB context
      JAXBContext jaxbContext = JAXBContext.newInstance("it.polito.dp2.RNS.sol1.jaxb");
      // Create Unmarshaller
      Unmarshaller u = jaxbContext.createUnmarshaller();
      // Instantiate schema factory and add the custom validation schema
      SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
      Schema schema = sf.newSchema(new File("xsd/rnsInfo.xsd"));
      u.setSchema(schema);
      // Unmarshall and return value (safe cast because the file has been validated)
      rns = (Rns) u.unmarshal(new File(fileName));
    } catch (JAXBException e) {
      System.out.println("Caught JAXB Exception");
      throw new RnsReaderException(e);
    } catch (SAXException e) {
      System.out.println("Caught SAX Exception");
      throw new RnsReaderException(e);
    } catch (NullPointerException e) {
      System.out.println("Caught NULLPointer Exception");
      throw new RnsReaderException(e);
    }

  }

}
