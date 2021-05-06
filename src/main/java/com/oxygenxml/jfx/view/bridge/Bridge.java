package com.oxygenxml.jfx.view.bridge;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import ro.sync.ecss.extensions.api.AuthorCaretListener;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.listeners.WSEditorListener;

/**
 * A bridge between JavaScript and Java. JavaScript code will be able to invoke 
 * these methods.
 *  
 * @author alex_jitianu
 */
public class Bridge {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(Bridge.class.getName());

  /**
   * Interface to Oxygen's workspace.
   */
  private PluginWorkspace pluginWorkspace;
  /**
   * The browser engine.
   */
  private WebEngine engine;
  /**
   * Keeps track of the installed hooks.
   */
  private Map<String, Boolean> hookInstalled = new HashMap<>();
  
  /**
   * Constructor.
   * @param engine 
   * 
   * @param pluginWorkspace 
   * @param resultsPresenter 
   * @param variablesResolver 
   * @param xspec The executed XSpec file.
   */
  @SuppressWarnings("restriction")
  private Bridge(
      WebEngine engine, 
      PluginWorkspace pluginWorkspace) {
    this.engine = engine;
    this.pluginWorkspace = pluginWorkspace;
    
    pluginWorkspace.addEditorChangeListener(new WSEditorChangeListener() {
      @Override
      public void editorSelected(URL editorLocation) {
        // 1. Invoke a JavaScript function.
        javafx.application.Platform.runLater(() -> engine.executeScript("editorSelected()"));
        
        // 2. Send an event.
        String fileName = pluginWorkspace.getUtilAccess().getFileName(editorLocation.toString());
        //  Send a message as well.
        javafx.application.Platform.runLater(() -> {
          JSObject event = (JSObject) engine.executeScript("new CustomEvent('message', { detail: {selectedFileName:'" + fileName
              + "'} });");
          JSObject window2 = (JSObject) engine.executeScript("window");
          window2.call("dispatchEvent", event);
        });
        
        // 3. Add a hook on the author page to fire other events.
        WSEditor editorAccess = pluginWorkspace.getEditorAccess(editorLocation, PluginWorkspace.MAIN_EDITING_AREA);
        addHook(editorAccess.getCurrentPage());
        editorAccess.addEditorListener(new WSEditorListener() {
          @Override
          public void editorPageChanged() {
            addHook(editorAccess.getCurrentPage());
          }
        });
      }
      
      @Override
      public void editorClosed(URL editorLocation) {
        hookInstalled.remove(editorLocation.toExternalForm());
      }
    }, PluginWorkspace.MAIN_EDITING_AREA);

  }

  /**
   * Installs in the Web Engine the bridge between Javascript and the Java environment.
   * Javascript code will be able to call Java methods.
   * 
   * @param engine Web Engine.
   * @param pluginWorkspace Oxygen workspace.
   * 
   * @return The installed bridge.
   */
  @SuppressWarnings("restriction")
  public static Bridge install(
      WebEngine engine, 
      PluginWorkspace pluginWorkspace) {
    JSObject window = (JSObject) engine.executeScript("window");
    Bridge value = new Bridge(engine,
        pluginWorkspace);
    
    // For those things that are easier to implement in Java and expose an API.
    // Things that should be performed on AWT can also be implemented like this.
    window.setMember("helper", value);
    
    // For when you want to use the full plugin API directly from JS.
    window.setMember("pluginWorkspace", pluginWorkspace);
    
    return value;
  }
  
  /**
   * Adds various listeners on the Author page and fires events.
   * 
   * @param currentPage Page on which to install.
   */
  @SuppressWarnings("restriction")
  protected void addHook(WSEditorPage currentPage) {
    if (currentPage instanceof WSAuthorEditorPage && 
        !hookInstalled.containsKey(currentPage.getParentEditor().getEditorLocation().toExternalForm())) {
      hookInstalled.put(currentPage.getParentEditor().getEditorLocation().toExternalForm(), true);
      WSAuthorEditorPage authorPage = (WSAuthorEditorPage) currentPage;
      AuthorCaretListener caretListener = caretEvent -> {
        try {
          AuthorNode nodeAtOffset = authorPage.getDocumentController().getNodeAtOffset(caretEvent.getOffset());
          
          javafx.application.Platform.runLater(() -> {
            JSObject event = (JSObject) engine.executeScript("new CustomEvent('message', { detail: {element:'" + nodeAtOffset.getName()
            + "'} });");
            JSObject window2 = (JSObject) engine.executeScript("window");
            window2.call("dispatchEvent", event);
          });
        } catch (BadLocationException e) {
          logger.error(e, e);
        }
      };
      
      ((WSAuthorEditorPage) currentPage).addAuthorCaretListener(caretListener);
    }
  }

  // ================================
  // Functions indented to be executed from JavaScript
  // ================================
  
  /**
   * Executed from JavaScript.
   * 
   * @param url Location to open.
   * 
   * @throws MalformedURLException Unable to build a URL.
   */
  public void open(String url) throws MalformedURLException {
    pluginWorkspace.open(new URL(url));
  }
  
  /**
   * Executed from JavaScript.
   * 
   * @param url Location to open.
   * 
   * @throws MalformedURLException Unable to build a URL.
   */
  public JFXAuthorDocumentController getControllerWrapper(AuthorDocumentController ctrl) {
    return new JFXAuthorDocumentController(ctrl);
  }
}
