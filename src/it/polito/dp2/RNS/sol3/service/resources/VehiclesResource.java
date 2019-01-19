package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.jaxrs.PATCH;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.RnsService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Path("vehicles")
public class VehiclesResource {

  @Context
  private UriInfo uriInfo;
  private RnsService service = new RnsService();

  @GET
  @ApiOperation(value = "vehicles", notes = "get tracked vehicles")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicles getVehicles (
    @QueryParam("admin") @DefaultValue("false") boolean admin,
    @QueryParam("page") int page,
    @QueryParam("since") String since,
    @QueryParam("state") String state,
    @QueryParam("type") Set<String> types
  ) {
    if (admin) {
      GregorianCalendar calendar = null;
      VehicleStateEnum vehicleState;
      Set<VehicleTypeEnum> vehicleTypes;

      try {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        if (since != null) {
          calendar = new GregorianCalendar();
          calendar.setTime(format.parse(since));
        }

        vehicleState = state == null ? null : VehicleStateEnum.fromValue(state);
        vehicleTypes = types.stream().map(VehicleTypeEnum::fromValue).collect(Collectors.toSet());
      } catch (Exception e) {
        throw new BadRequestException();
      }

      Vehicles vehicles = new Vehicles();

      vehicles.setPage(BigInteger.ONE);
      vehicles.setTotalPages(BigInteger.ONE);

      vehicles.getVehicle().addAll(
        service.getVehicles(calendar, vehicleState, vehicleTypes)
      );

      return vehicles;
    }

    throw new ClientErrorException(403);
  }

