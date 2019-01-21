package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.*;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.RnsService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("places")
@Api(value = "places")
public class PlacesResource {

  @Context
  private UriInfo uriInfo;
  private RnsService service = new RnsService();

  @GET
  @ApiOperation(value = "Get places", notes = "Get places of rns, returned in portion")
  @ApiResponse(code = 200, message = "OK", response = Places.class)
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Places getPlaces (
    @ApiParam(value = "Which page of the resource must be returned") @QueryParam("page") int page,
    @ApiParam(value = "Retrieve only places for which the id have this suffix") @QueryParam("idSuffix") String idSuffix
  ) {
    Places places = service.getPlaces(idSuffix, page);

    UriBuilder uri = uriInfo.getAbsolutePathBuilder();
    places.setRoadSegments(uri.clone().path("roadSegments").toTemplate());
    places.setParkingAreas(uri.clone().path("parkingAreas").toTemplate());
    places.setGates(uri.clone().path("gates").toTemplate());

    places.getPlace()
      .replaceAll(placeType -> setPlaceLinks(placeType, uriInfo.getBaseUriBuilder()));
    return places;
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "Get place", notes = "Get a single place by its id, restricted to admin")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = PlaceType.class),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public JAXBElement<PlaceType> getPlace (
    @ApiParam(value = "Specify if the client requesting the resource is an admin") @QueryParam("admin") @DefaultValue("false") boolean admin,
    @ApiParam(value = "Id of the place to get") @PathParam("id") String id
  ) {
    if (admin) {
      PlaceType placeType = service.getPlace(id);
      if (placeType == null)
        throw new NotFoundException();

      UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
      return new ObjectFactory().createPlace(
        setPlaceLinks(placeType, baseUriBuilder)
      );
    }

    throw new ClientErrorException(403);
  }

  @GET
  @Path("{id}/connections")
  @ApiOperation(value = "Get place connection", notes = "Get connections of the specified place, restricted to admin")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = PlaceType.class, responseContainer = "List"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public List<JAXBElement<PlaceType>> getPlaceConnections (
    @ApiParam(value = "Specify if the client requesting the resource is an admin") @QueryParam("admin") @DefaultValue("false") boolean admin,
    @ApiParam(value = "Id of the place") @PathParam("id") String id
  ) {
    if (admin) {
      List<PlaceType> placeTypes = service.getPlaceConnections(id);
      if (placeTypes == null)
        throw new NotFoundException();

      ObjectFactory of = new ObjectFactory();
      UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
      return placeTypes.stream()
        .map(p -> of.createPlace(setPlaceLinks(p, baseUriBuilder)))
        .collect(Collectors.toList());
    }

    throw new ClientErrorException(403);
  }

  @GET
  @Path("{id}/vehicles")
  @ApiOperation(value = "Get vehicles in place", notes = "Get vehicles that are currently in the specified place, restricted to admin")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = Vehicles.class),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicles getVehiclesInPlace (
    @ApiParam(value = "Specify if the client requesting the resource is an admin") @QueryParam("admin") @DefaultValue("false") boolean admin,
    @ApiParam(value = "Id of the place") @PathParam("id") String placeId
  ) {
    if (admin) {
      if (service.getPlace(placeId) == null)
        throw new NotFoundException();

      Vehicles vehicles = new Vehicles();
      vehicles.setPage(BigInteger.ONE);
      vehicles.setTotalPages(BigInteger.ONE);

      vehicles.getVehicle().addAll(
        service.getVehicles(null, null, null)
          .stream()
          .filter(vehicle -> {
            String vehicleId = VehiclesResource.getVehicleIdFromUri(vehicle.getPosition(), uriInfo.getBaseUriBuilder());
            return vehicleId.equals(placeId);
          })
          .collect(Collectors.toList())
      );

      return vehicles;
    }

    throw new ClientErrorException(403);
  }

  @GET
  @Path("roadSegments")
  @ApiOperation(value = "Get road segments", notes = "Get road segments in the system, restricted to admin")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = RoadSegments.class),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public RoadSegments getRoadSegments (
    @ApiParam(value = "Specify if the client requesting the resource is an admin") @QueryParam("admin") @DefaultValue("false") boolean admin,
    @ApiParam(value = "Get road segment only from the specified road name") @QueryParam("roadName") String roadName
  ) {
    if (admin) {
      RoadSegments roadSegments = service.getRoadSegments(roadName);
      UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
      roadSegments.getRoadSegment().replaceAll(
        rs -> (RoadSegment) setPlaceLinks(rs, baseUriBuilder)
      );

      return roadSegments;
    }

    throw new ClientErrorException(403);
  }

  @GET
  @Path("parkingAreas")
  @ApiOperation(value = "Get parking areas", notes = "Get parking areas in the system, restricted to admin")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = ParkingAreas.class),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public ParkingAreas getParkingAreas (
    @ApiParam(value = "Specify if the client requesting the resource is an admin") @QueryParam("admin") @DefaultValue("false") boolean admin,
    @ApiParam(value = "Get only parking areas that have the specified list of services") @QueryParam("service") List<String> servicesList
  ) {
    if (admin) {
      Set<String> services = servicesList == null ? null : new HashSet<>(servicesList);
      ParkingAreas parkingAreas = service.getParkingAreas(services);

      UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
      parkingAreas.getParkingArea().replaceAll(
        pa -> (ParkingArea) setPlaceLinks(pa, baseUriBuilder)
      );
      return parkingAreas;
    }

    throw new ClientErrorException(403);
  }

  @GET
  @Path("gates")
  @ApiOperation(value = "Get gates", notes = "Get gates in the system, restricted to admin")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = Gates.class),
    @ApiResponse(code = 400, message = "Bad request"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Gates getGates (
    @ApiParam(value = "Specify if the client requesting the resource is an admin") @QueryParam("admin") @DefaultValue("false") boolean admin,
    @ApiParam(value = "Get only gates of the specified type") @QueryParam("gateType") String type
  ) {
    if (admin) {
      GateType gateType = null;

      if (type != null) {
        try {
          gateType = GateType.fromValue(type);
        } catch (Exception e) {
          throw new BadRequestException();
        }
      }

      Gates gates = service.getGates(gateType);
      UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
      gates.getGate().replaceAll(
        g -> (Gate) setPlaceLinks(g, baseUriBuilder)
      );
      return gates;
    }

    throw new ClientErrorException(403);
  }

  public static PlaceType setPlaceLinks (PlaceType placeType, UriBuilder baseUrl) {
    UriBuilder self = baseUrl.clone().path("places").path(placeType.getId());
    placeType.setSelf(self.toTemplate());
    placeType.getConnection().replaceAll(
      placeId -> baseUrl.clone().path("places").path(placeId).toTemplate()
    );
    placeType.setVehicles(self.path("vehicles").toTemplate());
    return placeType;
  }

  public static String getPlaceIdFromUri (String uri, UriBuilder baseUrl) {
    return uri
      .replaceAll(baseUrl.clone().path("places").toTemplate(), "")
      .replaceAll("/", "");
  }

}
