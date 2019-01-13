package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.PlacesService;
import org.apache.tools.ant.taskdefs.condition.Not;

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

@Path("rns/vehicles")
public class VehiclesResource {

  @Context
  private UriInfo uriInfo;
  private PlacesService service = new PlacesService();

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

    String gateId = getIdFromUri(enterRequest.getEnterGate());

    boolean isGateValid = service.getGates(null).getGate()
      .stream()
      .filter(g -> g.getType().equals(GateTypeEnum.IN) || g.getType().equals(GateTypeEnum.INOUT))
      .anyMatch(g -> g.getId().equals(gateId));

    if (!isGateValid)
      throw new BadRequestException();

    PlaceType destination = service.getPlace(
      getIdFromUri(enterRequest.getDestination())
    );

    if (destination == null)
      throw new BadRequestException();

    Vehicle vehicle = new Vehicle();
    vehicle.setPlateId(enterRequest.getPlateId());
    vehicle.setState(VehicleStateEnum.IN_TRANSIT);
    vehicle.setType(enterRequest.getVehicleType());
    vehicle.setOrigin(enterRequest.getEnterGate());
    vehicle.setPosition(enterRequest.getEnterGate());
    vehicle.setSelf(uriInfo.getAbsolutePathBuilder().path(vehicle.getPlateId()).toTemplate());
    vehicle.setDestination(enterRequest.getDestination());

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
        getIdFromUri(vehicle.getPosition()),
        getIdFromUri(vehicle.getDestination())
      );
      vehicle.getPlace().addAll(
        paths.stream()
          .map(p -> PlacesResource.setPlaceLinks(service.getPlace(p), baseUrl).getSelf())
          .filter(p -> !p.equals(vehicle.getPosition()))
          .collect(Collectors.toList())
      );
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
    service.removeVehicle(id, admin);
  }

  public static String getIdFromUri (String uri) {
    String[] split = uri.split("/");
    return split[split.length - 1];
  }

}
