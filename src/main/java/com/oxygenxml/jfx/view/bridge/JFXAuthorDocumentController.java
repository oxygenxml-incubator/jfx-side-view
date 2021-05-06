package com.oxygenxml.jfx.view.bridge;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class JFXAuthorDocumentController {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(JFXAuthorDocumentController.class.getName());

  
  private AuthorDocumentController ctrl;

  public JFXAuthorDocumentController(AuthorDocumentController ctrl) {
    this.ctrl = ctrl;
  }

  
  public void insertXMLFragment(String xmlFragment, int offset) {
    SwingUtilities.invokeLater(() -> {
      try {
        ctrl.insertXMLFragment(xmlFragment, offset);
      } catch (AuthorOperationException e) {
        logger.error(e, e);
      }
    });
  }
}
