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
import java.util.Set;
import java.util.stream.Collectors;


public class RnsReaderFactory extends it.polito.dp2.RNS.RnsReaderFactory {

  private RnsReaderSol reader;
  private Rns rns;

  public static void main (String[] args) {
    System.setProperty("it.polito.dp2.RNS.sol1.RnsInfo.file", "output.xml");
    RnsReaderFactory factory = new RnsReaderFactory();
    factory.newRnsReader();
  }

  @Override
  public RnsReaderSol newRnsReader () {
    String xmlOutput = System.getProperty("it.polito.dp2.RNS.sol1.RnsInfo.file");

    // Create an empty reader
    reader = new RnsReaderSol();

    rns = unmarshallRns(xmlOutput);
    if (rns != null) {
      /* Load data from jaxb classes if unmarshal
       * and schema validation complete successfully */
      loadGates();
    }

    /* Always return the reader, which is empty
     * or loaded with data based on unmarshal result */
    return reader;
  }

  private Rns unmarshallRns (String fileName) {

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
      return (Rns) u.unmarshal(new File(fileName));
    } catch (JAXBException e) {
      System.out.println("Caught JAXB Exception");
      e.printStackTrace();
    } catch (SAXException e) {
      System.out.println("Caught SAX Exception");
      e.printStackTrace();
    } catch (NullPointerException e) {
      System.out.println("Caught NULLPointer Exception");
      e.printStackTrace();
    }

    return null;
  }

  private void loadGates () {
    Set<GateReader> gates = rns.getGates().getGate()
      .stream()
      .map(g -> new GateReaderSol(g.getId(), GateType.fromValue(g.getType().value())))
      .collect(Collectors.toSet());

    reader.getGates(null).addAll(gates);
  }

  private void loadVehicles () {
    Set<VehicleReader> vehicles; // @TODO load vehicles

    // reader.getVehicles(null, null, null).addAll(vehicles);
  }

}
