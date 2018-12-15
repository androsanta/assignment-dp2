package it.polito.dp2.RNS.sol1;

public class PlaceValidationException extends Exception {

  private static final long serialVersionUID = 1L;

  public PlaceValidationException() {
  }

  public PlaceValidationException(String message) {
    super(message);
  }

  public PlaceValidationException(Throwable cause) {
    super(cause);
  }

  public PlaceValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public PlaceValidationException(String message, Throwable cause, boolean enableSuppression,
                           boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
