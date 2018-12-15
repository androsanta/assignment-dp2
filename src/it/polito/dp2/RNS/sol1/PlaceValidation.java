package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.VehicleReader;

import java.util.Set;

public class PlaceValidation {

  private PlaceValidation () {}

  private static Boolean checkSelfConnection (PlaceReader placeReader) {
    // return true if place is connected to itself
    return placeReader
      .getNextPlaces()
      .stream()
      .anyMatch(place -> place.getId().equals(placeReader.getId()));
  }

  private static Boolean checkPlaceCapacity (PlaceReader placeReader, Set<VehicleReader> vehicleReaders) {
    long count = vehicleReaders
      .stream()
      .filter(vehicle -> vehicle.getPosition().getId().equals(placeReader.getId()))
      .count();

    // return true if place capacity not exceed
    return count > placeReader.getCapacity();
  }

  public static void validateReader (RnsReader reader) throws PlaceValidationException {
    for (PlaceReader placeReader : reader.getPlaces(null)) {
      if (checkSelfConnection(placeReader)) {
        throw new PlaceValidationException("Invalid Reader content, place " + placeReader.getId() + " is connected to itself");
      }
      if (checkPlaceCapacity(placeReader, reader.getVehicles(null, null, null))) {
        throw new PlaceValidationException("Invalid Reader content, place " + placeReader.getId() + " capacity exceeded");
      }
    }
  }

}