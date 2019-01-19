package it.polito.dp2.RNS.sol3.vehClient;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.*;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.RnsEntry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

public class VehClientImpl implements it.polito.dp2.RNS.lab3.VehClient {

  private String vehicleUrl;

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

    try {

    } catch (Exception e) {

    }

    return null;
  }

  @Override
  public List<String> move (String newPlace) throws ServiceException, UnknownPlaceException, WrongPlaceException {
    return null;
  }

  @Override
  public void changeState (VehicleState newState) throws ServiceException {

  }

  @Override
  public void exit (String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException {

  }

}
