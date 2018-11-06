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
    setProperties();
    RnsReaderFactory factory = RnsReaderFactory.newInstance();
    reader = factory.newRnsReader();
    rns = new Rns();
  }

  private void setProperties () {
    System.setProperty(
      "it.polito.dp2.RNS.RnsReaderFactory",
      "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.seed",
      "2"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.testcase",
      "1"
    );
  }

  public static void main (String[] args) {

    if (args.length != 1) {
      System.out.println("Specify an output file as argument");
      return;
    }

    final String output = args[0];
    RnsInfoSerializer rnsInfo;

    try {
      rnsInfo = new RnsInfoSerializer();
      rnsInfo.serializeAll(output);
    } catch (RnsReaderException e) {
      System.out.println("Error while instantiating Rns Serializer");
      e.printStackTrace();
    }

  }

  private void serializeRoads () {
    RoadsType roadsType = new RoadsType();

    // Get all road segments and get from them all the roads name
    Set<RoadType> roads = reader.getRoadSegments(null)
      .stream()
      // Group by roadName - list of road segment
      .collect(Collectors.groupingBy(RoadSegmentReader::getRoadName))
      .entrySet()
      .stream()
      // Map to RoadType (which contains a list of Segment)
      .map(e -> {
        RoadType road = new RoadType();
        road.setName(e.getKey());
        // Add all segments by mapping them to Segment object
        road.getSegment().addAll(
          e.getValue()
            .stream()
            .map(rs -> {
              RoadSegmentType segment = new RoadSegmentType();
              segment.setId(rs.getId());
              segment.setName(rs.getName());
              return segment;
            })
            .collect(Collectors.toSet())
        );
        return road;
      })
      // Collect everything into a list of RoadType
      .collect(Collectors.toSet());

    roadsType.getRoad().addAll(roads);
    rns.setRoads(roadsType);
  }

  private void serializeParkingAreas () {
    ParkingAreasType parkingAreasType = new ParkingAreasType();

    Set<ParkingAreaType> parkingAreas = reader.getParkingAreas(null)
      .stream()
      .map(pa -> {
        ParkingAreaType parkingArea = new ParkingAreaType();
        parkingArea.setId(pa.getId());

        ParkingAreaType.Services services = new ParkingAreaType.Services();
        services.getService().addAll(
          pa.getServices()
            .stream()
            .map(s -> {
              ParkingAreaType.Services.Service service = new ParkingAreaType.Services.Service();
              service.setName(s);
              return service;
            })
            .collect(Collectors.toList())
        );
        parkingArea.setServices(services);

        return parkingArea;
      })
      .collect(Collectors.toSet());

    parkingAreasType.getParkingArea().addAll(parkingAreas);
    rns.setParkingAreas(parkingAreasType);
  }

  private void serializeGates () {
    GatesType gatesType = new GatesType();

    Set<GateType> gates = reader.getGates(null)
      .stream()
      .map(g -> {
        GateType gate = new GateType();

        gate.setId(g.getId());
        gate.setType(GateTypeEnum.fromValue(g.getType().value()));

        return gate;
      })
      .collect(Collectors.toSet());

    gatesType.getGate().addAll(gates);
    rns.setGates(gatesType);
  }

  private void serializePlaces () {
    PlacesType placesType = new PlacesType();

    Set<PlaceType> places = reader.getPlaces(null)
      .stream()
      .map(p -> {
        PlaceType place = new PlaceType();

        place.setId(p.getId());
        place.setCapacity(BigInteger.valueOf(p.getCapacity()));

        Set<ConnectionType> connections = p.getNextPlaces()
          .stream()
          .map(c -> {
            ConnectionType conn = new ConnectionType();
            conn.setId(c.getId());
            return conn;
          })
          .collect(Collectors.toSet());

        place.getConnection().addAll(connections);

        return place;
      })
      .collect(Collectors.toSet());

    placesType.getPlace().addAll(places);
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

  private void serializeAll (String output) {

    serializeRoads();
    serializeParkingAreas();
    serializeGates();
    serializePlaces();
    serializeVehicles();

    try {
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
    }
  }

}
