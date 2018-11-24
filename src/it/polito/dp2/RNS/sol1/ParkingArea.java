package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.ParkingAreaReader;

import java.util.Set;

public class ParkingArea extends Place implements ParkingAreaReader {

  private Set<String> services;

  public ParkingArea (String id, int capacity, Set<String> services) {
    super(id, capacity);
    this.services = services;
  }

  @Override
  public Set<String> getServices () {
    return services;
  }
}
