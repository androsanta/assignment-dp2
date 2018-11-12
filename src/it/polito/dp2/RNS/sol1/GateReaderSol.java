package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.PlaceReader;

import java.util.Set;

public class GateReaderSol implements GateReader {

  private String id;
  private GateType type;
  private int capacity;
  private Set<PlaceReader> nextPlaces;

  public GateReaderSol (String id, GateType type) {
    this.id = id;
    this.type = type;
  }

  @Override
  public GateType getType () {
    return type;
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

  public void setType (GateType type) {
    this.type = type;
  }

  public void setCapacity (int capacity) {
    this.capacity = capacity;
  }
}
