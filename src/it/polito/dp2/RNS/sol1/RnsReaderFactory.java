package it.polito.dp2.RNS.sol1;

public class RnsReaderFactory extends it.polito.dp2.RNS.RnsReaderFactory {
  @Override
  public RnsReaderSol newRnsReader () {
    String xmlOutput = System.getProperty("it.polito.dp2.RNS.sol1.RnsInfo.file");

    if (xmlOutput == null) {
      // if no file is specified return an empty reader
      return new RnsReaderSol();
    }

    //@TODO implement library
    return new RnsReaderSol();
  }



}
