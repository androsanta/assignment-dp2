package S251579.test;

import it.polito.dp2.RNS.lab2.PathFinder;
import it.polito.dp2.RNS.lab2.PathFinderFactory;

public class Test {

  public static void main (String args[]) throws Exception {
    System.setProperty(
      "it.polito.dp2.RNS.lab2.URL",
      "http://192.168.1.5:7474/db/data"
    );
    System.setProperty(
      "it.polito.dp2.RNS.lab2.PathFinderFactory",
      "it.polito.dp2.RNS.sol2.PathFinderFactory"
    );
    System.setProperty(
      "it.polito.dp2.RNS.RnsReaderFactory",
      "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl"
    );

    PathFinderFactory pathFinderFactory = PathFinderFactory.newInstance();
    PathFinder pathFinder = pathFinderFactory.newPathFinder();
    pathFinder.reloadModel();
  }

}
