package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Place;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Places;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicles;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("rns/places")
@Api(value = "/rns/places")
public class PlacesResource {

  @GET
  @ApiOperation(value = "get places", notes = "get places of rns")
  @ApiResponse(code = 200, message = "OK")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Places getPlaces () {

    return null;
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "get item", notes = "get single item")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Place getPlace (@PathParam("id") long id) {

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
