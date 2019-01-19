package S251579.test;

import it.polito.dp2.RNS.lab3.AdmClient;
import it.polito.dp2.RNS.lab3.AdmClientException;
import it.polito.dp2.RNS.lab3.AdmClientFactory;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicles;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class Test {

  public static void main (String args[]) {
    System.setProperty(
      "it.polito.dp2.RNS.lab2.URL",
      "http://192.168.1.5:7474/db"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab2.PathFinderFactory",
      "it.polito.dp2.RNS.sol2.PathFinderFactory"
    );
    System.setProperty(
      "it.polito.dp2.RNS.RnsReaderFactory",
      "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.seed",
      "1211530"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.testcase",
      "1"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab3.AdmClientFactory",
      "it.polito.dp2.RNS.sol3.admClient.AdmClientFactory"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab3.URL",
      "http://192.168.1.5:8080/RnsSystem/rest"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab3.VehClientFactory",
      "it.polito.dp2.RNS.sol3.vehClient.VehClientFactory"
    );




    try {
      AdmClient admClient = AdmClientFactory.newInstance().newAdmClient();
      System.out.println("\nPLACES ---------");
      admClient.getPlaces(null).forEach(p -> System.out.println(p.getId()));
      System.out.println("\nCONNECTIONS ---------");
      admClient.getConnections().forEach(c -> System.out.println("from " + c.getFrom().getId() + " to " + c.getTo().getId()));
      System.out.println("\nVEHICLES ---------");
      admClient.getUpdatedVehicles(null).forEach(v -> System.out.println(v.getId() + " " + v.getPosition().getId()));
    } catch (AdmClientException | ServiceException e) {
      e.printStackTrace();
    }

  }

}
