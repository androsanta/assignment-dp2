package S251579.test;

import it.polito.dp2.RNS.lab2.*;

public class Test {

  public static void main (String args[]) {
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

    PathFinder pathFinder;

    try {
      pathFinder = PathFinderFactory.newInstance().newPathFinder();
      pathFinder.reloadModel();
    } catch (PathFinderException | ModelException | ServiceException e) {
      e.printStackTrace();
    }

  }

}
