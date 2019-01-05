package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Places;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("rns/connections")
@Api(value = "/rns/connections")
public class ConnectionsResource {

  @GET
  @ApiOperation(value = "get connections", notes = "get connections of rns")
  @ApiResponse(code = 200, message = "OK")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Places getConnections () {

    return null;
  }

}
