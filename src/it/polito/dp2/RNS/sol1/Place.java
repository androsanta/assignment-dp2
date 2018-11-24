package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;

import java.util.HashSet;
import java.util.Set;

public class Place extends IdentifiedEntity implements PlaceReader {

  private Set<PlaceReader> nextPlaces = new HashSet<>();
  private int capacity;

  public Place (String id, int capacity) {
    super(id);
    this.capacity = capacity;
  }

  @Override
  public int getCapacity () {
    return capacity;
  }

  @Override
  public Set<PlaceReader> getNextPlaces () {
    return nextPlaces;
  }

  public void setCapacity (int capacity) {
    this.capacity = capacity;
  }

}
