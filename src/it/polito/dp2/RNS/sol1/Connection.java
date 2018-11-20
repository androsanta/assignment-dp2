package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.PlaceReader;

public class Connection implements ConnectionReader {

  private PlaceReader origin;
  private PlaceReader destination;

  public Connection (PlaceReader origin, PlaceReader destination) {
    this.origin = origin;
    this.destination = destination;
  }

  @Override
  public PlaceReader getFrom () {
    return origin;
  }

  @Override
  public PlaceReader getTo () {
    return destination;
  }

  public void setOrigin (PlaceReader origin) {
    this.origin = origin;
  }

  public void setDestination (PlaceReader destination) {
    this.destination = destination;
  }
}
