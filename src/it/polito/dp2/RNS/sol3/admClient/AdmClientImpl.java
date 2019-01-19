package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab3.ServiceException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class AdmClientImpl implements it.polito.dp2.RNS.lab3.AdmClient {

  private URI restUri;

  public AdmClientImpl () throws ServiceException {
    String uri = System.getProperty("it.polito.dp2.RNS.lab3.URL");
    uri = uri == null ? "http://localhost:8080/RnsSystem/rest" : uri;
    restUri = UriBuilder.fromUri(uri).path("rns").build();

    Client client = ClientBuilder.newClient();



    client.close();
  }

  @Override
  public Set<VehicleReader> getUpdatedVehicles (String place) throws ServiceException {
    return null;
  }

  @Override
  public VehicleReader getUpdatedVehicle (String id) throws ServiceException {
    return null;
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
    return new HashSet<>();
  }

  @Override
  public VehicleReader getVehicle (String s) {
    return null;
  }
}
