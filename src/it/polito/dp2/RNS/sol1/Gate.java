package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.PlaceReader;

import java.util.Set;

public class Gate extends Place implements GateReader {

  private GateType type;

  public Gate (String id, Set<PlaceReader> nextPlaces, int capacity, GateType type) {
    super(id, nextPlaces, capacity);
    this.type = type;
  }

  @Override
  public GateType getType () {
    return null;
  }

  public void setType (GateType type) {
    this.type = type;
  }
}
