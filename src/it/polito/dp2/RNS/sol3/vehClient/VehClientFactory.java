package it.polito.dp2.RNS.sol3.vehClient;

import it.polito.dp2.RNS.lab3.VehClient;
import it.polito.dp2.RNS.lab3.VehClientException;

public class VehClientFactory extends it.polito.dp2.RNS.lab3.VehClientFactory {
  @Override
  public VehClient newVehClient () throws VehClientException {
    return new VehClientImpl();
  }
}
