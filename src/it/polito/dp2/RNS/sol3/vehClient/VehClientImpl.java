package it.polito.dp2.RNS.sol3.vehClient;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.*;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

public class VehClientImpl implements it.polito.dp2.RNS.lab3.VehClient {

  private String vehicleUrl;
  private Vehicle vehicle;

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

    vehicleUrl = entry.getVehicles();
    client.close();
  }

  @Override
  public List<String> enter (String plateId, VehicleType type, String inGate, String destination)
    throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException {
    Client client = ClientBuilder.newClient();
    Response response;
    EnterVehicle enterVehicle = new EnterVehicle();
    enterVehicle.setPlateId(plateId);
    enterVehicle.setVehicleType(VehicleTypeEnum.fromValue(type.value()));
    enterVehicle.setEnterGate(inGate);
    enterVehicle.setDestination(destination);

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
        return vehicle.getShortestPath().getPlace();
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
    UpdateVehicle updateVehicle = new UpdateVehicle();
    updateVehicle.setPosition(newPlace);
    try {
      response = client.target(vehicle.getSelf())
        .request()
        .accept(MediaType.APPLICATION_XML)
        .method("PATCH", Entity.xml(updateVehicle));
    } catch (Exception e) {
      throw new ServiceException(e);
    }
    switch (response.getStatus()) {
      case 200:
        vehicle = response.readEntity(Vehicle.class);
        response.close();
        client.close();
        return vehicle.getShortestPath().getPlace();
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
    UpdateVehicle updateVehicle = new UpdateVehicle();
    updateVehicle.setState(VehicleStateEnum.fromValue(newState.value()));
    try {
      response = client.target(vehicle.getSelf())
        .request()
        .accept(MediaType.APPLICATION_XML)
        .method("PATCH", Entity.xml(updateVehicle));
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
    try {
      response = client.target(vehicle.getSelf())
        .queryParam("outGate", outGate)
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
