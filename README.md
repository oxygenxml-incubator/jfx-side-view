# jfx-side-view
A plugin that contributes a side view that can loads HTML and Javascript. It uses JavaFX.

# How to test the plugin

1. Download the release [2] and unzip it inside {oxygenInstallDir}/plugins. Make sure not to create any additional directories. The filesystem should look like this:

{oxygenInstallDir}/plugins/jfx-side-view-1.0/plugin.xml

2. Restart Oxygen. If you don't see a JFX Side view go to Window -> Show View and select it from there.

3. Open a DITA document in author mode. As you navigate though its elements, the view will update some data. There's also an action in the view to insert a paragraph.

# How it works

There are two sides involved here:

1. The actual plugin, written in Java. This plugin has access to out Java-based API and can work with the opened editors: get document data, change the document, etc

2. The JavaScript loaded in the side view's browser. This is the part that talks your server and presents metadata from it.

These two sides need to communicate with one another. This communication happens like this:

# Javascript to Java communication

The Java part injects two Java objects as global variables into the JavaScript. You will be able to call their public methods from withing JavaScript. This is how you will insert fragments, for example, into the document.

a) helper . This is an instance of com.oxygenxml.jfx.view.bridge.Bridge . It currently offers just 2 methods, one to open an editor and another to get a document controller for making changes in the document. This is an interaction layer between JavaScript and Java. You publish here all the operations that you need to call from JavaScript.

b) pluginWorkspace . This is an instance of ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace . It is a very powerful API, but it has two limitations when used from within JavaScript:

- you can't use it to make changes to the document. There's no point in getting into details, but it has something to do with specific threads on which Swing and JavaFX must run code. You can use it, still, to retrieve some data from withing the document.

- some pluginWorkspace API requires you to create specific API objects. You can't instantiate these objects from JavaScript. In this cases you will need to use the helper and offer in it a counterpart method. This method, by being on the Java side, will be able to use the full Java API.

Please see the interaction.js for examples on how these variables are being used.

# Java to Javascript communication

From within Java you can invoke JavaScript code, for example call specific methods. You can find examples inside com.oxygenxml.jfx.view.bridge.Bridge.

- when an editor is selected, it invokes the JavaScript editorSelected() method

- when an editor is selected or the caret is moved though the Author page, events are send to the JavaScript. This way you can send all sorts of context information to the JavaScript.
```
          javafx.application.Platform.runLater(() -> {
            JSObject event = (JSObject) engine.executeScript("new CustomEvent('message', { detail: {element:'" + nodeAtOffset.getName()
            + "'} });");
            JSObject window2 = (JSObject) engine.executeScript("window");
            window2.call("dispatchEvent", event);
          });
```
The event is then intercepted in interaction.js like this:
```
window.onload = function(x) {
    window.addEventListener("message", function(e) {
        document.getElementsByClassName("event")[0].innerHTML = JSON.stringify(e.detail);
    });
}
```
