package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.RnsEntry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/")
@Api(value = "/")
public class RnsResource {

  @GET
  @ApiOperation(value = "entry point", notes = "api entry point")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public RnsEntry rns (@Context UriInfo uriInfo) {
    System.out.println("RNS ENTRY POINT GET");
    RnsEntry rns = new RnsEntry();
    rns.setPlaces(uriInfo.getAbsolutePathBuilder().path("places").toTemplate());
    rns.setVehicles(uriInfo.getAbsolutePathBuilder().path("vehicles").toTemplate());
    rns.setConnections(uriInfo.getAbsolutePathBuilder().path("connections").toTemplate());
    return rns;
  }

}
