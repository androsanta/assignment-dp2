package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.math.BigInteger;

@Path("rns/places")
@Api(value = "/rns/places")
public class PlacesResource {

  @GET
  @ApiOperation(value = "get places", notes = "get places of rns")
  @ApiResponse(code = 200, message = "OK")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Places getPlaces () {

    Places places = new Places();

    ParkingArea parkingArea = new ParkingArea();
    parkingArea.setId("ciao");

    Gate gate = new Gate();
    gate.setId("come");

    places.getPlace().add(parkingArea);
    places.getPlace().add(gate);

    PlaceType placeType = new PlaceType();
    placeType.setId("stao");
    places.getPlace().add(placeType);

    places.setPage(BigInteger.valueOf(1));
    places.setTotalPages(BigInteger.valueOf(100));

    return places;
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "get item", notes = "get single item")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public JAXBElement<PlaceType> getPlace (@PathParam("id") String id) {

    return null;
  }

  @GET
  @Path("{id}/vehicles")
  @ApiOperation(value = "get vehicles", notes = "get tracked vehicles that are currently in the specified place")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicles getVehiclesInPlace (@PathParam("id") long id) {

    return null;
  }

  @GET
  @Path("{id}/connections")
  @ApiOperation(value = "get connections", notes = "get connections of specified place")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicles getPlaceConnections (@PathParam("id") long id) {

    return null;
  }

}
