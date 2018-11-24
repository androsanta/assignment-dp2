package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;

public class Gate extends Place implements GateReader {

  private GateType type;

  public Gate (String id, int capacity, GateType type) {
    super(id, capacity);
    this.type = type;
  }

  @Override
  public GateType getType () {
    return type;
  }

  public void setType (GateType type) {
    this.type = type;
  }
}
