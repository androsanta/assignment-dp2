package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.sol1.jaxb.PlaceType;
import it.polito.dp2.RNS.sol1.jaxb.Rns;
import it.polito.dp2.RNS.sol1.jaxb.ServiceType;
import org.xml.sax.SAXException;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RnsReaderFactory extends it.polito.dp2.RNS.RnsReaderFactory {

  private RnsLib reader;
  private Rns rns;

  public static void main (String[] args) {
    //@TODO remove before submitting solution
    // System.setProperty("it.polito.dp2.RNS.sol1.RnsInfo.file", "output.xml");
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
    loadRns();

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
    } catch (JAXBException | SAXException e) {
      System.out.println("Caught JAXB Exception");
      throw new RnsReaderException(e);
    }
  }

  private void loadRns () {
    this.reader = new RnsLib();
    loadPlaces();
    createConnections();
    loadVehicles();
  }

  private void loadPlaces () {
    // Road Segments
    rns.getPlaces()
      .getRoadSegment()
      .forEach(rs -> reader.addPlace(
        new RoadSegment(
          rs.getId(),
          rs.getCapacity().intValue(),
          rs.getName(),
          rs.getRoadName())
      ));

    // Parking Areas
    rns.getPlaces()
      .getParkingArea()
      .forEach(pa -> reader.addPlace(
        new ParkingArea(
          pa.getId(),
          pa.getCapacity().intValue(),
          pa.getServices().getService()
            .stream()
            .map(ServiceType::getName)
            .collect(Collectors.toSet())
        )
      ));

    // Gates
    rns.getPlaces()
      .getGate()
      .forEach(g -> reader.addPlace(
        new Gate(
          g.getId(),
          g.getCapacity().intValue(),
          GateType.fromValue(g.getType().value())
        )
      ));
  }

  private void createConnections () {
    Set<PlaceType> placesType = new HashSet<>();

    placesType.addAll(rns.getPlaces().getRoadSegment());
    placesType.addAll(rns.getPlaces().getParkingArea());
    placesType.addAll(rns.getPlaces().getGate());

    placesType.forEach(placeType -> {
      PlaceReader place = reader.getPlace(placeType.getId());
      reader.addConnections(
        placeType.getId(),
        placeType.getConnections().getConnection()
          .stream()
          .map(c -> new Connection(place, reader.getPlace(c.getId())))
          .collect(Collectors.toSet())
      );
    });
  }

  private void loadVehicles () {
    rns.getVehicles().getVehicle()
      .forEach(v -> reader.addVehicle(
        new Vehicle(
          v.getId(),
          VehicleType.fromValue(v.getType().value()),
          v.getEntryTime().toGregorianCalendar(),
          reader.getPlace(v.getOrigin()),
          reader.getPlace(v.getDestination()),
          reader.getPlace(v.getPosition()),
          VehicleState.fromValue(v.getState().value())
        )
      ));
  }

}
