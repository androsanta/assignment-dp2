package S251579.test;

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


    String s = "";

    String[] split = s.split("/");

    System.out.println(split[split.length - 1]);















    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    Date d = null;
    try {
      d = format.parse("2018-01-12T19:08:41.428+01:00");
    } catch (ParseException e) {
      e.printStackTrace();
    }
    GregorianCalendar gg = new GregorianCalendar();
    gg.setTime(d);
    Client client = ClientBuilder.newClient();
    String uri = "http://192.168.1.5:8080/RnsSystem/rest";
    URI restUri = null;
    XMLGregorianCalendar xmlGregorianCalendar = null;
    try {
      xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gg);
      System.out.println(xmlGregorianCalendar.toString());
    } catch (DatatypeConfigurationException e) {
      e.printStackTrace();
    }

    restUri = UriBuilder.fromUri(uri)
      .path("rns")
      .path("vehicles")
      .queryParam("admin", true)
      .queryParam("since", xmlGregorianCalendar)
      .build();
    WebTarget target = client.target(restUri);
    Response response = target
      .request()
      .accept(MediaType.APPLICATION_XML)
      .get();

    Vehicles vehicles = response.readEntity(Vehicles.class);
    vehicles.setTotalPages(BigInteger.ONE);
    response.close();


















    /*Places places = new Places();

    ParkingAreaType parkingArea = new ParkingAreaType();
    parkingArea.setId("ciao");

    Gate gate = new Gate();
    gate.setId("come");

    places.getPlace().add(parkingArea);
    // places.getPlace().add(gate);

    PlaceType placeType = new PlaceType();
    placeType.setId("stao");
    places.getPlace().add(placeType);

    places.setPage(BigInteger.valueOf(1));
    places.setTotalPages(BigInteger.valueOf(100));

    try {
      // Instantiate JAXB context
      JAXBContext jaxbContext = JAXBContext.newInstance("it.polito.dp2.RNS.sol3.rest.service.jaxb");
      // Create Marshaller
      Marshaller m = jaxbContext.createMarshaller();
      // Format output in readable format
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      // Instantiate schema factory and add the custom validation schema
      SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
      Schema schema = sf.newSchema(new File("xsd/RnsSystem.xsd"));
      // m.setSchema(schema);
      // Marshal data into the file specified as argument
      m.marshal(places, System.out);

    } catch (JAXBException | org.xml.sax.SAXException e) {
      e.printStackTrace();
    }


    /*PathFinder pathFinder = null;
    try {
      PathFinderFactory pathFinderFactory = PathFinderFactory.newInstance();
      pathFinder = pathFinderFactory.newPathFinder();
      pathFinder.reloadModel();
      Set<List<String>> paths = pathFinder.findShortestPaths("SS0-S1", "SS0-S3", 0);
      paths.forEach(p -> p.forEach(System.out::println));
      paths = pathFinder.findShortestPaths("Wrong", "SS0-S3", 0);
      paths.forEach(p -> p.forEach(System.out::println));
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (pathFinder != null) {
        try {
          ((PathFinderImpl)pathFinder).unloadModel(ClientBuilder.newClient());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }*/
  }

}
