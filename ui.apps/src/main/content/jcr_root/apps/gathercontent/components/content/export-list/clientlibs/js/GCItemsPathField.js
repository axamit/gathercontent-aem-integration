/*
 * Copyright 1997-2009 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */

/**
 * @class CQ.form.PathField
 * @extends CQ.Ext.form.ComboBox
 * <p>The PathField is an input field designed for paths with path completion
 * and a button to open a {@link CQ.BrowseDialog} for browsing the server
 * repository. It can also browse page paragraphs for advanced link generation.</p>
 * <p>The default configuration is for browsing pages below /content. Here are
 * some other typical configs:</p>
 * <ul>
 * <li>Full repository browsing, showing node names, not titles
 <pre><code>
 var pathfield = new CQ.form.PathField({
    rootPath: "/",
    predicate: "nosystem",
    showTitlesInTree: false
});
 </code></pre>
 * </li>
 * <li>Browsing only a subtree
 <pre><code>
 var pathfield = new CQ.form.PathField({
    rootPath: "/content/dam",
    showTitlesInTree: false
});
 </code></pre>
 * </li>
 * <li>Paragraph browsing (with custom link patterns)
 <pre><code>
 var pathfield = new CQ.form.PathField({
    parBrowse: true,
    linkPattern: "{0}.selector.html",
    parLinkPattern: "{0}.selector.html#{1}"
});
 </code></pre>
 * </li>
 * <li>Only (fast) path completion, but no browse dialog
 <pre><code>
 var pathfield = new CQ.form.PathField({
    hideTrigger: true,
    searchDelay: 100
});
 </code></pre>
 * </li>
 * </ul>
 * @constructor
 * Creates a new PathField.
 * @param {Object} config The config object
 */

