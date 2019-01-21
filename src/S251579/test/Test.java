package S251579.test;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.lab3.*;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.EnterVehicle;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.VehicleTypeEnum;
import it.polito.dp2.RNS.sol3.vehClient.VehClientImpl;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Test {

  public static void main (String args[]) {
    System.setProperty(
      "it.polito.dp2.RNS.lab2.URL",
      "http://192.168.1.3:7474/db"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab2.PathFinderFactory",
      "it.polito.dp2.RNS.sol2.PathFinderFactory"
    );
    System.setProperty(
      "it.polito.dp2.RNS.RnsReaderFactory",
      "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.seed",
      "1211530"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.testcase",
      "1"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab3.AdmClientFactory",
      "it.polito.dp2.RNS.sol3.admClient.AdmClientFactory"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab3.URL",
      "http://192.168.1.3:8080/RnsSystem/rest"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab3.VehClientFactory",
      "it.polito.dp2.RNS.sol3.vehClient.VehClientFactory"
    );

    VehClient vehClient = null;
    try {
      vehClient = new VehClientImpl();
      List<String> path = vehClient.enter(
        "ziofa",
        VehicleType.CAR,
        "Gate0",
        "Gate1"
      );

      for (int i = 1; i < path.size() - 2; i++) {
        System.out.println("change path to" + path.get(i));
        vehClient.move(path.get(i));
      }

      vehClient.changeState(VehicleState.PARKED);
    } catch (VehClientException | UnknownPlaceException | WrongPlaceException | ServiceException | EntranceRefusedException e) {
      e.printStackTrace();
    }

    try {
      AdmClient admClient = AdmClientFactory.newInstance().newAdmClient();
      System.out.println("\nPLACES ---------");
      admClient.getPlaces(null).forEach(p -> System.out.println(p.getId()));
      System.out.println("\nCONNECTIONS ---------");
      admClient.getConnections().forEach(c -> System.out.println("from " + c.getFrom().getId() + " to " + c.getTo().getId()));
      System.out.println("\nVEHICLES ---------");
      admClient.getUpdatedVehicles(null).forEach(v -> System.out.println(v.getId() + " " + v.getPosition().getId()));

      vehClient.exit("Gate1");

      System.out.println("\nVEHICLES ---------");
      admClient.getUpdatedVehicles(null).forEach(v -> System.out.println(v.getId() + " " + v.getPosition().getId()));
    } catch (AdmClientException | ServiceException | UnknownPlaceException | WrongPlaceException e) {
      e.printStackTrace();
    }

  }

}
