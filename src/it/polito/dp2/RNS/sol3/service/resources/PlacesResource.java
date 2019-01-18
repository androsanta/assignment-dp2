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

import static it.polito.dp2.RNS.sol3.service.RnsService.getIdFromUri;

@Path("rns/places")
@Api(value = "/rns/places")
public class PlacesResource {

  @Context
  private UriInfo uriInfo;
  private RnsService service = new RnsService();

  @GET
  @ApiOperation(value = "get places", notes = "get places of rns")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "Forbidden"),
  })
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Places getPlaces (
    @QueryParam("admin") @DefaultValue("false") boolean admin,
    @QueryParam("page") int page,
    @QueryParam("idSuffix") String idSuffix
  ) {
    if (admin) {
      Places places = service.getPlaces(idSuffix, page);

      UriBuilder uri = uriInfo.getAbsolutePathBuilder();
      places.setRoadSegments(uri.clone().path("roadSegments").toTemplate());
      places.setParkingAreas(uri.clone().path("parkingAreas").toTemplate());
      places.setGates(uri.clone().path("gates").toTemplate());

      places.getPlace()
        .replaceAll(placeType -> {
          UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(placeType.getId());
          placeType.setSelf(uriBuilder.toTemplate());
          placeType.setConnections(uriBuilder.clone().path("connections").toTemplate());
          return placeType;
        });
      return places;
    }

    throw new ClientErrorException(403);
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "get item", notes = "get single item")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public JAXBElement<PlaceType> getPlace (
    @QueryParam("admin") @DefaultValue("false") boolean admin,
    @PathParam("id") String id
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
  @ApiOperation(value = "get connections", notes = "get connections of specified place")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public List<JAXBElement<PlaceType>> getPlaceConnections (
    @QueryParam("admin") @DefaultValue("false") boolean admin,
    @PathParam("id") String id
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
  @ApiOperation(value = "get connections", notes = "get connections of specified place")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "Forbidden"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicles getVehiclesInPlace (
    @QueryParam("admin") @DefaultValue("false") boolean admin,
    @PathParam("id") String placeId
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
          .filter(vehicle -> getIdFromUri(vehicle.getPosition()).equals(placeId))
          .collect(Collectors.toList())
      );

      return vehicles;
    }

    throw new ClientErrorException(403);
  }

  @GET
  @Path("roadSegments")
  @ApiOperation(value = "get road segments", notes = "get road segments")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public RoadSegments getRoadSegments (
    @QueryParam("admin") @DefaultValue("false") boolean admin,
    @QueryParam("roadName") String roadName
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
  @ApiOperation(value = "get parking areas", notes = "get parking areas")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public ParkingAreas getParkingAreas (
    @QueryParam("admin") @DefaultValue("false") boolean admin,
    @QueryParam("service") List<String> servicesList
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
  @ApiOperation(value = "get gates", notes = "get gates")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "Bad request"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Gates getGates (
    @QueryParam("admin") @DefaultValue("false") boolean admin,
    @QueryParam("gateType") String type
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
    UriBuilder self = baseUrl.clone().path("rns/places").path(placeType.getId());
    placeType.setSelf(self.toTemplate());
    placeType.setConnections(self.clone().path("connections").toTemplate());
    return placeType;
  }

}
