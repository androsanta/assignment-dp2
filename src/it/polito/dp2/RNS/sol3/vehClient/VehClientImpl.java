package it.polito.dp2.RNS.sol3.vehClient;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.*;
import it.polito.dp2.RNS.sol1.Place;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VehClientImpl implements it.polito.dp2.RNS.lab3.VehClient {

  private String vehicleUrl;
  private Vehicle vehicle;
  private Map<String, String> placeIdByUrl;
  private Map<String, String> placeUrlById;

  public VehClientImpl () throws VehClientException {
    String uri = System.getProperty("it.polito.dp2.RNS.lab3.URL");
    uri = uri == null ? "http://localhost:8080/RnsSystem/rest" : uri;
    URI restUri = UriBuilder.fromUri(uri).build();

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
      throw new VehClientException(e);
    }

    Places places;
    try {
      Response response = client.target(entry.getPlaces())
        .request()
        .accept(MediaType.APPLICATION_XML)
        .get();

      if (response.getStatus() != 200)
        throw new Exception("Get to " + entry.getPlaces() + " failed with code " + response.getStatus());

      places = response.readEntity(Places.class);
      response.close();
    } catch (Exception e) {
      throw new VehClientException(e.getMessage());
    }

    placeIdByUrl = places.getPlace()
      .stream()
      .collect(Collectors.toMap(PlaceType::getSelf, PlaceType::getId));
    placeUrlById = places.getPlace()
      .stream()
      .collect(Collectors.toMap(PlaceType::getId, PlaceType::getSelf));
    vehicleUrl = entry.getVehicles();
    client.close();
  }

  private List<String> getShortestPath () {
    return vehicle.getShortestPath().getPlace()
      .stream()
      .map(placeUrl -> placeIdByUrl.get(placeUrl))
      .collect(Collectors.toList());
  }

  @Override
  public List<String> enter (String plateId, VehicleType type, String inGate, String destination)
    throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException {

    String inGateUrl = placeUrlById.get(inGate) == null ? inGate : placeUrlById.get(inGate);
    String destinationUrl = placeUrlById.get(destination) == null ? destination : placeUrlById.get(destination);

    Client client = ClientBuilder.newClient();
    Response response;
    EnterVehicle enterVehicle = new EnterVehicle();
    enterVehicle.setPlateId(plateId);
    enterVehicle.setVehicleType(VehicleTypeEnum.fromValue(type.value()));
    enterVehicle.setEnterGate(inGateUrl);
    enterVehicle.setDestination(destinationUrl);

    try {
      response = client.target(vehicleUrl)
        .request()
        .accept(MediaType.APPLICATION_XML)
        .post(Entity.xml(enterVehicle));
    } catch (Exception e) {
      throw new ServiceException(e);
    }

    switch (response.getStatus()) {
      case 200:
        vehicle = response.readEntity(Vehicle.class);
        response.close();
        client.close();
        return getShortestPath();
      case 403:
        response.close();
        client.close();
        throw new WrongPlaceException();
      case 409:
        response.close();
        client.close();
        throw new EntranceRefusedException();
      case 422:
        response.close();
        client.close();
        throw new UnknownPlaceException();
      default:
        response.close();
        client.close();
        throw new ServiceException();
    }

  }

  @Override
  public List<String> move (String newPlace) throws ServiceException, UnknownPlaceException, WrongPlaceException {
    Client client = ClientBuilder.newClient();

    Response response;
    String newPlaceUrl = placeUrlById.get(newPlace) == null ? newPlace : placeUrlById.get(newPlace);
    vehicle.setPosition(newPlaceUrl);
    vehicle.setState(VehicleStateEnum.IN_TRANSIT);
    try {
      response = client.target(vehicle.getSelf())
        .request()
        .accept(MediaType.APPLICATION_XML)
        .put(Entity.xml(vehicle));
    } catch (Exception e) {
      throw new ServiceException(e);
    }

    switch (response.getStatus()) {
      case 200:
        Vehicle newVehicle = response.readEntity(Vehicle.class);
        response.close();
        client.close();
        boolean pathChanged = newVehicle.getShortestPath().getPlace().containsAll(getShortestPath());
        vehicle = newVehicle;
        if (!pathChanged)
          return null;
        return getShortestPath();
      case 403:
        response.close();
        client.close();
        throw new WrongPlaceException();
      case 422:
        response.close();
        client.close();
        throw new UnknownPlaceException();
      default:
        response.close();
        client.close();
        throw new ServiceException();
    }
  }

  @Override
  public void changeState (VehicleState newState) throws ServiceException {
    Client client = ClientBuilder.newClient();

    Response response;
    vehicle.setState(VehicleStateEnum.fromValue(newState.value()));
    try {
      response = client.target(vehicle.getSelf())
        .request()
        .accept(MediaType.APPLICATION_XML)
        .put(Entity.xml(vehicle));
    } catch (Exception e) {
      throw new ServiceException(e);
    }

    if (response.getStatus() != 200)
      throw new ServiceException();

    vehicle = response.readEntity(Vehicle.class);
  }

  @Override
  public void exit (String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException {
    Client client = ClientBuilder.newClient();

    Response response;
    String outGateUrl = placeUrlById.get(outGate) == null ? outGate : placeUrlById.get(outGate);
    try {
      response = client.target(vehicle.getSelf())
        .queryParam("outGate", outGateUrl)
        .request()
        .accept(MediaType.APPLICATION_XML)
        .delete();
    } catch (Exception e) {
      throw new ServiceException(e);
    }

    switch (response.getStatus()) {
      case 204:
        response.close();
        client.close();
        return;
      case 403:
        response.close();
        client.close();
        throw new WrongPlaceException();
      case 422:
        response.close();
        client.close();
        throw new UnknownPlaceException();
      default:
        response.close();
        client.close();
        throw new ServiceException();
    }
  }

}
