package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.RnsReaderFactory;
import it.polito.dp2.RNS.sol1.jaxb.*;
import it.polito.dp2.RNS.sol1.jaxb.GateType;
import it.polito.dp2.RNS.sol1.jaxb.VehicleType;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.stream.Collectors;

public class RnsInfoSerializer {

  private RnsReader reader;
  private Rns rns;

  public RnsInfoSerializer () throws RnsReaderException {
    RnsReaderFactory factory = RnsReaderFactory.newInstance();
    reader = factory.newRnsReader();
    rns = new Rns();
  }

  private static void setProperties () {
    System.setProperty(
      "it.polito.dp2.RNS.RnsReaderFactory",
      "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.seed",
      "1123956"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.testcase",
      "2"
    );
  }

  public static void main (String[] args) {
    //@TODO remove before submitting solution
    // setProperties();

    if (args.length != 1) {
      System.out.println("Specify an output file as argument");
      return;
    }

    final String output = args[0];

    try {
      RnsInfoSerializer rnsInfo = new RnsInfoSerializer();
      rnsInfo.serialize(output);
    } catch (RnsReaderException e) {
      System.out.println("Error while instantiating Rns Serializer");
      e.printStackTrace();
    }

  }

  private void serializePlace (PlaceType place, PlaceReader placeReader) {
    place.setId(placeReader.getId());
    place.setCapacity(BigInteger.valueOf(placeReader.getCapacity()));

    ConnectionsType connections = new ConnectionsType();
    connections.getConnection().addAll(
      placeReader.getNextPlaces()
        .stream()
        .map(nextPlace -> {
          ConnectionType connection = new ConnectionType();
          connection.setId(nextPlace.getId());
          return connection;
        })
        .collect(Collectors.toSet())
    );
    place.setConnections(connections);
  }

  private Set<RoadSegmentType> serializeRoadSegments () {
    return reader.getRoadSegments(null)
      .stream()
      .map(roadSegmentReader -> {
        RoadSegmentType roadSegment = new RoadSegmentType();

        serializePlace(roadSegment, roadSegmentReader);
        roadSegment.setRoadName(roadSegmentReader.getRoadName());
        roadSegment.setName(roadSegmentReader.getName());

        return roadSegment;
      })
      .collect(Collectors.toSet());
  }

  private Set<ParkingAreaType> serializeParkingAreas () {
    return reader.getParkingAreas(null)
      .stream()
      .map(parkingAreaReader -> {
        ParkingAreaType parkingArea = new ParkingAreaType();

        serializePlace(parkingArea, parkingAreaReader);
        ServicesType services = new ServicesType();
        services.getService().addAll(
          parkingAreaReader.getServices()
            .stream()
            .map(s -> {
              ServiceType service = new ServiceType();
              service.setName(s);
              return service;
            })
            .collect(Collectors.toSet())
        );
        parkingArea.setServices(services);

        return parkingArea;
      })
      .collect(Collectors.toSet());
  }

  private Set<GateType> serializeGates () {
    return reader.getGates(null)
      .stream()
      .map(gateReader -> {
        GateType gate = new GateType();

        serializePlace(gate, gateReader);
        gate.setType(GateTypeEnum.fromValue(gateReader.getType().value()));

        return gate;
      })
      .collect(Collectors.toSet());
  }

  private void serializePlaces () {
    PlacesType placesType = new PlacesType();

    placesType.getRoadSegment().addAll(serializeRoadSegments());
    placesType.getParkingArea().addAll(serializeParkingAreas());
    placesType.getGate().addAll(serializeGates());

    rns.setPlaces(placesType);
  }

  private XMLGregorianCalendar setVehicleEntryTime (GregorianCalendar gregorianCalendar) {
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    } catch (DatatypeConfigurationException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void serializeVehicles () {
    VehiclesType vehiclesType = new VehiclesType();

    Set<VehicleType> vehicles = reader.getVehicles(null, null, null)
      .stream()
      .map(v -> {
        VehicleType vehicle = new VehicleType();

        vehicle.setId(v.getId());
        vehicle.setType(VehicleTypeEnum.fromValue(v.getType().value()));
        vehicle.setEntryTime(setVehicleEntryTime((GregorianCalendar) v.getEntryTime()));
        vehicle.setDestination(v.getDestination().getId());
        vehicle.setOrigin(v.getOrigin().getId());
        vehicle.setPosition(v.getPosition().getId());
        vehicle.setState(VehicleStateEnum.fromValue(v.getState().value()));

        return vehicle;
      })
      .collect(Collectors.toSet());

    vehiclesType.getVehicle().addAll(vehicles);
    rns.setVehicles(vehiclesType);
  }

  private void serialize (String output) {

    serializePlaces();
    serializeVehicles();

    try {
      // Check if place is connected to itself and that place capacity is respected
      PlaceValidation.validateReader(reader);

      // Instantiate JAXB context
      JAXBContext jaxbContext = JAXBContext.newInstance("it.polito.dp2.RNS.sol1.jaxb");
      // Create Marshaller
      Marshaller m = jaxbContext.createMarshaller();
      // Format output in readable format
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      // Instantiate schema factory and add the custom validation schema
      SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
      Schema schema = sf.newSchema(new File("xsd/rnsInfo.xsd"));
      m.setSchema(schema);
      // Marshal data into the file specified as argument
      m.marshal(rns, new File(output));

    } catch (JAXBException e) {
      System.out.println("Caught JAXB Exception");
      e.printStackTrace();
    } catch (org.xml.sax.SAXException e) {
      System.out.println("Caught SAX Exception");
      e.printStackTrace();
    } catch (PlaceValidationException e) {
      System.out.println("Caught PlaceValidationException");
      e.printStackTrace();
    }
  }

}
