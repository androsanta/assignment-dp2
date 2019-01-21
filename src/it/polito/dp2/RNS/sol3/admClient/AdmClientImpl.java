package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.sol1.Connection;
import it.polito.dp2.RNS.sol1.Gate;
import it.polito.dp2.RNS.sol1.ParkingArea;
import it.polito.dp2.RNS.sol1.RnsLib;
import it.polito.dp2.RNS.sol1.RoadSegment;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AdmClientImpl extends RnsLib implements it.polito.dp2.RNS.lab3.AdmClient {

  private URI restUri;
  private String vehicleUrl;
  private Map<String, String> placesIdByUrl;
  private Map<String, String> vehiclesInPlaceById;


  public AdmClientImpl () throws ServiceException {
    String uri = System.getProperty("it.polito.dp2.RNS.lab3.URL");
    uri = uri == null ? "http://localhost:8080/RnsSystem/rest" : uri;
    restUri = UriBuilder.fromUri(uri).build();

    Client client = ClientBuilder.newClient();

    RnsEntry entry;

    try {
      Response response = client.target(restUri)
        .request()
        .accept(MediaType.APPLICATION_XML)
        .get();

      if (response.getStatus() != 200)
        throw new Exception("Get to " + restUri.toString() + " failed with code " + response.getStatus());

      entry = response.readEntity(RnsEntry.class);
      response.close();
    } catch (Exception e) {
      throw new ServiceException(e);
    }

    vehicleUrl = entry.getVehicles();
    loadPlaces(client, entry.getPlaces());
    client.close();
  }

  private void loadPlaces (Client client, String placesUrl) throws ServiceException {
    Places places;
    try {
      Response response = client.target(placesUrl)
        .request()
        .accept(MediaType.APPLICATION_XML)
        .get();

      if (response.getStatus() != 200)
        throw new Exception("Get to " + placesUrl + " failed with code " + response.getStatus());

      places = response.readEntity(Places.class);
      response.close();
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }

    // Road Segments
    RoadSegments roadSegments;
    try {
      Response response = client.target(places.getRoadSegments())
        .queryParam("admin", true)
        .request()
        .accept(MediaType.APPLICATION_XML)
        .get();

      if (response.getStatus() != 200)
        throw new Exception("Get to " + places.getRoadSegments() + " failed with code " + response.getStatus());

      roadSegments = response.readEntity(RoadSegments.class);
      response.close();
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }

    roadSegments.getRoadSegment().forEach(rs ->
      addPlace(new RoadSegment(rs.getId(), rs.getCapacity().intValue(), rs.getName(), rs.getRoadName()))
    );

    // Parking Areas
    ParkingAreas parkingAreas;
    try {
      Response response = client.target(places.getParkingAreas())
        .queryParam("admin", true)
        .request()
        .accept(MediaType.APPLICATION_XML)
        .get();

      if (response.getStatus() != 200)
        throw new Exception("Get to " + places.getParkingAreas() + " failed with code " + response.getStatus());

      parkingAreas = response.readEntity(ParkingAreas.class);
      response.close();
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }

    parkingAreas.getParkingArea().forEach(pa ->
      addPlace(new ParkingArea(
        pa.getId(),
        pa.getCapacity().intValue(),
        new HashSet<>(pa.getServices().getService()))
      )
    );

    // Gates
    Gates gates;
    try {
      Response response = client.target(places.getGates())
        .queryParam("admin", true)
        .request()
        .accept(MediaType.APPLICATION_XML)
        .get();

      if (response.getStatus() != 200)
        throw new Exception("Get to " + places.getGates() + " failed with code " + response.getStatus());

      gates = response.readEntity(Gates.class);
      response.close();
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }

    gates.getGate().forEach(g ->
      addPlace(new Gate(g.getId(), g.getCapacity().intValue(), GateType.fromValue(g.getType().value())))
    );

    // Connections
    placesIdByUrl = places.getPlace()
      .stream()
      .collect(Collectors.toMap(PlaceType::getSelf, PlaceType::getId));

    places.getPlace().forEach(p -> {
      PlaceReader from = getPlace(p.getId());

      Set<ConnectionReader> connectionReaders = p.getConnection()
        .stream()
        .map(c -> new Connection(from, getPlace(placesIdByUrl.get(c))))
        .collect(Collectors.toSet());

      addConnections(p.getId(), connectionReaders);
    });

    vehiclesInPlaceById = places.getPlace()
      .stream()
      .collect(Collectors.toMap(PlaceType::getId, PlaceType::getVehicles));
  }

  @Override
  public Set<VehicleReader> getUpdatedVehicles (String place) throws ServiceException {
    Client client = ClientBuilder.newClient();

    String requestUrl = place == null ? vehicleUrl : vehiclesInPlaceById.get(place);

    Vehicles vehicles;
    try {
      Response response = client.target(requestUrl)
        .queryParam("admin", true)
        .request()
        .accept(MediaType.APPLICATION_XML)
        .get();

      if (response.getStatus() != 200)
        throw new Exception("Get to " + requestUrl + " failed with code " + response.getStatus());

      vehicles = response.readEntity(Vehicles.class);
      response.close();
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }

    client.close();

    Set<VehicleReader> readers = vehicles.getVehicle()
      .stream()
      .map(v -> new it.polito.dp2.RNS.sol1.Vehicle(
        v.getPlateId(),
        VehicleType.fromValue(v.getType().value()),
        v.getEntryTime().toGregorianCalendar(),
        getPlace(placesIdByUrl.get(v.getOrigin())),
        getPlace(placesIdByUrl.get(v.getDestination())),
        getPlace(placesIdByUrl.get(v.getPosition())),
        VehicleState.fromValue(v.getState().value())
      ))
      .collect(Collectors.toSet());
    return readers;
  }

  @Override
  public VehicleReader getUpdatedVehicle (String id) throws ServiceException {
    Client client = ClientBuilder.newClient();
    Vehicle vehicle;
    try {
      Response response = client.target(vehicleUrl)
        .path(id)
        .request()
        .accept(MediaType.APPLICATION_XML)
        .get();

      if (response.getStatus() == 404)
        return null;

      if (response.getStatus() != 200)
        throw new Exception("Get to " + vehicleUrl + "/" + id + " failed with code " + response.getStatus());

      vehicle = response.readEntity(Vehicle.class);
      response.close();
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }

    client.close();

    return new it.polito.dp2.RNS.sol1.Vehicle(
      vehicle.getPlateId(),
      VehicleType.fromValue(vehicle.getType().value()),
      vehicle.getEntryTime().toGregorianCalendar(),
      getPlace(placesIdByUrl.get(vehicle.getOrigin())),
      getPlace(placesIdByUrl.get(vehicle.getDestination())),
      getPlace(placesIdByUrl.get(vehicle.getPosition())),
      VehicleState.fromValue(vehicle.getState().value())
    );
  }
}
