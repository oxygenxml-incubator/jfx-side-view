package com.oxygenxml.jfx.view;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.oxygenxml.jfx.view.bridge.Bridge;
import com.oxygenxml.jfx.view.browser.BrowserInteractor;
import com.oxygenxml.jfx.view.browser.SwingBrowserPanel;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Plugin extension - workspace access extension.
 */
public class JFXViewPluginExtension implements WorkspaceAccessPluginExtension {
  /**
   * Logging.
   */
  private static Logger logger = Logger.getLogger(JFXViewPluginExtension.class);
  /**
   * The ID of the side view.
   */
  private static final String JFX_SIDE_VIEW = "JFXSideView";
  /**
   * Name of a system property that tells us if this plugin is in developer mode.
   */
  private static final String DEVELOPER_MODE = "jfx.developer.mode";
  // We need to keep this reference otherwise the GC will take it. It's a JFX bug.
  @SuppressWarnings("unused")
  private Bridge install;

  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  @SuppressWarnings("restriction")
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    // Keep JFX running.
    javafx.application.Platform.setImplicitExit(false);
    
	  pluginWorkspaceAccess.addViewComponentCustomizer(viewInfo -> {
      if(JFX_SIDE_VIEW.equals(viewInfo.getViewID())) {

        SwingBrowserPanel browser = new SwingBrowserPanel(Boolean.valueOf(System.getProperty(DEVELOPER_MODE)));
        browser.setHandler(new BrowserInteractor() {
          @Override
          public void pageLoaded() {
            install = Bridge.install(browser.getWebEngine(), pluginWorkspaceAccess);
          }

          @Override
          public void alert(String message) {
            if (logger.isDebugEnabled()) {
              logger.debug("LOG: " + message);
            }
          }
        });
        
        try {
          File baseDir = WorkspaceAccessPlugin.getInstance().getDescriptor().getBaseDir();
          File html = new File(baseDir, "resources/interaction.html");
          browser.loadURL(html.toURI().toURL());
        } catch (MalformedURLException e) {
          logger.error(e, e);
        }
        
        viewInfo.setComponent(browser);
        viewInfo.setTitle("JFX Side View");
      } 
    }); 
  }
  
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
   */
  public boolean applicationClosing() {
	  //You can reject the application closing here
    return true;
  }
}