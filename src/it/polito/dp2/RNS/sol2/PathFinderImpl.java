package it.polito.dp2.RNS.sol2;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.sol2.rest.client.jaxb.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class PathFinderImpl implements PathFinder {

  private Boolean isModelLoaded = false;
  private URI restUri;
  private Map<String, NodeResult> nodes;
  private Map<String, String> nodesById;
  private List<RelationshipResult> relationships;


  public PathFinderImpl () throws PathFinderException {
    String uri = System.getProperty("it.polito.dp2.RNS.lab2.URL");

    if (uri == null)
      throw new PathFinderException("System property 'it.polito.dp2.RNS.lab2.URL' must be set");
    restUri = UriBuilder.fromUri(uri).path("data").build();
  }

  private RnsReader loadReader () throws ModelException {
    try {
      return RnsReaderFactory.newInstance().newRnsReader();
    } catch (FactoryConfigurationError | RnsReaderException e) {
      throw new ModelException(e.getMessage());
    }
  }

  private void loadNodes (Client client, Set<PlaceReader> places) throws ServiceException, ModelException {
    WebTarget target = client.target(restUri).path("node");

    // Create Neo4j nodes
    for (PlaceReader placeReader : places) {
      // Request body, a node property
      NodeProperty nodeProperty = new NodeProperty();
      nodeProperty.setId(placeReader.getId());

      Response response;
      try {
        response = target
          .request()
          .accept(MediaType.APPLICATION_JSON)
          .post(Entity.json(nodeProperty));

        if (response.getStatus() != 201) { // created
          throw new Exception("Post failed with code " + response.getStatus());
        }
      } catch (Exception e) {
        throw new ServiceException(e);
      }

      // Convert result to NodeResult and save it
      NodeResult nodeResult = response.readEntity(NodeResult.class);
      response.close();

      // Save node info
      nodes.put(nodeResult.getData().getId(), nodeResult);
    }

    // Also keep a map between node url and node id
    nodesById = nodes
      .entrySet()
      .stream()
      .collect(Collectors.toMap(
        e -> e.getValue().getSelf(),
        Map.Entry::getKey
      ));
  }

  private void loadConnections (Client client, Set<ConnectionReader> connections) throws ServiceException {
    // Create Neo4j Connections
    for (ConnectionReader connection : connections) {
      try {
        NodeResult sourceNode = nodes.get(connection.getFrom().getId());
        NodeResult destinationNode = nodes.get(connection.getTo().getId());

        WebTarget target = client.target(sourceNode.getCreateRelationship());

        RelationshipRequest relationshipRequest = new RelationshipRequest();
        relationshipRequest.setTo(destinationNode.getSelf());
        relationshipRequest.setType(ConnectionType.CONNECTED_TO);

        Response response = target
          .request()
          .accept(MediaType.APPLICATION_JSON)
          .post(Entity.json(relationshipRequest));

        if (response.getStatus() != 201) { // created
          throw new Exception("Post failed with code " + response.getStatus());
        }

        RelationshipResult relationship = response.readEntity(RelationshipResult.class);

        relationships.add(relationship);
      } catch (Exception e) {
        throw new ServiceException(e);
      }
    }
  }

  private void deleteElement (WebTarget target) throws ServiceException {
    try {
      int status = target
        .request()
        .delete()
        .getStatus();

      if (status != 204) { // No content
        throw new Exception("Delete failed with code " + status);
      }
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  private void unloadModel (Client client) throws ServiceException {
    // If things go wrong the model should not be set as loaded
    isModelLoaded = false;

    // Delete relationships first (neo4j requirement)
    for (RelationshipResult relationship : relationships) {
      deleteElement(client.target(relationship.getSelf()));
    }
    relationships = null;

    // Delete nodes
    for (NodeResult nodeResult : nodes.values()) {
      deleteElement(client.target(nodeResult.getSelf()));
    }
    nodes = null;
    nodesById = null;
  }

  @Override
  public boolean isModelLoaded () {
    return isModelLoaded;
  }

  @Override
  public void reloadModel () throws ServiceException, ModelException {
    Client client = ClientBuilder.newClient();

    if (isModelLoaded()) {
      unloadModel(client);
    }

    RnsReader reader = loadReader();
    nodes = new HashMap<>();
    nodesById = new HashMap<>();
    relationships = new ArrayList<>();

    loadNodes(client, reader.getPlaces(null));
    loadConnections(client, reader.getConnections());

    client.close();
    isModelLoaded = true;
  }

  @Override
  public Set<List<String>> findShortestPaths (String source, String destination, int maxlength) throws UnknownIdException, BadStateException, ServiceException {
    if (!isModelLoaded()) {
      throw new BadStateException("Calling findShortestPaths while on unloaded state");
    }

    NodeResult sourceNode = nodes.get(source);
    NodeResult destinationNode = nodes.get(destination);
    if (sourceNode == null || destinationNode == null) {
      throw new UnknownIdException("Source and Destination must be a valid node id");
    }

    PathRequest pathRequest = new PathRequest();
    pathRequest.setTo(destinationNode.getSelf());
    pathRequest.setMaxDepth(BigInteger.valueOf(maxlength > 0 ? maxlength : nodes.size()));
    pathRequest.setAlgorithm(AlgorithmType.SHORTEST_PATH);

    PathRequest.Relationships relationshipsField = new PathRequest.Relationships();
    relationshipsField.setType(ConnectionType.CONNECTED_TO);
    relationshipsField.setDirection(RelationshipDirection.OUT);

    pathRequest.setRelationships(relationshipsField);

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(sourceNode.getSelf()).path("paths");
    Response response;

    try {
      response = target
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .post(Entity.json(pathRequest));

      if (response.getStatus() != 200) { // ok
        throw new Exception("Post failed with code " + response.getStatus());
      }

      List<PathResponse> pathResponses = response.readEntity(new GenericType<List<PathResponse>>() {});

      client.close();

      return pathResponses
        .stream()
        .map(pathResponse -> pathResponse.getNodes()
          .stream()
          .map(nodesById::get)
          .collect(Collectors.toList())
        )
        .collect(Collectors.toSet());
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }
}
