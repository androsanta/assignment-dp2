package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.sol1.jaxb.*;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import javax.xml.bind.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
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
      "1"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.testcase",
      "0"
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
    List<RoadType> roads = reader.getRoadSegments(null)
      .stream()
      // Group by roadName - list of road segment
      .collect(Collectors.groupingBy(RoadSegmentReader::getRoadName))
      .entrySet()
      .stream()
      // Gap to RoadType (which contains a list of Segment)
      .map(e -> {
        RoadType road = new RoadType();
        road.setName(e.getKey());
        // Add all segments by mapping them to Segment object
        road.getSegment().addAll(
          e.getValue()
            .stream()
            .map(rs -> {
              RoadType.Segment segment = new RoadType.Segment();
              segment.setId(rs.getId());
              segment.setName(rs.getName());
              return segment;
            })
          .collect(Collectors.toSet())
        );
        return road;
      })
      // Collect everything into a list of RoadType
      .collect(Collectors.toList());

    roadsType.getRoad().addAll(roads);
    rns.setRoads(roadsType);
  }

  private void serializePlaces () {
    PlacesType placesType = new PlacesType();

    List<PlaceType> places = reader.getPlaces(null)
      .stream()
      .map(place -> {
        PlaceType placeType = new PlaceType();

        placeType.setId(place.getId());
        placeType.setCapacity(BigInteger.valueOf(place.getCapacity()));



        return placeType;
      })
      .collect(Collectors.toList());

    placesType.getPlace().addAll(places);
    rns.setPlaces(placesType);
  }

  private void serializeAll (String output) {

    serializeRoads();
    serializePlaces();

    try {
      // Instantiate JAXB context
      JAXBContext jc = JAXBContext.newInstance("it.polito.dp2.RNS.sol1.jaxb");
      // Create Marshaller
      Marshaller m = jc.createMarshaller();
      // Format output in readable format
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      // Instantiate schema factory and add the custom validation schema
      // SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
      // Schema schema = sf.newSchema(new File("xsd/rnsInfo.xsd"));
      // m.setSchema(schema);
      // Marshal data into the file specified as argument
      m.marshal(rns, new File(output));

    } catch (JAXBException e) {
      System.out.println("Caught JAXB Exception");
      e.printStackTrace();
    }/* catch (org.xml.sax.SAXException e) {
      System.out.println("Caught SAX Exception");
      e.printStackTrace();
    }*/
  }

}
