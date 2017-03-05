package de.uni_leipzig.dbs.formRepository.evaluation.exception;

/**
 * Created by christen on 28.02.2017.
 */
public class AnnotationException extends Exception {

  public AnnotationException() {
    super();
  }

  public AnnotationException(String message) {
    super(message);
  }

  public AnnotationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AnnotationException(Throwable cause) {
    super(cause);
  }
}
