package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;

import java.util.HashSet;
import java.util.Set;

public class PlaceReaderSol implements PlaceReader {

  private String id;
  private Set<PlaceReader> nextPlaces;
  private int capacity;

  public PlaceReaderSol (String id, int capacity) {
    this.id = id;
    this.capacity = capacity;
    this.nextPlaces = new HashSet<>();
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
