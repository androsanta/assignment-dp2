package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.EnterRequest;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicles;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("rns/vehicles")
public class VehiclesResource {

  @GET
  @ApiOperation(value = "vehicles", notes = "get tracked vehicles")
  @ApiResponse(code = 200, message = "OK")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicles getVehicles () {

    return null;
  }

  @POST
  @ApiOperation(value = "create vehicle", notes = "insert new vehicle into the tracked vehicles")
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Created"),
    @ApiResponse(code = 400, message = "Bad Request"),
  })
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicle createVehicle (EnterRequest enterRequest) {

    return null;
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "get vehicle", notes = "get single tracked vehicle")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Vehicle getVehicle (@PathParam("id") long id) {

    return null;
  }

  @DELETE
  @Path("{id}")
  @ApiOperation(value = "delete vehicle", notes = "remove vehicle from tracked vehicles")
  @ApiResponses(value = {
    @ApiResponse(code = 204, message = "No Content"),
    @ApiResponse(code = 404, message = "Not Found"),
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public void removeVehicle (@PathParam("id") long id) {

  }

}
