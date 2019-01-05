package it.polito.dp2.RNS.sol3.service;

import it.polito.dp2.RNS.sol3.rest.service.jaxb.Places;
import it.polito.dp2.RNS.sol3.service.db.RnsSystemDb;

import java.util.stream.Collectors;

public class PlacesService {

  private RnsSystemDb db = RnsSystemDb.getDb();

  public Places getPlaces () {
    Places places = new Places();

    /*places.getRoadSegment().addAll(
      db.getRoadSegments()
        .stream()
        .map()
        .collect(Collectors.toSet())
    );*/

    return places;
  }

}