  @POST
  @ApiOperation(value = "create vehicle", notes = "insert new vehicle into the tracked vehicles")
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Created"),
    @ApiResponse(code = 400, message = "Bad Request"),
  })
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicle createVehicle (EnterVehicle enterRequest) {

    String gateId = PlacesResource.getPlaceIdFromUri(enterRequest.getEnterGate(), uriInfo.getBaseUriBuilder());

    boolean isGateValid = service.getGates(null).getGate()
      .stream()
      .filter(g -> g.getType().equals(GateTypeEnum.IN) || g.getType().equals(GateTypeEnum.INOUT))
      .anyMatch(g -> g.getId().equals(gateId));

    if (!isGateValid)
      throw new BadRequestException();

    PlaceType destination = service.getPlace(
      PlacesResource.getPlaceIdFromUri(enterRequest.getDestination(), uriInfo.getBaseUriBuilder())
    );

    if (destination == null)
      throw new BadRequestException();

    Vehicle vehicle = new Vehicle();
    vehicle.setPlateId(enterRequest.getPlateId());
    vehicle.setState(VehicleStateEnum.IN_TRANSIT);
    vehicle.setType(enterRequest.getVehicleType());
    vehicle.setOrigin(enterRequest.getEnterGate());
    vehicle.setPosition(enterRequest.getEnterGate());
    vehicle.setDestination(enterRequest.getDestination());

    UriBuilder selfBuilder = uriInfo.getAbsolutePathBuilder().path(vehicle.getPlateId());
    vehicle.setSelf(selfBuilder.toTemplate());

    try {
      vehicle.setEntryTime(
        DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar())
      );
    } catch (DatatypeConfigurationException e) {
      throw new InternalServerErrorException();
    }

    try {
      UriBuilder baseUrl = uriInfo.getBaseUriBuilder();
      List<String> paths = service.findShortestPath(
        PlacesResource.getPlaceIdFromUri(vehicle.getPosition(), uriInfo.getBaseUriBuilder()),
        PlacesResource.getPlaceIdFromUri(vehicle.getDestination(), uriInfo.getBaseUriBuilder())
      );

      ShortestPath shortestPath = new ShortestPath();
      shortestPath.setPage(BigInteger.ONE);
      shortestPath.setTotalPages(BigInteger.ONE);
      shortestPath.getPlace().addAll(
        paths.stream()
          .map(p -> PlacesResource.setPlaceLinks(service.getPlace(p), baseUrl).getSelf())
          .filter(p -> !p.equals(vehicle.getPosition()))
          .collect(Collectors.toList())
      );
      vehicle.setShortestPath(shortestPath);
      vehicle.setShortestPathLink(selfBuilder.path("shortestPath").toTemplate());
    } catch (ServiceException | UnknownIdException | BadStateException e) {
      throw new InternalServerErrorException();
    }

    if (service.addVehicle(vehicle) == null)
      throw new BadRequestException();

    return vehicle;
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "get vehicle", notes = "get single tracked vehicle")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicle getVehicle (@PathParam("id") String id) {
    Vehicle vehicle = service.getVehicle(id);
    if (vehicle == null)
      throw new NotFoundException();

    return vehicle;
  }

  @PATCH
  @Path("{id}")
  @ApiOperation(value = "update vehicle", notes = "update state or position of a vehicle")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 404, message = "Not Found")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicle updateVehicle (@PathParam("id") String id, UpdateVehicle updateVehicle) {
    System.out.println("update vehicle " +  updateVehicle.getState() + " " + updateVehicle.getPosition());

    Vehicle oldVehicle = service.getVehicle(id);

    System.out.println("vehicle " + oldVehicle);

    if (oldVehicle == null)
      throw new NotFoundException();

    String vehicleId = getVehicleIdFromUri(oldVehicle.getSelf(), uriInfo.getBaseUriBuilder());

    Vehicle vehicle = new Vehicle();
    vehicle.setShortestPath(oldVehicle.getShortestPath());
    vehicle.setShortestPathLink(oldVehicle.getShortestPathLink());
    vehicle.setSelf(oldVehicle.getSelf());
    vehicle.setPlateId(oldVehicle.getPlateId());
    vehicle.setPosition(oldVehicle.getPosition());
    vehicle.setOrigin(oldVehicle.getOrigin());
    vehicle.setDestination(oldVehicle.getDestination());
    vehicle.setEntryTime(oldVehicle.getEntryTime());
    vehicle.setState(oldVehicle.getState());
    vehicle.setType(oldVehicle.getType());

    if (updateVehicle.getState() != null) {
      System.out.println("changing vehicle state");

      vehicle.setState(updateVehicle.getState());
      vehicle = service.updateVehicle(vehicleId, vehicle);
      if (vehicle == null)
        throw new InternalServerErrorException(); // or not found?
      return vehicle;
    }


    String newPosition = updateVehicle.getPosition();
    String newPositionId = PlacesResource.getPlaceIdFromUri(newPosition, uriInfo.getBaseUriBuilder());
    if (service.getPlace(newPositionId) != null) {
      System.out.println("changing vehicle position");
      if (!oldVehicle.getState().equals(VehicleStateEnum.IN_TRANSIT))
        throw new BadRequestException();

      System.out.println("vehicle is IN_TRANSIT");

      // check if vehicle has a suggested path

      if (oldVehicle.getShortestPath().getPlace().size() > 0) {
        System.out.println("vehicle has a suggested path");
        // check if the new place is on the suggested path
        if (newPosition.equals(oldVehicle.getShortestPath().getPlace().get(0))) {
          System.out.println("vehicle continue on suggested path");
          vehicle.setPosition(newPosition);
          vehicle.getShortestPath().getPlace().remove(0);
          vehicle = service.updateVehicle(vehicleId, vehicle);

          if (vehicle == null)
            throw new InternalServerErrorException();
          return vehicle;
        }
      }

      System.out.println("vehicle has no suggested path, or is deviating from it");

      // continue if place is not on suggested path or there's no suggested path
      // vehicle can move only to near places

      String oldPositionId = PlacesResource.getPlaceIdFromUri(oldVehicle.getPosition(), uriInfo.getBaseUriBuilder());
      String destinationId = PlacesResource.getPlaceIdFromUri(oldVehicle.getDestination(), uriInfo.getBaseUriBuilder());

      boolean isConnected = service.getPlaceConnections(oldPositionId)
        .stream()
        .anyMatch(place -> place.getId().equals(newPositionId));

      if (isConnected) {
        vehicle.setPosition(newPosition);

        System.out.println("requested position is reachable");

        try {
          UriBuilder baseUrl = uriInfo.getBaseUriBuilder();
          List<String> shortestPath = service.findShortestPath(
            newPositionId,
            destinationId
          );
          vehicle.getShortestPath().getPlace().clear();
          vehicle.getShortestPath().getPlace().addAll(
            shortestPath
              .stream()
              .map(p -> PlacesResource.setPlaceLinks(service.getPlace(p), baseUrl).getSelf())
              .filter(p -> !p.equals(newPosition))
              .collect(Collectors.toList())
          );
          System.out.println("new shortest path computed");
        } catch (UnknownIdException | BadStateException e) {
          throw new InternalServerErrorException();
        } catch (ServiceException e) {
          vehicle.getShortestPath().getPlace().clear();
          System.out.println("is not possible to compute a shortest path");
        }

        vehicle = service.updateVehicle(vehicleId, vehicle);

        if (vehicle == null)
          throw new InternalServerErrorException();
        return vehicle;
      }

      System.out.println("requested position is not reachable from the current position");
    }

    throw new BadRequestException();
  }

  @GET
  @Path("{id}/shortestPath")
  @ApiOperation(value = "update vehicle", notes = "update state or position of a vehicle")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Not Found")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public ShortestPath getShortestPath (@PathParam("id") String id, @QueryParam("page") int page) {
    Vehicle vehicle = service.getVehicle(id);

    if (vehicle == null)
      throw new NotFoundException();

    return vehicle.getShortestPath();
  }

  @DELETE
  @Path("{id}")
  @ApiOperation(value = "delete vehicle", notes = "remove vehicle from tracked vehicles")
  @ApiResponses(value = {
    @ApiResponse(code = 204, message = "No Content"),
    @ApiResponse(code = 400, message = "Bad request"),
    @ApiResponse(code = 404, message = "Not Found")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public void removeVehicle (
    @QueryParam("admin") @DefaultValue("false") boolean admin,
    @PathParam("id") String id
  ) {
    service.removeVehicle(id, admin, uriInfo.getBaseUriBuilder());
  }

  public static String getVehicleIdFromUri (String uri, UriBuilder baseUrl) {
    return uri
      .replaceAll(baseUrl.clone().path("vehicles").toTemplate(), "")
      .replaceAll("/", "");
  }
}
