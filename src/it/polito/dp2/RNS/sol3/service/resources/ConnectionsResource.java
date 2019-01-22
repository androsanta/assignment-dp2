package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.*;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Connections;
import it.polito.dp2.RNS.sol3.service.RnsService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path("connections")
@Api(value = "connections")
public class ConnectionsResource {

  @Context
  private UriInfo uriInfo;
  private RnsService service = new RnsService();

  @GET
  @ApiOperation(
    value = "Get connections",
    notes = "Get connections of rns, restricted to admin and returned in portion"
  )
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = Connections.class),
    @ApiResponse(code = 403, message = "Forbidden")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Connections getConnections (
    @ApiParam(value = "Specify if the client requesting the resource is an admin") @QueryParam("admin") boolean admin,
    @ApiParam(value = "Which page of the resource must be returned") @QueryParam("page") int page
  ) {
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
