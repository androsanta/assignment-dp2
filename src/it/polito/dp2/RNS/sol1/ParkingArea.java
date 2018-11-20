package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;

import java.util.Set;

public class ParkingArea extends Place implements ParkingAreaReader {

  private Set<String> services;

  public ParkingArea (String id, Set<PlaceReader> nextPlaces, int capacity, Set<String> services) {
    super(id, nextPlaces, capacity);
    this.services = services;
  }

  @Override
  public Set<String> getServices () {
    return services;
  }
}