CQ.form.GCItemsPathField = CQ.Ext.extend(CQ.Ext.form.ComboBox, {
    /**
     * @cfg {String} rootPath
     * The root path where completion and browsing starts. Use the empty string
     * for the repository root (defaults to '/content').
     */

    /**
     * @cfg {String} suffix
     * The suffix to append to the selected path, defaults to "".
     */

    /**
     * @cfg {String} rootTitle
     * Custom title for the root path.<br/><br/>
     *
     * <p>Defaults to the value of {@link #rootPath}; if that is not set, it will be
     * 'Websites' (localized), to match the default value of '/content' for the
     * {@link #rootPath}.</p>
     */

    /**
     * @cfg {String/String[]} predicate
     * The predicate(s) to pass to the server when listing resources. Use empty
     * string to browse the full Sling resource tree. Example predicates
     * are 'hierarchy', 'folder', 'hierarchyNotFile', 'nosystem' and 'siteadmin'.
     * If you want multiple predicates, pass them as array of strings.<br/><br/>
     *
     * <p>Defaults to 'siteadmin', for browsing the pages that are visible in the siteadmin.</p>
     */

    /**
     * @cfg {Boolean} showTitlesInTree
     * Whether to show the (jcr:)titles as names of the tree nodes or the
     * plain jcr node name (defaults to true).
     */

    /**
     * @cfg {Boolean} hideTrigger
     * True to disable the option to open the browse dialog (this config is
     * inherited from {@link CQ.Ext.form.TriggerField}). Defaults to false.
     */

    /**
     * @cfg {Boolean} parBrowse
     * True to allow paragraph browsing and section in a grid next to the
     * tree panel in the browse dialog. If this is enabled, it is recommended
     * to use a predicate like 'hierarchy' to have pages as leaf nodes in the tree.
     * Defaults to false.
     */

    /**
     * @cfg {String} linkPattern
     * A pattern to format links after selection in the browse dialog (using
     * {@link CQ.Util#patchText}). This is used when only a tree item is selected
     * (which is always the case if {@link #parBrowse} = false). It has only one
     * argument '{0}', which is the path from the tree. See also
     * {@link #parLinkPattern}.<br/><br/>
     *
     * <p>Defaults to '{0}.html' if {@link #parBrowse} = true, otherwise simply '{0}'.</p>
     */

    /**
     * @cfg {String} parLinkPattern
     * A pattern to format links after selection of a paragraph in the browse
     * dialog (using {@link CQ.Util#patchText}). This only applies when
     * {@link #parBrowse} = true. It has two arguments,
     * the first '{0}' is the path from the tree, the second '{1}'
     * is the paragraph. See also {@link #linkPattern}.<br/><br/>
     *
     * <p>Defaults to '{0}.html#{1}'.</p>
     */

    /**
     * @cfg {Number} searchDelay
     * The time in ms to delay the search event after the user has stopped typing.
     * This prevents the field from firing the search event after each key input.
     * Use 0 to not fire the search event at all (defaults to 200).
     */

    /**
     * @cfg {Object} treeLoader
     * The config options for the tree loader in the browse dialog.
     * See {@link CQ.Ext.tree.TreeLoader} for possible options.<br/><br/>
     *
     * <p>Defaults to '/content.ext.json' for the dataUrl and uses 'predicate' as
     * baseParam.predicate; also note that the treeLoader's createNode and getParams
     * functions are overwritten.</p>
     */

    /**
     * @cfg {Object} browseDialogCfg
     * The config for the {@link CQ.BrowseDialog}.
     * @since 5.4
     */

    /**
     * @cfg {Object} treeRoot
     * The config options for the tree root node in the browse dialog.
     * See {@link CQ.Ext.tree.TreeNode} for possible options.<br/><br/>
     *
     * <p>Defaults to {@link #rootPath} for the name and {@link #rootTitle} for the text of the root.</p>
     */

    /**
     * @cfg {Boolean} modeless
     * Indicates the control is in a modeless container (ie: not a dialog), and that
     * the BrowseDialog should be updated each time it is triggered.  (Default behaviour
     * is to instantiate a single BrowseDialog for the life of the PathField control.)
     */

    /**
     * @cfg {Boolean} escapeAmp
     * True to url-encode the ampersand character (&amp;amp;) to %26. Defaults to false
     * @since 5.5
     */

    /**
     * Remembers the last value when the last key up happened.
     * @type String
     * @private
     */
    lastValue: null,

    /**
     * The ID of the delayed search interval.
     * @type Number
     * @private
     */
    searchIntervalId: 0,

    /**
     * The panel holding the link-browser.
     * @type CQ.BrowseDialog
     * @private
     */
    browseDialog: null,

    /**
     * Returns the anchor of the selected paragraph (or an empty string if
     * no paragraph has been selected yet).
     * @return {String} The anchor
     */
    getParagraphAnchor: function () {
        return this.browseDialog.getSelectedAnchor();
    },

    /**
     * Checks if the current path is quoted. If yes the new value is decorated
     * with quotes as well.
     * @private
     */
    adjustNewValue: function (currentValue, newValue) {
        if (/^path:"/.test(currentValue)) {
            // current value starts with quotes: decorate with quotes
            // (add final quotes even if they do not exist yet - otherwise
            // the triggered search would fail ('path:"/content')
            newValue = '"' + newValue + '"';
        }
        return newValue;
    },

    /**
     * Executed on key up in the control.
     * - Checks if its value matches a path. If yes, request .pages.json
     * @private
     */
    keyup: function (comp, evt) {
        var currentValue = this.getRawValue();

        var key = evt.getKey();
        if (key === 13) {
            // [enter] hit
            this.fireEvent("search", this, currentValue);
        }

        if (currentValue === this.lastValue) {
            // value did not change (key was arrows, ctrl etc.)
            return;
        }
        this.lastValue = currentValue;

        var path = currentValue;

        if (/^\//.test(path) && /\/$/.test(path)) {
            // path starts with a slash: ignore non-absolute path (#29745)
            // path ends with a slash: request path.pages.json
            if (path === "/") {
                path = this.rootPath ? this.rootPath : "";
            }
            else {
                // remove final slash:
                path = path.replace(/\/$/, "");
            }
            this.loadStore(CQ.shared.HTTP.encodePath(path));
        }
        else if (this.searchDelay) {
            window.clearTimeout(this.searchIntervalId);
            var pc = this;
            this.searchIntervalId = window.setTimeout(function () {
                pc.fireEvent("search", pc, currentValue);
            }, this.searchDelay);
        }

    },

    /**
     * Reloads the autocompletion store with a new URL.
     * @private
     */
    loadStore: function (path) {
        this.store.proxy.api["read"].url = "/etc/cloudservices/gathercontent/test.tree.json";
        this.store.reload();
    },

    /**
     * The trigger action of the TriggerField, creates a new BrowseDialog
     * if it has not been created before, and shows it.
     * @private
     */
    onTriggerClick: function () {
        if (this.disabled) {
            return;
        }
        // lazy creation of browse dialog
        if (this.browseDialog === null || this.modeless) {
            function okHandler() {
                var path = this.getSelectedPath();
                var anchor = this.parBrowse ? this.getSelectedAnchor() : null;

                var value;
                if (anchor) {
                    value = CQ.Util.patchText(this.pathField.parLinkPattern, [path, anchor]);
                } else {
                    value = CQ.Util.patchText(this.pathField.linkPattern, path);
                }
                if (this.pathField.suffix) {
                    value += this.pathField.suffix;
                }

                this.pathField.setValue(value);

                this.pathField.fireEvent("dialogselect", this.pathField, path, anchor);
                this.hide();
            }

            var browseDialogConfig = CQ.Util.applyDefaults(this.browseDialogCfg, {
                ok: okHandler,
                // pass this to the BrowseDialog to make in configurable from 'outside'
                getSelectedPath: function () {
                    try {
                        var str = this.treePanel.getSelectionModel().getSelectedNode().getPath();
                        str = str.substr(str.indexOf('\/', str.indexOf('.tree')) + 1);
                        // console.log(str);
                        if (str.indexOf('.tree') !== -1) {
                            str = "";
                        }
                        return str;
                    } catch (e) {
                        return "";
                    }
                },
                parBrowse: this.parBrowse,
                treeRoot: this.treeRoot,
                treeLoader: this.treeLoader,
                listeners: [{
                    hide: function () {
                        if (this.pathField) {
                            this.pathField.fireEvent("dialogclose");
                        }
                    }
                },
                    {
                        beforeLoad: function (loader, node) {
                            // console.log("before in browse dialog");
                            this.dataUrl = GATHERCONTENT.getGCItemsJSONTreeServletURL($('#gcproject-select').val());//window.location.pathname.substring(0, window.location.pathname.indexOf('.')) + ".tree" + "." + $('#gcproject-select').val() + ".json";
                        }
                    }],
                loadAndShowPath: function (path) {
                    this.path = path;
                    // if the root node is the real root, we need an additional slash
                    // at the begining for selectPath() to work properly
                    if (this.pathField.rootPath === "" || this.pathField.rootPath === "/") {
                        path = GATHERCONTENT.getGCItemsJSONTreeServletURL($('#gcproject-select').val()) + "/" + path;
                        //console.log(path);
                        // path = window.location.href.substring(0, window.location.href.indexOf('.'))+".tree"+"/" + path;
                    }

                    var browseDialog = this;
                    var treePanel = this.treePanel;

                    // what to do when selectPath worked
                    function successHandler(node) {
                        // ensureVisible fails on root, ie. getParentNode() == null
                        if (node.parentNode) {
                            node.ensureVisible();
                        }
                        if (browseDialog.parBrowse) {
                            browseDialog.onSelectPage(node);
                        }
                    }

                    // string split helper function
                    function substringBeforeLast(str, delim) {
                        var pos = str.lastIndexOf(delim);
                        if (pos >= 0) {
                            return str.substring(0, pos);
                        } else {
                            return str;
                        }
                    }

                    // try to handle links created by linkPattern/parLinkPattern,
                    // such as "/content/foo/bar.html#par_sys"; needs to try various
                    // cut-offs until selectPath works (eg. /content/foo/bar)
                    // 1) try full link (path)
                    treePanel.selectPath(path, null, function (success, node) {
                        if (success && node) {
                            successHandler(node);
                        } else {
                            // 2) try and split typical anchor from (par)linkPattern
                            path = substringBeforeLast(path, "#");

                            treePanel.selectPath(path, null, function (success, node) {
                                if (success && node) {
                                    successHandler(node);
                                } else {
                                    // 3) try and split typical extension from (par)linkPattern
                                    path = substringBeforeLast(path, ".");

                                    treePanel.selectPath(path, null, function (success, node) {
                                        if (success && node) {
                                            successHandler(node);
                                        }
                                    });
                                }
                            });
                        }
                    });
                },

                pathField: this
            });

            // fix dialog width for par browse to include 3 cols of pars
            if (this.parBrowse) {
                browseDialogConfig.width = 570;
            }


            this.browseDialog = new CQ.BrowseDialog(browseDialogConfig, {});


        }

        this.browseDialog.loadAndShowPath(this.getValue());
        // console.log("init browse dialog");

        this.browseDialog.show();
        this.fireEvent("dialogopen");
    },

    constructor: function (config) {
        // set default values
        // done here, because it is already used in below applyDefaults
        if (typeof config.rootTitle === "undefined") {
            config.rootTitle = config.rootPath || "root";
        }
        if (typeof config.rootPath === "undefined") {
            config.rootPath = "/";
        }
        var rootName = config.rootPath;
        // the root path must not include a leading slash for the root tree node
        // (it's added automatically in CQ.Ext.data.Node.getPath())
        if (rootName.charAt(0) === "/") {
            rootName = rootName.substring(1);
        }
        if (typeof config.showTitlesInTree === "undefined") {
            config.showTitlesInTree = true;
        }

        var pathField = "path";
        if (config.escapeAmp) {
            pathField = "escapedPath";
            delete config.escapeAmp;
        }

        CQ.Util.applyDefaults(config, {
            linkPattern: config.parBrowse ? "{0}.html" : "{0}",
            parLinkPattern: "{0}.html#{1}",

            tpl: new CQ.Ext.XTemplate(
                '<tpl for=".">',
                '<div ext:qtip="{tooltip}" class="x-combo-list-item">',
                '<span class="cq-pathfield-completion-list-name">{label}</span>',
                '<span class="cq-pathfield-completion-list-title">{title}</span>',
                '</div>',
                '</tpl>'),
            displayField: pathField,
            treeRoot: {
                id: "root",
                name: GATHERCONTENT.getGCItemsJSONTreeServletURL($('#gcproject-select').val()).substring(1),
                text: $('#gcproject-select option[value=' + $('#gcproject-select').val() + ']').text(),
            },
            typeAhead: true,
            searchDelay: 200,
            suffix: "",
            mode: 'local',
            selectOnFocus: true,
            enableKeyEvents: true,
            validationEvent: false,
            validateOnBlur: false,
            // show a search icon
            triggerClass: "x-form-search-trigger",
            treeLoader: {

                // overwriting method to be able to intercept node labeling
                createNode: function (attr) {
                    // if (!config.showTitlesInTree) {
                    //     // no labled resources, use plain node name for tree nodes
                    //     attr.text = attr.name;
                    // }
                    // return CQ.Ext.tree.TreeLoader.prototype.createNode.call(this, attr);
                    //  debugger;
                    //  console.log(attr);
                    // attr.name=attr.name+'('+attr.id+')';
                    return CQ.Ext.tree.TreeLoader.prototype.createNode.call(this, attr);

                },
                listeners: {
                    beforeLoad: function (loader, node) {
                        this.dataUrl = GATHERCONTENT.getGCItemsJSONTreeServletURL($('#gcproject-select').val());
                    }
                }
            }
        });

        // store for autocompletion while typing
        if (!(config.store instanceof CQ.Ext.data.Store)) {
            var storeConfig = CQ.Util.applyDefaults(config.store, {
                // URL for proxy is set dynamically based on current path in loadStore()
                proxy: new CQ.Ext.data.HttpProxy({
                    url: "aa", //"etc/cloudservices/gathercontent/test.tree.json",
                    method: "GET"
                }),
                "reader": new CQ.Ext.data.JsonReader(
                    {
                        "totalProperty": "results",
                        "root": "pages",
                        "id": "path"
                    },
                    CQ.Ext.data.Record.create([
                        {
                            "name": "label",
                            "convert": function (v, rec) {
                                return CQ.shared.XSS.getXSSValue(rec.label);
                            }
                        },
                        {
                            "name": "title",
                            "mapping": CQ.shared.XSS.getXSSPropertyName("title")
                        },
                        {
                            "name": pathField
                        },
                        {
                            "name": "tooltip",
                            // have to encode this twice because the template decodes the value before 
                            // injecting it into the tooltip div
                            "convert": function (v, rec) {
                                return _g.Util.htmlEncode(_g.Util.htmlEncode(rec.path));
                            }
                        },
                        {
                            "children": "children"
                        }
                    ])
                )
            });
            config.store = new CQ.Ext.data.Store(storeConfig);
        }
        this.store = config.store;

        CQ.form.PathField.superclass.constructor.call(this, config);
    },

    initComponent: function () {
        CQ.form.PathField.superclass.initComponent.call(this);

        this.addListener("keyup", this.keyup, this);

        this.addEvents(
            /**
             * @event search
             * Fires when the enter key is hit or after the user stopped typing.
             * The period between the last key press and the firing of the event
             * is specified in {@link #searchDelay}.
             * @param {CQ.form.PathField} this
             * @param {String} value The current value of the field
             */
            'search',
            /**
             * @event dialogopen
             * Fires when the browse dialog is opened.
             * @param {CQ.form.PathField} this
             */
            "dialogopen",
            /**
             * @event dialogselect
             * Fires when a new value is selected in the browse dialog.
             * @param {CQ.form.PathField} this
             * @param {String} path The path selected in the tree of the browse dialog
             * @param {String} anchor The paragraph selected in the browse dialog (or null)
             */
            "dialogselect",
            /**
             * @event dialogclose
             * Fires when the browse dialog is closed.
             * @param {CQ.form.PathField} this
             */
            "dialogclose"
        );

        // register component as drop target
        CQ.WCM.registerDropTargetComponent(this);
    },

    getDropTargets: function () {
        var pathFieldComponent = this;
        var target = new CQ.wcm.EditBase.DropTarget(this.el, {
            "ddAccept": "*/*",
            "notifyDrop": function (dragObject, evt, data) {
                if (dragObject && dragObject.clearAnimations) {
                    dragObject.clearAnimations(this);
                }
                if (data && data.records && data.records[0]) {
                    var pathInfo = data.records[0].get("path");
                    if (pathInfo) {
                        pathFieldComponent.setValue(pathInfo);
                        return true;
                    }
                }
                return false;
            }
        });
        target.groups["media"] = true;
        target.groups["s7media"] = true;
        target.groups["page"] = true;
        return [target];
    }
});

CQ.Ext.reg("GCItemsPathField", CQ.form.GCItemsPathField);

