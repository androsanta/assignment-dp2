package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Connections;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Places;
import it.polito.dp2.RNS.sol3.service.RnsService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
  @ApiResponse(code = 200, message = "OK")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Connections getConnections (@QueryParam("admin") boolean admin, @QueryParam("page") int page) {
    //@TODO restrict to admin?
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

}
