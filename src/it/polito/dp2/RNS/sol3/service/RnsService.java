package it.polito.dp2.RNS.sol3.service;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.db.RnsSystemDb;
import it.polito.dp2.RNS.sol3.service.resources.PlacesResource;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriBuilder;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RnsService {

  private RnsSystemDb db = RnsSystemDb.getDb();

  private static PlaceType createPlaceType (PlaceReader placeReader) {
    PlaceType placeType = new PlaceType();
    placeType.setId(placeReader.getId());
    placeType.setCapacity(BigInteger.valueOf(placeReader.getCapacity()));
    placeType.getConnection().addAll(
      placeReader.getNextPlaces().stream()
        .map(IdentifiedEntityReader::getId)
        .collect(Collectors.toList())
    );
    return placeType;
  }

  public Places getPlaces (String idSuffix, int page) {
    Places places = new Places();

    places.setPage(BigInteger.ONE);
    places.setTotalPages(BigInteger.ONE);

    places.getPlace().addAll(
      db.getPlaces(idSuffix)
        .stream()
        .map(RnsService::createPlaceType)
        .collect(Collectors.toList())
    );

    return places;
  }

  public PlaceType getPlace (String id) {
    PlaceReader placeReader = db.getPlace(id);
    if (placeReader == null)
      return null;

    return createPlaceType(placeReader);
  }

  public List<PlaceType> getPlaceConnections (String id) {

    PlaceReader placeReader = db.getPlace(id);
    if (placeReader == null)
      return null;

    return placeReader.getNextPlaces()
      .stream()
      .map(RnsService::createPlaceType)
      .collect(Collectors.toList());
  }

  public RoadSegments getRoadSegments (String roadName) {
    RoadSegments roadSegments = new RoadSegments();

    roadSegments.setPage(BigInteger.ONE);
    roadSegments.setTotalPages(BigInteger.ONE);

    roadSegments.getRoadSegment().addAll(
      db.getRoadSegments(roadName)
        .stream()
        .map(rs -> {
          RoadSegment roadSegment = new RoadSegment();
          roadSegment.setId(rs.getId());
          roadSegment.setCapacity(BigInteger.valueOf(rs.getCapacity()));
          roadSegment.setName(rs.getName());
          roadSegment.setRoadName(rs.getRoadName());
          roadSegment.getConnection().addAll(
            rs.getNextPlaces().stream()
              .map(IdentifiedEntityReader::getId)
              .collect(Collectors.toList())
          );
          return roadSegment;
        })
        .collect(Collectors.toSet())
    );

    return roadSegments;
  }

  public ParkingAreas getParkingAreas (Set<String> services) {
    ParkingAreas parkingAreas = new ParkingAreas();

    parkingAreas.setPage(BigInteger.ONE);
    parkingAreas.setTotalPages(BigInteger.ONE);

    parkingAreas.getParkingArea().addAll(
      db.getParkingAreas(services)
        .stream()
        .map(pa -> {
          ParkingArea parkingArea = new ParkingArea();
          parkingArea.setId(pa.getId());
          parkingArea.setCapacity(BigInteger.valueOf(pa.getCapacity()));
          ServicesType servicesType = new ServicesType();
          servicesType.getService().addAll(pa.getServices());
          parkingArea.setServices(servicesType);
          parkingArea.getConnection().addAll(
            pa.getNextPlaces().stream()
              .map(IdentifiedEntityReader::getId)
              .collect(Collectors.toList())
          );
          return parkingArea;
        })
        .collect(Collectors.toSet())
    );

    return parkingAreas;
  }

  public Gates getGates (GateType type) {
    Gates gates = new Gates();

    gates.setPage(BigInteger.ONE);
    gates.setTotalPages(BigInteger.ONE);

    gates.getGate().addAll(
      db.getGates(type)
        .stream()
        .map(g -> {
          Gate gate = new Gate();
          gate.setId(g.getId());
          gate.setCapacity(BigInteger.valueOf(g.getCapacity()));
          gate.setType(GateTypeEnum.fromValue(g.getType().value()));
          gate.getConnection().addAll(
            g.getNextPlaces().stream()
              .map(IdentifiedEntityReader::getId)
              .collect(Collectors.toList())
          );
          return gate;
        })
        .collect(Collectors.toSet())
    );

    return gates;
  }

  public Connections getConnections (int page) {
    Connections connections = new Connections();

    connections.setPage(BigInteger.ONE);
    connections.setTotalPages(BigInteger.ONE);

    connections.getConnection().addAll(
      db.getConnections().stream()
        .map(c -> {
          Connection connection = new Connection();
          connection.setFrom(c.getFrom().getId());
          connection.setTo(c.getTo().getId());
          return connection;
        })
        .collect(Collectors.toList())
    );

    return connections;
  }

  public List<String> findShortestPath (String sourceId, String destinationId)
    throws UnknownIdException, ServiceException, BadStateException {
    return db.findShortestPath(sourceId, destinationId);
  }

  public Vehicle addVehicle (Vehicle vehicle) {
    return db.addVehicle(vehicle);
  }

  public List<Vehicle> getVehicles (GregorianCalendar since, VehicleStateEnum state, Set<VehicleTypeEnum> types) {
    return db.getVehicles(since, state, types);
  }

  public Vehicle getVehicle (String id) {
    return db.getVehicle(id);
  }

  public Vehicle updateVehicle (String id, Vehicle vehicle) {
    return db.updateVehicle(id, vehicle);
  }

  public synchronized void removeVehicle (String id, String outGate, boolean forced, UriBuilder baseUri) {
    Vehicle vehicle = getVehicle(id);

    System.out.println("DELETE vehicle --- args: id " + id + " outgate " + outGate + " forced " + forced + " base " + baseUri.clone().toTemplate());
    System.out.println("vehicle " + vehicle);

    if (vehicle == null)
      throw new NotFoundException();

    if (forced) {
      db.removeVehicle(id);
      return;
    }

    GateReader gate = db.getGate(PlacesResource.getPlaceIdFromUri(outGate, baseUri));

    System.out.println("Gate provided " + gate);

    if (gate == null)
      throw new ClientErrorException(422);

    PlaceReader previousPosition = db.getPlace(PlacesResource.getPlaceIdFromUri(vehicle.getPosition(), baseUri));
    System.out.println("previous position " + previousPosition.getId());
    System.out.println("previous position next places: ");
    previousPosition.getNextPlaces().forEach(p -> System.out.println(p.getId()));

    boolean isGateNear = previousPosition.getNextPlaces()
      .stream()
      .anyMatch(p -> p.getId().equals(gate.getId()));

    System.out.println("isGateNear " + isGateNear);
    System.out.println("Gate Type " + gate.getType().value());

    if ((gate.getType() == GateType.OUT || gate.getType() == GateType.INOUT) && isGateNear) {
      db.removeVehicle(id);
      return;
    }

    throw new ClientErrorException(403);
  }
}
