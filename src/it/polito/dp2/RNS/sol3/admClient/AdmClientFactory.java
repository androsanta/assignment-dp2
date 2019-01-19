package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.lab3.AdmClient;
import it.polito.dp2.RNS.lab3.AdmClientException;
import it.polito.dp2.RNS.lab3.ServiceException;

public class AdmClientFactory extends it.polito.dp2.RNS.lab3.AdmClientFactory {

  @Override
  public AdmClient newAdmClient () throws AdmClientException {
    try {
      return new AdmClientImpl();
    } catch (ServiceException e) {
      throw new AdmClientException(e);
    }
  }

}
