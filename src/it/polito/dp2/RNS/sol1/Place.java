package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;

import java.util.Set;

public class Place implements PlaceReader {

  private String id;
  private Set<PlaceReader> nextPlaces;
  private int capacity;

  public Place (String id, Set<PlaceReader> nextPlaces, int capacity) {
    this.id = id;
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

  @Override
  public String getId () {
    return id;
  }

  public void setId (String id) {
    this.id = id;
  }

  public void setCapacity (int capacity) {
    this.capacity = capacity;
  }

}
