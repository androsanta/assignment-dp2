package it.polito.dp2.RNS.sol2;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CustomErrorHandler implements ErrorHandler {

  @Override
  public void warning (SAXParseException exception) throws SAXException {}

  @Override
  public void error (SAXParseException exception) throws SAXException {
    throw exception;
  }

  @Override
  public void fatalError (SAXParseException exception) throws SAXException {
    throw exception;
  }
}
