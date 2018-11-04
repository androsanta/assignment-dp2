package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.sol1.jaxb.Rns;

import java.util.Calendar;
import java.util.Set;


public class RnsReaderSol implements RnsReader {

  private Set<PlaceReader> places;
  private Set<GateReader> gates;
  private Set<RoadSegmentReader> roadSegments;
  private Set<ParkingAreaReader> parkingAreas;
  private Set<ConnectionReader> connections;
  private Set<VehicleReader> vehicles;

  public RnsReaderSol (Rns rns) {

  }

  @Override
  public Set<PlaceReader> getPlaces (String s) {
    return null;
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
