package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RnsLib implements RnsReader {

  private Map<String, PlaceReader> places = new HashMap<>();
  private Map<String, RoadSegmentReader> roadSegments = new HashMap<>();
  private Map<String, ParkingAreaReader> parkingAreas = new HashMap<>();
  private Map<String, GateReader> gates = new HashMap<>();
  private Set<ConnectionReader> connections = new HashSet<>();
  private Map<String, VehicleReader> vehicles = new HashMap<>();

  @Override
  public Set<PlaceReader> getPlaces (String s) {

    if (s == null) {
      return new HashSet<>(places.values());
    }

    return places.entrySet()
      .stream()
      .filter(e -> e.getKey().startsWith(s))
      .map(Map.Entry::getValue)
      .collect(Collectors.toSet());
  }

  @Override
  public PlaceReader getPlace (String s) {
    return places.get(s);
  }

  @Override
  public Set<GateReader> getGates (GateType gateType) {
    if (gateType == null) {
      return new HashSet<>(gates.values());
    }

    return gates.entrySet()
      .stream()
      .map(Map.Entry::getValue)
      .filter(g -> g.getType().equals(gateType))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<RoadSegmentReader> getRoadSegments (String s) {
    if (s == null) {
      return new HashSet<>(roadSegments.values());
    }

    return roadSegments.entrySet()
      .stream()
      .map(Map.Entry::getValue)
      .filter(rs -> rs.getRoadName().equals(s))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<ParkingAreaReader> getParkingAreas (Set<String> set) {
    if (set == null) {
      return new HashSet<>(parkingAreas.values());
    }

    return parkingAreas.entrySet()
      .stream()
      .map(Map.Entry::getValue)
      .filter(pa -> pa.getServices().containsAll(set))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<ConnectionReader> getConnections () {
    return connections;
  }

  @Override
  public Set<VehicleReader> getVehicles (Calendar calendar, Set<VehicleType> set, VehicleState vehicleState) {
    return vehicles.entrySet()
      .stream()
      .map(Map.Entry::getValue)
      .filter(v -> {
        if (calendar == null)
          return true;
        return v.getEntryTime().after(calendar);
      })
      .filter(v -> {
        if (set == null)
          return true;
        return set.contains(v.getType());
      })
      .filter(v -> {
        if (vehicleState == null)
          return true;
        return v.getState().equals(vehicleState);
      })
      .collect(Collectors.toSet());
  }

  @Override
  public VehicleReader getVehicle (String s) {
    return vehicles.get(s);
  }

  public void addPlace (RoadSegmentReader roadSegment) {
    roadSegments.put(roadSegment.getId(), roadSegment);
    places.put(roadSegment.getId(), roadSegment);
  }

  public void addPlace (ParkingAreaReader parkingArea) {
    parkingAreas.put(parkingArea.getId(), parkingArea);
    places.put(parkingArea.getId(), parkingArea);
  }

  public void addPlace (GateReader gate) {
    gates.put(gate.getId(), gate);
    places.put(gate.getId(), gate);
  }

  public void addConnections (String id, Set<ConnectionReader> connections) {
    getPlace(id).getNextPlaces()
      .addAll(
        connections
          .stream()
          .map(ConnectionReader::getTo)
          .collect(Collectors.toSet())
      );

    this.connections.addAll(connections);
  }

  public void addVehicle (VehicleReader vehicle) {
    vehicles.put(vehicle.getId(), vehicle);
  }

}
