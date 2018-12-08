package it.polito.dp2.RNS.sol2;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.sol2.rest.client.jaxb.*;
import org.xml.sax.SAXException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

public class PathFinderImpl implements PathFinder {

  private Boolean isModelLoaded = false;
  private URI restUri;
  private Map<String, NodeResult> nodes;
  private List<RelationshipResult> relationships;

  private JAXBContext jaxbContext;
  private Validator validator;


  public PathFinderImpl () throws PathFinderException {
    String uri = System.getProperty("it.polito.dp2.RNS.lab2.URL");

    if (uri == null)
      throw new PathFinderException("System property 'it.polito.dp2.RNS.lab2.URL' must be set");
    restUri = UriBuilder.fromUri(uri).build();

    try {
      SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
      Schema schema = sf.newSchema(new File("custom/restClient.xsd"));

      validator = schema.newValidator();
      validator.setErrorHandler(new CustomErrorHandler());

      jaxbContext = JAXBContext.newInstance("it.polito.dp2.RNS.sol2.rest.client.jaxb");
    } catch (JAXBException | SAXException e) {
      throw new PathFinderException(e);
    }
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
      ObjectFactory of = new ObjectFactory();
      JAXBSource source;

      // Request body, a node property
      NodeProperty nodeProperty = new NodeProperty();
      nodeProperty.setId(placeReader.getId());

      try {
        // Validate nodeProperty (non empty id)
        // if element cannot be validated then something is wrong with the model
        source = new JAXBSource(jaxbContext, of.createNodeProperty(nodeProperty));
        validator.validate(source);
      } catch (JAXBException | SAXException | IOException e) {
        throw new ModelException(e);
      }

      Response response;
      try {
        response = target.request()
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

      try {
        // Validate response
        // if response cannot be validated something is wrong with the service
        source = new JAXBSource(jaxbContext, of.createNodeResult(nodeResult));
        validator.validate(source);
      } catch (JAXBException | SAXException | IOException e) {
        throw new ServiceException(e);
      }

      // Save node info
      nodes.put(nodeResult.getData().getId(), nodeResult);
    }
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

        ObjectFactory of = new ObjectFactory();
        JAXBSource source = new JAXBSource(jaxbContext, of.createRelationshipRequest(relationshipRequest));
        validator.validate(source);

        Response response = target.request()
          .accept(MediaType.APPLICATION_JSON)
          .post(Entity.json(relationshipRequest));

        if (response.getStatus() != 201) { // created
          throw new Exception("Post failed with code " + response.getStatus());
        }

        RelationshipResult relationship = response.readEntity(RelationshipResult.class);

        source = new JAXBSource(jaxbContext, of.createRelationshipResult(relationship));
        validator.validate(source);

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
    relationships = new ArrayList<>();

    loadNodes(client, reader.getPlaces(null));
    loadConnections(client, reader.getConnections());

    client.close();
    isModelLoaded = true;
  }

  @Override
  public Set<List<String>> findShortestPaths (String source, String destination, int maxlength) throws UnknownIdException, BadStateException, ServiceException {
    return null;
  }
}
