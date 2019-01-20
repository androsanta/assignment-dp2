package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.VehicleStateEnum;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.VehicleTypeEnum;
import it.polito.dp2.RNS.sol3.service.resources.PlacesResource;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriBuilder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RnsSystemDb {

  private static RnsSystemDb db = new RnsSystemDb();

  private RnsReader rnsReader;
  private Map<String, PlaceReader> places;

  private PathFinder pathFinder;
  private ConcurrentHashMap<String, Vehicle> trackedVehicles;

  private RnsSystemDb () {
    System.setProperty(
      "it.polito.dp2.RNS.lab2.PathFinderFactory",
      "it.polito.dp2.RNS.sol2.PathFinderFactory"
    );
    if (System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL") == null) {
      System.setProperty(
        "it.polito.dp2.RNS.lab2.URL",
        "http://localhost:7474/db"
      );
    } else {
      System.setProperty(
        "it.polito.dp2.RNS.lab2.URL",
        System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL")
      );
    }

    try {
      // Instantiate the reader
      rnsReader = RnsReaderFactory.newInstance().newRnsReader();
      // Instantiate pathFinder and load into Neo4j the reader
      pathFinder = PathFinderFactory.newInstance().newPathFinder();
      pathFinder.reloadModel();
      // Initialize list of tracked vehicles
      trackedVehicles = new ConcurrentHashMap<>();
    } catch (RnsReaderException | PathFinderException | ServiceException | ModelException e) {
      throw new InternalServerErrorException(e);
    }
  }

  public static RnsSystemDb getDb () {
    return db;
  }

  public Set<PlaceReader> getPlaces (String idSuffix) {
    return rnsReader.getPlaces(idSuffix);
  }

  public PlaceReader getPlace (String id) {
    return rnsReader.getPlace(id);
  }

  public Set<RoadSegmentReader> getRoadSegments (String roadName) {
    return rnsReader.getRoadSegments(roadName);
  }

  public Set<ParkingAreaReader> getParkingAreas (Set<String> services) {
    return rnsReader.getParkingAreas(services);
  }

  public Set<GateReader> getGates (GateType type) {
    return rnsReader.getGates(type);
  }

  public GateReader getGate (String id) {
    List<GateReader> gates = rnsReader.getGates(null).stream()
      .filter(g -> g.getId().equals(id))
      .collect(Collectors.toList());
    if (gates.size() == 1)
      return gates.get(0);
    return null;
  }

  public Set<ConnectionReader> getConnections () {
    return rnsReader.getConnections();
  }

  public Vehicle addVehicle (Vehicle vehicle) {
    if (trackedVehicles.putIfAbsent(vehicle.getPlateId(), vehicle) == null) {
      return vehicle;
    }
    return null;
  }

  public List<String> findShortestPath (String sourceId, String destinationId)
    throws UnknownIdException, ServiceException, BadStateException {
    Set<List<String>> paths = pathFinder.findShortestPaths(sourceId, destinationId, 0);
    if (paths.size() == 0)
      return new ArrayList<>();

    return new ArrayList<>(paths).get(0);
  }

  public List<Vehicle> getVehicles (GregorianCalendar since, VehicleStateEnum state, Set<VehicleTypeEnum> types) {
    return trackedVehicles.values().stream()
       .filter(v -> since == null || v.getEntryTime().toGregorianCalendar().after(since))
       .filter(v -> state == null || v.getState().equals(state))
       .filter(v -> types == null || types.size() == 0 || types.contains(v.getType()))
      .collect(Collectors.toList());
  }

  public Vehicle getVehicle (String plateId) {
    return trackedVehicles.get(plateId);
  }

  public Vehicle updateVehicle (String id, Vehicle updatedVehicle) {
    return trackedVehicles.computeIfPresent(
      id,
      (key, value) -> updatedVehicle
    );
  }

  public Vehicle forceRemoveVehicle (String plateId) {
    return trackedVehicles.remove(plateId);
  }

  public synchronized void removeVehicle (String plateId, String outGate, UriBuilder baseUri) {
    Vehicle vehicle = getVehicle(plateId);

    System.out.println("DELETE vehicle --- args: id " + plateId + " outgate " + outGate + " base " + baseUri.clone().toTemplate());
    System.out.println("vehicle " + vehicle);

    if (vehicle == null)
      throw new NotFoundException();

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

    if (
      (gate.getType() == GateType.OUT || gate.getType() == GateType.INOUT) &&
        (isGateNear || outGate.equals(vehicle.getPosition()))
    ) {
      trackedVehicles.remove(plateId);
      return;
    }

    throw new ClientErrorException(403);
  }
}
