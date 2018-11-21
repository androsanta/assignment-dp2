package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;

import java.util.Calendar;

public class Vehicle extends IdentifiedEntity implements VehicleReader {

  private VehicleType type;
  private Calendar entryTime;
  private PlaceReader origin, destination, position;
  private VehicleState state;

  public Vehicle (String id, VehicleType type, Calendar entryTime, PlaceReader origin, PlaceReader destination, PlaceReader position, VehicleState state) {
    super(id);
    this.type = type;
    this.entryTime = entryTime;
    this.origin = origin;
    this.destination = destination;
    this.position = position;
    this.state = state;
  }

  @Override
  public VehicleType getType () {
    return type;
  }

  @Override
  public Calendar getEntryTime () {
    return entryTime;
  }

  @Override
  public PlaceReader getDestination () {
    return destination;
  }

  @Override
  public PlaceReader getOrigin () {
    return origin;
  }

  @Override
  public PlaceReader getPosition () {
    return position;
  }

  @Override
  public VehicleState getState () {
    return state;
  }

  public void setType (VehicleType type) {
    this.type = type;
  }

  public void setEntryTime (Calendar entryTime) {
    this.entryTime = entryTime;
  }

  public void setOrigin (PlaceReader origin) {
    this.origin = origin;
  }

  public void setDestination (PlaceReader destination) {
    this.destination = destination;
  }

  public void setPosition (PlaceReader position) {
    this.position = position;
  }

  public void setState (VehicleState state) {
    this.state = state;
  }
}
