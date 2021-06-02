/** 
 * Injected variables:
 * 
 * contextElement - an instance of ro.sync.ecss.extensions.api.node.AuthorNode. The form control is added over this node.
 * pluginWorkspace- an instance of ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace
 * 
 */
 
 
/**
 * Initializes the form control with the values from the document.
 */
function editorSelected () {
	if (pluginWorkspace.getCurrentEditorAccess(0) != null) {
      var editorLocation = pluginWorkspace.getCurrentEditorAccess(0).getEditorLocation();
      document.getElementsByClassName("called.method")[0].innerHTML = "Selected editor: <b>" + editorLocation + "</b>";
	}
}

/**
 * Opens the HTML in an Oxygen editor.
 */
function openHTMLInOxygen() {
    helper.open(window.location.href)
}

/**
 * Adds a new paragraph inside the document by using Oxygen's API.
 */
function addPara() {
    // Create the fragment.
    var fragment = "<p>A new paragraph inserted from Javascript.</p>";
    // We insert the fragment before the context element.
    var currentPage = pluginWorkspace.getCurrentEditorAccess(0).getCurrentPage();
    var ctrl = currentPage.getDocumentController();
    var offset = currentPage.getCaretOffset();
    var contextElement = ctrl.getNodeAtOffset(offset);
    var offset = contextElement.getStartOffset();
    
    try {
        helper.getControllerWrapper(ctrl)[ "insertXMLFragment(java.lang.String,int)"](fragment, offset);
    } catch (e) {
        console.log(e);
    }
    
}

/* 
 * Intercepts various events fired from the Java side.
 */
window.onload = function(x) {
    window.addEventListener("message", function(e) {
        document.getElementsByClassName("event")[0].innerHTML = JSON.stringify(e.detail);
    });
}

