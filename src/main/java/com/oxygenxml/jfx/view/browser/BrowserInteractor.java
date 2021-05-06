package com.oxygenxml.jfx.view.browser;

/**
 * Receives notifications from the browser.
 */
public interface BrowserInteractor {

	/**
	 * A message intercepted on the JavaScript alert() method.
	 */
	void alert(String message);
	/**
	 * The page was loaded.
	 */
	void pageLoaded();
}
