package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.RoadSegmentReader;

public class RoadSegment extends Place implements RoadSegmentReader {

  private String name;
  private String roadName;

  public RoadSegment (String id, int capacity, String name, String roadName) {
    super(id, capacity);
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

  public void setName (String name) {
    this.name = name;
  }

  public void setRoadName (String roadName) {
    this.roadName = roadName;
  }
}
