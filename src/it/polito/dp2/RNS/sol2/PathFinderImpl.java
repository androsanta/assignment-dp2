package it.polito.dp2.RNS.sol2;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.sol2.rest.client.node.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathFinderImpl implements PathFinder {

  private URI restUri;
  private Map<String, NodeResult> nodes;

  public PathFinderImpl () throws PathFinderException {
    String uri = System.getProperty("it.polito.dp2.RNS.lab2.URL");

    if (uri == null)
      throw new PathFinderException("System property 'it.polito.dp2.RNS.lab2.URL' must be set");

    restUri = UriBuilder.fromUri(uri).build();
  }

  private RnsReader loadReader () throws ModelException {
    try {
      return RnsReaderFactory.newInstance().newRnsReader();
    } catch (FactoryConfigurationError | RnsReaderException e) {
      throw new ModelException(e.getMessage());
    }
  }

  private void loadNodes (WebTarget target, Set<PlaceReader> places) throws ServiceException {
    // Create Neo4j nodes
    for (PlaceReader placeReader : places) {
      // Request body, a node property
      NodeProperty nodeProperty = new NodeProperty();
      nodeProperty.setId(placeReader.getId());

      Response response;
      try {
        response = target.request()
          .accept(MediaType.APPLICATION_JSON)
          .post(Entity.json(nodeProperty));
      } catch (Exception e) {
        throw new ServiceException(e);
      }

      // Convert result to NodeResult and save it
      NodeResult nodeResult = response.readEntity(NodeResult.class);

      //@TODO validate response

      nodes.put(nodeResult.getData().getId(), nodeResult);
    }
  }

  private void loadConnections (Client client, Set<ConnectionReader> connections) throws ServiceException {
    // Create Neo4j Connections
    for (ConnectionReader connection : connections) {
      NodeResult sourceNode = nodes.get(connection.getFrom().getId());
      NodeResult destinationNode = nodes.get(connection.getTo().getId());

      WebTarget target = client.target(sourceNode.getCreateRelationship());
      Response response;

      Relationship relation = new Relationship();
      relation.setTo(destinationNode.getSelf());
      relation.setType(ConnectionType.CONNECTED_TO);

      try {
        response = target.request()
          .accept(MediaType.APPLICATION_JSON)
          .post(Entity.json(relation));
      } catch (Exception e) {
        throw new ServiceException(e);
      }

      System.out.println(response.getStatus());

    }
  }

  @Override
  public boolean isModelLoaded () {
    return nodes == null;
  }

  @Override
  public void reloadModel () throws ServiceException, ModelException {
    //@TODO delete previous data

    RnsReader reader = loadReader();
    nodes = new HashMap<>();

    Client client = ClientBuilder.newClient();

    WebTarget webTarget = client.target(restUri).path("node");
    loadNodes(webTarget, reader.getPlaces(null));

    loadConnections(client, reader.getConnections());
  }

  @Override
  public Set<List<String>> findShortestPaths (String source, String destination, int maxlength) throws UnknownIdException, BadStateException, ServiceException {
    return null;
  }
}
