package it.polito.dp2.RNS.sol3.service;

import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.IdentifiedEntityReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.db.RnsSystemDb;
import it.polito.dp2.RNS.sol3.service.resources.PlacesResource;

import javax.ws.rs.core.UriBuilder;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
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

  public List<Vehicle> getVehicles (GregorianCalendar since, VehicleStateEnum state, Set<VehicleTypeEnum> types, String placeId, UriBuilder baseUrl) {
    return db.getVehicles()
      .stream()
      .filter(v -> since == null || v.getEntryTime().toGregorianCalendar().after(since))
      .filter(v -> state == null || v.getState().equals(state))
      .filter(v -> types == null || types.size() == 0 || types.contains(v.getType()))
      .filter(v -> placeId == null || placeId.equals(PlacesResource.getPlaceIdFromUri(v.getPosition(), baseUrl)))
      .collect(Collectors.toList());
  }

  public Vehicle getVehicle (String id) {
    return db.getVehicle(id);
  }

  public Vehicle updateVehicle (String id, Vehicle vehicle) {
    return db.updateVehicle(id, vehicle);
  }

  public Map<String, Vehicle> getVehiclesSyncObject () {
    return db.getVehiclesMap();
  }

  public Vehicle removeVehicle (String id) {
    return db.removeVehicle(id);
  }

  public static boolean areVehiclesEquals (Vehicle v1, Vehicle v2) {
    return (
      v1.getSelf().equals(v2.getSelf()) &&
        v1.getPlateId().equals(v2.getPlateId()) &&
        v1.getOrigin().equals(v2.getOrigin()) &&
        v1.getDestination().equals(v2.getDestination()) &&
        v1.getEntryTime().equals(v2.getEntryTime()) &&
        v1.getType().equals(v2.getType())
    );
  }
}
