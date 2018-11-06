package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.*;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


public class RnsReaderSol implements RnsReader {

  private Set<PlaceReader> places;
  private Set<GateReader> gates;
  private Set<RoadSegmentReader> roadSegments;
  private Set<ParkingAreaReader> parkingAreas;
  private Set<ConnectionReader> connections;
  private Set<VehicleReader> vehicles;

  public RnsReaderSol () {
    // Initialize everything as an empty set
    places = new HashSet<>();
    gates = new HashSet<>();
    roadSegments = new HashSet<>();
    parkingAreas = new HashSet<>();
    connections = new HashSet<>();
    vehicles = new HashSet<>();
  }

  @Override
  public Set<PlaceReader> getPlaces (String s) {
    return places;
  }

  @Override
  public PlaceReader getPlace (String s) {
    return null;
  }

  @Override
  public Set<GateReader> getGates (GateType gateType) {
    return null;
  }

  @Override
  public Set<RoadSegmentReader> getRoadSegments (String s) {
    return null;
  }

  @Override
  public Set<ParkingAreaReader> getParkingAreas (Set<String> set) {
    return null;
  }

  @Override
  public Set<ConnectionReader> getConnections () {
    return null;
  }

  @Override
  public Set<VehicleReader> getVehicles (Calendar calendar, Set<VehicleType> set, VehicleState vehicleState) {
    return null;
  }

  @Override
  public VehicleReader getVehicle (String s) {
    return null;
  }

}
