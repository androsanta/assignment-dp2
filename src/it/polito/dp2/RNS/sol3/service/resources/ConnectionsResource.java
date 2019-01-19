package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Connections;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Places;
import it.polito.dp2.RNS.sol3.service.RnsService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.math.BigInteger;

@Path("connections")
@Api(value = "connections")
public class ConnectionsResource {

  @Context
  private UriInfo uriInfo;
  private RnsService service = new RnsService();

  @GET
  @ApiOperation(value = "get connections", notes = "get connections of rns")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Connections getConnections (@QueryParam("admin") boolean admin, @QueryParam("page") int page) {
    if (admin) {
      Connections connections = service.getConnections(page);

      connections.getConnection().replaceAll(
        connection -> {
          UriBuilder baseUrl = uriInfo.getBaseUriBuilder();
          connection.setTo(baseUrl.clone().path("places").path(connection.getTo()).toTemplate());
          connection.setFrom(baseUrl.clone().path("places").path(connection.getFrom()).toTemplate());
          return connection;
        }
      );

      return connections;
    }

    throw new ForbiddenException();
  }

}
