package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.sol1.jaxb.Rns;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;


public class RnsReaderFactory extends it.polito.dp2.RNS.RnsReaderFactory {

  public static void main (String[] args) {
    System.setProperty("it.polito.dp2.RNS.sol1.RnsInfo.file", "output.xml");
    RnsReaderFactory factory = new RnsReaderFactory();
    factory.newRnsReader();
  }

  @Override
  public RnsReaderSol newRnsReader () {
    String xmlOutput = System.getProperty("it.polito.dp2.RNS.sol1.RnsInfo.file");

    //@TODO handle system property not correctly set or file errors

    return new RnsReaderSol(getRns(xmlOutput));
  }

  private Rns getRns (String fileName) {

    try {
      // Instantiate JAXB context
      JAXBContext jaxbContext = JAXBContext.newInstance("it.polito.dp2.RNS.sol1.jaxb");
      // Create Unmarshaller
      Unmarshaller u = jaxbContext.createUnmarshaller();
      // Unmarshall and return value
      return (Rns)u.unmarshal(new File(fileName));
    } catch (JAXBException e) {
      System.out.println("Caught JAXB Exception");
      e.printStackTrace();
    }

    return new Rns();
  }

}
