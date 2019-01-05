package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.ModelException;
import it.polito.dp2.RNS.lab2.PathFinder;
import it.polito.dp2.RNS.lab2.PathFinderException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.sol2.PathFinderFactory;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicle;

import javax.ws.rs.InternalServerErrorException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RnsSystemDb {

  private static RnsSystemDb db = new RnsSystemDb();

  private RnsReader rnsReader;
  private PathFinder pathFinder;
  private ConcurrentHashMap<String, Vehicle> trackedVehicles;

  private RnsSystemDb () {
    try {
      // Instantiate the reader
      rnsReader = RnsReaderFactory.newInstance().newRnsReader();
      // Instantiate pathFinder and load into Neo4j the reader
      pathFinder = PathFinderFactory.newInstance().newPathFinder();
      pathFinder.reloadModel();
      // Initialize list of tracked vehicles
      trackedVehicles = new ConcurrentHashMap<>();
    } catch (RnsReaderException | PathFinderException | ServiceException | ModelException e) {
      throw new InternalServerErrorException();
    }
  }

  public static RnsSystemDb getDb () {
    return db;
  }

  public Set<RoadSegmentReader> getRoadSegments () {
    return db.rnsReader.getRoadSegments(null);
  }

}
