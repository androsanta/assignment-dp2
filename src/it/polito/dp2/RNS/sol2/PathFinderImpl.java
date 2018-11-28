package it.polito.dp2.RNS.sol2;

import it.polito.dp2.RNS.lab2.*;

import java.util.List;
import java.util.Set;

public class PathFinderImpl implements PathFinder {
  @Override
  public boolean isModelLoaded () {
    return false;
  }

  @Override
  public void reloadModel () throws ServiceException, ModelException {

  }

  @Override
  public Set<List<String>> findShortestPaths (String source, String destination, int maxlength) throws UnknownIdException, BadStateException, ServiceException {
    return null;
  }
}
