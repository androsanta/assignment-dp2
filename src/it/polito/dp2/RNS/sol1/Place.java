package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;

import java.util.Set;

public class Place extends IdentifiedEntity implements PlaceReader {

  private Set<PlaceReader> nextPlaces;
  private int capacity;

  public Place (String id, Set<PlaceReader> nextPlaces, int capacity) {
    super(id);
    this.nextPlaces = nextPlaces;
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
