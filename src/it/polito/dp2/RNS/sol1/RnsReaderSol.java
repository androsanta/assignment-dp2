package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.*;

import java.util.Calendar;
import java.util.Set;

public class RnsReaderSol implements it.polito.dp2.RNS.RnsReader {

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
