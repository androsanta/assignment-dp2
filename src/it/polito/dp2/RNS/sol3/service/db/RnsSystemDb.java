package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.VehicleStateEnum;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.VehicleTypeEnum;

import javax.ws.rs.InternalServerErrorException;
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

  public Map<String, Vehicle> getVehiclesMap () {
    return trackedVehicles;
  }

  public Vehicle updateVehicle (String id, Vehicle updatedVehicle) {
    return trackedVehicles.computeIfPresent(
      id,
      (key, value) -> updatedVehicle
    );
  }

  public Vehicle removeVehicle (String plateId) {
    return trackedVehicles.remove(plateId);
  }
}
