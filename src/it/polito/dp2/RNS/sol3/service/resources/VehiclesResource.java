package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.*;
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
@Api(value = "vehicles")
public class VehiclesResource {

  @Context
  private UriInfo uriInfo;
  private RnsService service = new RnsService();

  @GET
  @ApiOperation(value = "Get vehicles", notes = "Get tracked vehicles in the system, restricted to admin")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = Vehicles.class),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicles getVehicles (
    @ApiParam(value = "Specify if the client requesting the resource is an admin") @QueryParam("admin") @DefaultValue("false") boolean admin,
    @ApiParam(value = "Which page of the resource must be returned") @QueryParam("page") int page,
    @ApiParam(value = "Return only vehicles that have entered into the system since this date (using format yyyy-MM-dd'T'HH:mm:ss.SSSXXX)") @QueryParam("since") String since,
    @ApiParam(value = "Filter vehicles by the specified state") @QueryParam("state") String state,
    @ApiParam(value = "Filter vehicles by the specified types") @QueryParam("type") Set<String> types
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
  @ApiOperation(value = "Create vehicle", notes = "Create a vehicle and insert it into the tracked vehicles of the system")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = Vehicle.class),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 409, message = "Conflict"),
    @ApiResponse(code = 422, message = "Unprocessable Entity")
  })
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicle createVehicle (EnterVehicle enterRequest) {
    /*
    403 -> Forbidden - entrance gate provided is not IN/INOUT
    409 -> Conflict - a vehicle with the same plateId is already in the system
    422 -> Unprocessable Entity - source or destination place cannot be found
     */

    String entranceGateId = PlacesResource.getPlaceIdFromUri(enterRequest.getEnterGate(), uriInfo.getBaseUriBuilder());
    String destinationPlaceId = PlacesResource.getPlaceIdFromUri(enterRequest.getDestination(), uriInfo.getBaseUriBuilder());

    PlaceType origin = service.getPlace(entranceGateId);
    PlaceType destination = service.getPlace(destinationPlaceId);

    if (origin == null || destination == null)
      throw new ClientErrorException(422);


    boolean isGateValid = service.getGates(null).getGate()
      .stream()
      .filter(g -> g.getType().equals(GateTypeEnum.IN) || g.getType().equals(GateTypeEnum.INOUT))
      .anyMatch(g -> g.getId().equals(entranceGateId));

    if (!isGateValid)
      throw new ClientErrorException(403);


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

    ShortestPath shortestPath = new ShortestPath();
    shortestPath.setPage(BigInteger.ONE);
    shortestPath.setTotalPages(BigInteger.ONE);
    try {
      UriBuilder baseUrl = uriInfo.getBaseUriBuilder();
      List<String> paths = service.findShortestPath(entranceGateId, destinationPlaceId);

      shortestPath.getPlace().addAll(
        paths.stream()
          .map(p -> PlacesResource.setPlaceLinks(service.getPlace(p), baseUrl).getSelf())
          .collect(Collectors.toList())
      );
    } catch (UnknownIdException | BadStateException | ServiceException e) {
      // If findShortestPath fails is not client's fault so InternalServerError is returned
      throw new InternalServerErrorException();
    }

    vehicle.setShortestPath(shortestPath);
    vehicle.setShortestPathLink(selfBuilder.path("shortestPath").toTemplate());

    if (service.addVehicle(vehicle) == null)
      throw new ClientErrorException(409);

    return vehicle;
  }

  @PUT
  @Path("{id}")
  @ApiOperation(value = "Update vehicle", notes = "Update vehicle, note that only state or position can be changed")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = Vehicle.class),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 409, message = "Conflict"),
    @ApiResponse(code = 422, message = "Unprocessable Entity")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicle updateVehicle (@ApiParam(value = "Id of the vehicle") @PathParam("id") String id, Vehicle vehicle) {
    Vehicle oldVehicle = service.getVehicle(id);

    if (oldVehicle == null || !RnsService.areVehiclesEquals(oldVehicle, vehicle))
      throw new NotFoundException();

    String vehicleId = getVehicleIdFromUri(oldVehicle.getSelf(), uriInfo.getBaseUriBuilder());
    String newPosition = vehicle.getPosition();
    String newPositionId = PlacesResource.getPlaceIdFromUri(newPosition, uriInfo.getBaseUriBuilder());


    // start from the shortest path already present in the system
    vehicle.getShortestPath().getPlace().clear();
    vehicle.getShortestPath().getPlace().addAll(
      oldVehicle.getShortestPath().getPlace()
    );

    if (!newPosition.equals(oldVehicle.getPosition())) {

      if (service.getPlace(newPositionId) != null) {

        // if position change than the vehicle is IN_TRANSIT
        vehicle.setState(VehicleStateEnum.IN_TRANSIT);

        // check if vehicle has a suggested path
        if (vehicle.getShortestPath().getPlace().size() > 0) {

          // check if the new place is on the suggested path
          int currentPosIndex = vehicle.getShortestPath().getPlace().indexOf(oldVehicle.getPosition());
          // check if next place from old place is equals to the new position
          if (currentPosIndex != -1 && vehicle.getShortestPath().getPlace().get(currentPosIndex + 1).equals(vehicle.getPosition())) {
            return service.updateVehicle(vehicleId, vehicle);
          }
        }

        // continue if place is not on suggested path or there's no suggested path
        // vehicle can move only to near places

        String oldPositionId = PlacesResource.getPlaceIdFromUri(oldVehicle.getPosition(), uriInfo.getBaseUriBuilder());
        String destinationId = PlacesResource.getPlaceIdFromUri(vehicle.getDestination(), uriInfo.getBaseUriBuilder());
        boolean isConnected = service.getPlaceConnections(oldPositionId)
          .stream()
          .anyMatch(place -> place.getId().equals(newPositionId));

        if (isConnected) {
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
                .collect(Collectors.toList())
            );
          } catch (UnknownIdException | BadStateException e) {
            throw new InternalServerErrorException();
          } catch (ServiceException e) {
            vehicle.getShortestPath().getPlace().clear();
          }

          return service.updateVehicle(vehicleId, vehicle);
        }

      }

      throw new ClientErrorException(422);
    }

    return service.updateVehicle(vehicleId, vehicle);
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "Get vehicle", notes = "Get a single tracked vehicle")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = Vehicle.class),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicle getVehicle (@ApiParam(value = "Id of the vehicle") @PathParam("id") String id) {
    Vehicle vehicle = service.getVehicle(id);
    if (vehicle == null)
      throw new NotFoundException();

    return vehicle;
  }

  @GET
  @Path("{id}/shortestPath")
  @ApiOperation(value = "Get shortest path", notes = "Get shortest path of a vehicle")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = ShortestPath.class),
    @ApiResponse(code = 404, message = "Not Found")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public ShortestPath getShortestPath (
    @ApiParam(value = "Id of the vehicle") @PathParam("id") String id,
    @ApiParam(value = "Which page of the resource must be returned") @QueryParam("page") int page
  ) {
    Vehicle vehicle = service.getVehicle(id);

    if (vehicle == null)
      throw new NotFoundException();

    return vehicle.getShortestPath();
  }

  @DELETE
  @Path("{id}")
  @ApiOperation(
    value = "Remove vehicle",
    notes = "Remove vehicle from tracked vehicles, vehicle must specify an " +
      "OUT or INOUT gate, only admin can remove it from any place"
  )
  @ApiResponses(value = {
    @ApiResponse(code = 204, message = "No Content"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 422, message = "Unprocessable Entity")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public void removeVehicle (
    @ApiParam(value = "Specify if the client requesting the resource is an admin") @QueryParam("admin") @DefaultValue("false") boolean admin,
    @ApiParam(value = "The gate from which the vehicle must exit") @QueryParam("outGate") @DefaultValue("") String outGate,
    @ApiParam(value = "The id if the vehicle") @PathParam("id") String id
  ) {
    if (admin) {
      Vehicle vehicle = service.forceRemoveVehicle(id);
      if (vehicle == null)
        throw new NotFoundException();
      return;
    }

    service.removeVehicle(id, outGate, uriInfo.getBaseUriBuilder());
  }

  public static String getVehicleIdFromUri (String uri, UriBuilder baseUrl) {
    return uri
      .replaceAll(baseUrl.clone().path("vehicles").toTemplate(), "")
      .replaceAll("/", "");
  }
}
