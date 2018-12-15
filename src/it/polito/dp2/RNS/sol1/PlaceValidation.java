package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.VehicleReader;

import java.util.Set;

public class PlaceValidation {

  private PlaceValidation () {}

  private static Boolean checkSelfConnection (PlaceReader placeReader) {
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

    return count <= placeReader.getCapacity();
  }

  public static void validateReader (RnsReader reader) throws PlaceValidationException {
    long validPlaces = reader
      .getPlaces(null)
      .stream()
      .filter(PlaceValidation::checkSelfConnection)
      .filter(place -> checkPlaceCapacity(place, reader.getVehicles(null, null, null)))
      .count();

    if (validPlaces != reader.getPlaces(null).size()) {
      throw new PlaceValidationException("Invalid Reader content, capacity exceeded or place is connected to itself");
    }
  }

}