package de.uni_leipzig.dbs.formRepository.exception;

/**
 * Created by christen on 20.04.2017.
 */
public class SelectionBuildException extends Exception {

  public SelectionBuildException() {
  }

  public SelectionBuildException(String message) {
    super(message);
  }

  public SelectionBuildException(String message, Throwable cause) {
    super(message, cause);
  }

  public SelectionBuildException(Throwable cause) {
    super(cause);
  }

  public SelectionBuildException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
