package S251579.test;

import it.polito.dp2.RNS.lab2.PathFinder;
import it.polito.dp2.RNS.lab2.PathFinderFactory;
import it.polito.dp2.RNS.sol2.PathFinderImpl;

import javax.ws.rs.client.ClientBuilder;
import java.util.List;
import java.util.Set;

public class Test {

  public static void main (String args[]) {
    System.setProperty(
      "it.polito.dp2.RNS.lab2.URL",
      "http://192.168.1.5:7474/db"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab2.PathFinderFactory",
      "it.polito.dp2.RNS.sol2.PathFinderFactory"
    );
    System.setProperty(
      "it.polito.dp2.RNS.RnsReaderFactory",
      "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.seed",
      "1211530"
    );
    System.setProperty(
      "it.polito.dp2.RNS.Random.testcase",
      "1"
    );

    PathFinder pathFinder = null;
    try {
      PathFinderFactory pathFinderFactory = PathFinderFactory.newInstance();
      pathFinder = pathFinderFactory.newPathFinder();
      pathFinder.reloadModel();
      Set<List<String>> paths = pathFinder.findShortestPaths("SS0-S1", "SS0-S3", 0);
      paths.forEach(p -> p.forEach(System.out::println));
      paths = pathFinder.findShortestPaths("Wrong", "SS0-S3", 0);
      paths.forEach(p -> p.forEach(System.out::println));
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (pathFinder != null) {
        try {
          ((PathFinderImpl)pathFinder).unloadModel(ClientBuilder.newClient());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
