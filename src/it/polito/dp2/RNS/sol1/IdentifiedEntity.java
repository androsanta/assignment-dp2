package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.IdentifiedEntityReader;

public class IdentifiedEntity implements IdentifiedEntityReader {

  private String id;

  public IdentifiedEntity (String id) {
    this.id = id;
  }

  @Override
  public String getId () {
    return id;
  }

  public void setId (String id) {
    this.id = id;
  }
}
