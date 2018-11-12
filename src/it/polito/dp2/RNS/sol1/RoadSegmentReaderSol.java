package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RoadSegmentReader;

import java.util.Set;

public class RoadSegmentReaderSol implements RoadSegmentReader {

  private String id;
  private int capacity;
  private String name;
  private String roadName;

  public RoadSegmentReaderSol (String id, int capacity, String name, String roadName) {
    this.id = id;
    this.capacity = capacity;
    this.name = name;
    this.roadName = roadName;
  }

  @Override
  public String getName () {
    return name;
  }

  @Override
  public String getRoadName () {
    return roadName;
  }

  @Override
  public int getCapacity () {
    return capacity;
  }

  @Override
  public Set<PlaceReader> getNextPlaces () {
    return null;
  }

  @Override
  public String getId () {
    return id;
  }

}
