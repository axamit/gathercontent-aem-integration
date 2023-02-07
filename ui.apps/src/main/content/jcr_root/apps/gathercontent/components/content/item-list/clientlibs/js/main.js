/*
 * Axamit, gc.support@axamit.com
 */

$(function () {
        var isMultilocation = document.URL.indexOf(".multilocation.") !== -1;

        $("#empty-list-modal").dialog({
            autoOpen: false,
            modal: true,
            resizable: false,
            width: 400,
            buttons: [
                {
                    text: "Ok",
                    click: function () {
                        $(this).dialog("close");
                    }
                }
            ]
        });

        function findTopParent(selectedRows, itemId, parentId) {
            var result = itemId;
            var parentNode = selectedRows.row("[itemId=" + parentId + "]").node();
            if (parentNode) {
                result = findTopParent(selectedRows, $(parentNode).attr("itemId"), $(parentNode).attr("parentId"));
            }
            return result;
        }

        function getHierarchyItemValues(selectedRows, itemId, parentId, hierarchyName, importPath) {
            var hierarchyValues = {};
            var topParentId = itemId;
            if (!importPath) {
                importPath = "";
            }
            var importPaths = [];
            importItemsJson[topParentId].forEach(function (item, index) {
                importPaths.push(item.importPath + importPath);
            });
            hierarchyValues.hierarchyName = hierarchyName;
            hierarchyValues.topParentId = topParentId;
            hierarchyValues.importPaths = importPaths;

            var parentNode = selectedRows.row("[itemId=" + parentId + "]", {selected: true}).node();
            if (parentNode) {
                hierarchyName = "   " + hierarchyName;
                topParentId = $(parentNode).attr("itemId");
                importPath = "/" + $(parentNode).attr("validName") + importPath;
                hierarchyValues = getHierarchyItemValues(selectedRows, topParentId, $(parentNode).attr("parentId"), hierarchyName, importPath);
            }
            return hierarchyValues;
        }

        function updateChildrenPaths(datatable, id, newValue) {
            var currentNode = datatable.row("[itemId=" + id + "]").node();
            $(currentNode).find('.item-import-path').val(newValue);
            var parentPath = newValue + "/" + $(currentNode).attr("validName");
            var childrenNodes = datatable.rows("[parentId=" + id + "]").nodes();
            childrenNodes.each(function (item) {
                updateChildrenPaths(datatable, $(item).attr('itemId'), parentPath);
            });
        }

        function initPathFieldsSingleImport(table) {
            table.page.len(-1).draw();
            $('.root-import-path').each(function (index, element) {
                CQ.Ext.onReady(function () {
                    new CQ.Ext.form.FormPanel({
                        id: "import-path-form-panel-" + $(element).attr('id'),
                        renderTo: document.getElementById($(element).attr('id')),
                        border: false,
                        hideLabels: true,
                        items: [
                            {
                                cls: "item-import-path",
                                id: "import-path-" + $(element).attr('id'),
                                rootPath: "/content",
                                xtype: "pathfield",
                                value: $(element).data("importpath"),
                                width: 300,
                                "listeners": {
                                    "dialogselect": function (pathField, path) {
                                        GATHERCONTENT.pathFieldEmptyDialogSelectCheck(pathField, path);
                                        var datatable = $('#review-table').DataTable();
                                        var childrenNodes = datatable.rows("[parentId=" + $(element).attr('itemId') + "]").nodes();
                                        childrenNodes.each(function (item) {
                                            updateChildrenPaths(datatable, $(item).attr('itemId'), pathField.getValue() + "/" +
                                                $(datatable.row("[itemId=" + $(element).attr('itemId') + "]").node()).attr('validName'));
                                        });
                                    },
                                    "change": function (field) {
                                        var datatable = $('#review-table').DataTable();
                                        var childrenNodes = datatable.rows("[parentId=" + $(element).attr('itemId') + "]").nodes();
                                        childrenNodes.each(function (item) {
                                            updateChildrenPaths(datatable, $(item).attr('itemId'), field.getValue() + "/" +
                                                $(datatable.row("[itemId=" + $(element).attr('itemId') + "]").node()).attr('validName'));
                                        });
                                    }
                                }
                            }
                        ]
                    });

                });
            });
            table.page.len(10).draw();
        }

        function buildSingleImportReviewTable(importData, jQueryTableElement) {
            var options = {
                order: [],
                fnCreatedRow: function (nRow, aData, iDataIndex) {
                    $(nRow).attr("parentId", aData.parentId);
                    $(nRow).attr("itemId", aData.itemId);
                    $(nRow).attr("validName", aData.validName);
                    $(nRow).attr("title", aData.title);
                    $(nRow).attr("isRoot", aData.isRoot);
                },
                columns: [
                    GATHERCONTENT.dataTablesColumns.status,
                    GATHERCONTENT.dataTablesColumns.hierarchyTitle,
                    {
                        title: "Specify Mapping", data: 'itemId', name: "specify-mapping",
                        render: function (data, type, row) {
                            var specifyMappingSelect = '';
                            importItemsJson[data].forEach(function (item, index) {
                                if (item.mappingPath && item.mappingName) {
                                    specifyMappingSelect += "<option value='" + item.mappingPath + "'>" + item.mappingName + "</option>";
                                }
                            });
                            if (!GCStringUtils.isEmpty(specifyMappingSelect)) {
                                specifyMappingSelect = '<select class="specify-mapping-select full-width">'
                                    + specifyMappingSelect + '</select>';
                            }
                            return specifyMappingSelect;
                        }
                    },
                    {
                        title: "Import Path", data: 'itemId', name: "import-path",
                        render: function (data, type, row) {
                            var importPath = row.mappings[0].importPath;
                            if (row.isRoot) {
                                return '<div id="CQ"><div class="root-import-path" itemId="' + row.itemId
                                    + '" data-importpath="' + importPath + '" id="pathfield_' + row.itemId + '"></div></div>'
                                    + '<div class="error-validation" id="error-invalid-import-path-'
                                    + row.itemId + '" style="display: none"></div>';
                            } else {
                                return '<input class="item-import-path full-width child-import-path table-text-input" id="item-import-path-' + row.itemId
                                    + '" disabled type="text" value="' + importPath + '">';
                            }
                        }
                    },
                    GATHERCONTENT.dataTablesColumns.templateName
                ],
                searching: false
            };

            return GATHERCONTENT.createAndFillDataTable(jQueryTableElement, options, importData.items);
        }

        function updateMultilocationChildrenPaths(datatable, id, newValues) {
            var currentNode = datatable.row("[itemId=" + id + "]").node();
            $(currentNode).find('.specify-location-select').each(function (selectIndex, selectItem) {
                var specifyLocationSelectOptions = "<option value=''>Select</option>";
                var currentValue = $(selectItem).find('option:selected').val();
                newValues.forEach(function (optionItem, optionIndex) {
                    var selected = '';
                    if (currentValue === optionItem) {
                        selected = ' selected="true" ';
                    }
                    specifyLocationSelectOptions += "<option" + selected + " value='" + optionItem + "'>" + optionItem + "</option>";
                });
                $(selectItem).html(specifyLocationSelectOptions);
            });
            newValues.forEach(function (optionItem, optionIndex) {
                newValues[optionIndex] = optionItem + "/" + $(currentNode).attr("validName");
            });
            var childrenNodes = datatable.rows("[parentId=" + id + "]").nodes();
            childrenNodes.each(function (item) {
                updateMultilocationChildrenPaths(datatable, $(item).attr('itemId'), $.extend([], newValues));
            });
        }

        function multilocationImportPathfieldChangeListener(itemId) {
            var newValues = [];
            var datatable = $('#review-table').DataTable();
            var selfNode = datatable.row("[itemId=" + itemId + "]").node();
            $(selfNode).find('.item-import-path').each(function (index, item) {
                var pathfield = CQ.Ext.getCmp($(item).attr('id'));
                if (!pathfield.disabled) {
                    newValues.push(pathfield.getValue() + '/' + $(selfNode).attr('validName'));
                }
            });
            var childrenNodes = datatable.rows("[parentId=" + itemId + "]").nodes();
            childrenNodes.each(function (item) {
                updateMultilocationChildrenPaths(datatable, $(item).attr('itemId'), $.extend([], newValues));
            });
        }

        function initPathFieldsMultilocationImport(table) {
            table.page.len(-1).draw();
            $('.root-import-path').each(function (index, element) {
                CQ.Ext.onReady(function () {
                    new CQ.Ext.form.FormPanel({
                        id: "import-path-form-panel-" + $(element).attr('id'),
                        renderTo: document.getElementById($(element).attr('id')),
                        border: false,
                        hideLabels: true,
                        items: [
                            {
                                cls: "item-import-path",
                                id: "import-path-" + $(element).attr('id'),
                                disabled: true,
                                rootPath: "/content",
                                xtype: "pathfield",
                                value: $(element).data("importpath"),
                                width: 300,
                                "listeners": {
                                    "dialogselect": function (pathField, path) {
                                        GATHERCONTENT.pathFieldEmptyDialogSelectCheck(pathField, path);
                                        multilocationImportPathfieldChangeListener($(element).attr('itemId'));
                                    },
                                    "change": function () {
                                        multilocationImportPathfieldChangeListener($(element).attr('itemId'));
                                    }
                                }
                            }
                        ]
                    });

                });
            });
            table.page.len(10).draw();
        }

        function buildMultilocationImportReviewTable(importData, jQueryTableElement) {
            var options = {
                order: [],
                fnCreatedRow: function (nRow, aData, iDataIndex) {
                    $(nRow).attr("parentId", aData.parentId);
                    $(nRow).attr("itemId", aData.itemId);
                    $(nRow).attr("validName", aData.validName);
                    $(nRow).attr("title", aData.title);
                    $(nRow).attr("isRoot", aData.isRoot);
                },
                columns: [
                    GATHERCONTENT.dataTablesColumns.status,
                    GATHERCONTENT.dataTablesColumns.hierarchyTitle,
                    {
                        title: "Specify Mapping", data: 'mappings', name: "specify-mapping",
                        render: function (data, type, row) {
                            var specifyMappingCheckboxes = '';
                            data.forEach(function (item, index) {
                                specifyMappingCheckboxes += '<input class="mapping-checkbox" id="mapping-checkbox_' + row.itemId + "_" + index
                                    + '" type="checkbox" data-mappingname="' + item.mappingName
                                    + '" value="' + item.mappingPath + '" data-isroot="' + row.isRoot + '">' + item.mappingName + '<br>';
                            });
                            specifyMappingCheckboxes += '<div class="error-validation" id="error-no-checked-mapping-' + row.itemId + '" style="display: none">Select at least one mapping</div>';
                            return specifyMappingCheckboxes;
                        }
                    },
                    {
                        title: "Import Path", data: 'mappings', name: "import-path",
                        render: function (data, type, row) {
                            if (row.isRoot) {
                                var specifyLocationPathfields = '';
                                data.forEach(function (item, index) {
                                    specifyLocationPathfields += '<div id="CQ"><div class="root-import-path" itemId="'
                                        + row.itemId + '" data-importpath="' + item.importPath
                                        + '" id="pathfield_' + row.itemId + '_' + index + '"></div></div>'
                                        + '<div class="error-validation" id="error-invalid-import-path-'
                                        + row.itemId + '_' + index + '" style="display: none">Path does not exist</div>'
                                });
                                return specifyLocationPathfields;
                            } else {
                                var specifyLocationSelects = '';
                                data.forEach(function (item, outIndex) {
                                    specifyLocationSelects += '<select id="specify-location-select_' + row.itemId + '_' + outIndex
                                        + '" disabled class="specify-location-select full-width">';
                                    specifyLocationSelects += "<option value=''>Select</option>";
                                    specifyLocationSelects += '</select><br>';
                                });
                                return specifyLocationSelects;
                            }
                        }
                    },
                    GATHERCONTENT.dataTablesColumns.templateName
                ],
                searching: false
            };

            return GATHERCONTENT.createAndFillDataTable(jQueryTableElement, options, importData.items);
        }

        function buildItemsToImport() {
            var importData = {};
            var checked = [];
            var selectedRows = $('#item-list-table').DataTable().rows({selected: true});
            selectedRows.nodes().each(function (item) {
                var selected = {};
                selected.itemId = $(item).attr("itemId");
                selected.folderUuid = $(item).attr("folderUuid");
                selected.validName = $(item).attr("validName");
                selected.title = $(item).attr("title");
                var hierarchyValues = getHierarchyItemValues(selectedRows, selected.itemId, selected.folderUuid, selected.title);
                var rootId = hierarchyValues.topParentId;
                selected.isRoot = rootId === selected.itemId;
                var mappings = [];
                importItemsJson[selected.itemId].forEach(function (item, index) {
                    var mapping = {};
                    mapping.mappingName = item.mappingName;
                    mapping.mappingPath = item.mappingPath;
                    mapping.importPath = hierarchyValues.importPaths[index];
                    mappings.push(mapping);
                });
                selected.mappings = mappings;
                selected.hierarchyTitle = hierarchyValues.hierarchyName;
                selected.status = $(item).find('.item-status').text().trim();
                selected.template = $(item).find('.item-template').text().trim();
                selected.color = $(item).find(".coloredspan").css("background-color");
                checked.push(selected);
            });
            importData.items = checked;
            var changeStatusSelect = $('#change-status-select').find('option:selected');
            var newStatusId = $(changeStatusSelect).val();
            var newStatusName = $(changeStatusSelect).text();
            var newStatusColor = $(changeStatusSelect).data('color');
            if (newStatusId && newStatusName) {
                importData.newStatusId = newStatusId;
                importData.newStatusName = newStatusName;
                importData.newStatusColor = newStatusColor;
            }
            importData.projectName = $("#import-project-select").find("option:selected").text().trim();
            return importData;
        }

        $("#import-items").click(function () {
            if ($('#item-list-table').DataTable().rows({selected: true}).count() < 1) {
                $("#empty-list-modal").dialog("open");
                return false;
            }
            var importData = buildItemsToImport();
            var table;
            if (!isMultilocation) {
                table = buildSingleImportReviewTable(importData, $('#review-table'));
                initPathFieldsSingleImport(table);
            } else {
                table = buildMultilocationImportReviewTable(importData, $('#review-table'));
                initPathFieldsMultilocationImport(table);
            }
            $(table.column('import-path:name').header()).width(285);

            if (importData.newStatusId && importData.newStatusName) {
                $('#status-change-header').html('Status in GC will be changed to <span class="coloredspan" style="background-color:' + importData.newStatusColor + '"></span> ' + importData.newStatusName);
                $('#status-change-header').show();
            } else {
                $('#status-change-header').hide();
            }
            $('#import-table-wrapper').hide();
            $('#review-table-wrapper').show();
            GATHERCONTENT.attachLeavePageHandlers();
        });

        $("#review-table").on("click", ".mapping-checkbox", function () {
            var isRoot = $(this).data('isroot');
            var checkboxId = $(this).attr('id');

            if ($(this).prop('checked')) {
                if (isRoot) {
                    var pathfield = CQ.Ext.getCmp(checkboxId.replace('mapping-checkbox', 'import-path-pathfield'));
                    pathfield.enable();
                } else {
                    $('#' + checkboxId.replace('mapping-checkbox', 'specify-location-select')).prop('disabled', false);
                }
            } else {
                if (isRoot) {
                    var pathfield = CQ.Ext.getCmp(checkboxId.replace('mapping-checkbox', 'import-path-pathfield'));
                    pathfield.disable();
                } else {
                    $('#' + checkboxId.replace('mapping-checkbox', 'specify-location-select')).prop('disabled', true);
                }
            }
            multilocationImportPathfieldChangeListener(findTopParent($('#review-table').DataTable().rows(),
                $(this).closest("tr").attr('itemId'), $(this).closest("tr").attr('parentId')));
        });

        function fetchItemsToImport(reviewTable) {
            reviewTable.page.len(-1).draw();

            var importData = {};
            var checked = [];
            var selectedRows = reviewTable.rows();
            selectedRows.nodes().each(function (item) {
                var selected = {};
                selected.itemId = $(item).attr("itemId");
                selected.parentId = $(item).attr("parentId");
                selected.validName = $(item).attr("validName");
                selected.title = $(item).attr("title");
                selected.hierarchyTitle = $(item).find('.item-title').text();
                selected.status = $(item).find('.item-status').text().trim();
                selected.template = $(item).find('.item-template').text().trim();
                selected.color = $(item).find(".coloredspan").css("background-color");

                var mappings = [];
                if (!isMultilocation) {
                    var mapping = {};
                    var mappingSelect = $(item).find('.specify-mapping-select').find('option:selected');
                    if (mappingSelect.length) {
                        mapping.mappingPath = mappingSelect.val().trim();
                        mapping.mappingName = mappingSelect.text().trim();
                    }
                    mapping.importPath = $(item).find('input.item-import-path').val().trim();
                    mappings.push(mapping);
                } else {
                    var checkedMappings = $(item).find('.mapping-checkbox:checked');
                    checkedMappings.each(function (index, mappingCheckbox) {
                        var mapping = {};
                        mapping.mappingName = $(mappingCheckbox).data('mappingname');
                        mapping.mappingPath = $(mappingCheckbox).val();
                        var mappingCheckboxId = $(mappingCheckbox).attr('id');
                        if ($(item).attr('isRoot') === 'true') {
                            mapping.importPath = $('#' + mappingCheckboxId.replace('mapping-checkbox', 'import-path-pathfield')).val();
                        } else {
                            mapping.importPath = $('#' + mappingCheckboxId.replace('mapping-checkbox', 'specify-location-select')).find('option:selected').val();
                        }
                        mappings.push(mapping);
                    });
                }
                selected.mappings = mappings;
                checked.push(selected);
            });

            importData.items = checked;
            var changeStatusSelect = $('#change-status-select').find('option:selected');
            var newStatusId = $(changeStatusSelect).val();
            var newStatusName = $(changeStatusSelect).text();
            var newStatusColor = $(changeStatusSelect).data('color');
            if (newStatusId && newStatusName) {
                importData.newStatusId = newStatusId;
                importData.newStatusName = newStatusName;
                importData.newStatusColor = newStatusColor;
            }
            importData.projectName = $("#import-project-select").find("option:selected").text().trim();
            reviewTable.page.len(10).draw();
            return importData;
        }

        function splitMultipleItems(importData) {
            var splittedImportData = $.extend({}, importData);
            var newItems = [];
            importData.items.forEach(function (importItem) {
                if (importItem.mappings) {
                    if (importItem.mappings.length > 0) {
                        importItem.mappings.forEach(function (mappingItem) {
                            var splittedItem = $.extend({}, importItem);
                            splittedItem.mappingPath = mappingItem.mappingPath;
                            splittedItem.mappingName = mappingItem.mappingName;
                            splittedItem.importPath = mappingItem.importPath;
                            delete splittedItem.mappings;
                            newItems.push(splittedItem);
                        });
                    }
                }

            });
            splittedImportData.items = newItems;
            return splittedImportData;
        }

        function isValidMultilocationReviewTable(reviewTable) {
            var isValid = true;
            var errorPage;
            var itemsPerPage = reviewTable.page.len();
            var page = reviewTable.page();
            reviewTable.page.len(-1).draw();

            $('.item-import-path').removeClass('error-validation');
            $('.specify-location-select').removeClass('error-validation');

            reviewTable.rows().nodes().each(function (item) {
                var currentPage = Math.trunc((item.rowIndex - 1) / itemsPerPage);
                var itemId = $(item).attr("itemId");
                var parentId = $(item).attr("parentId");
                var checkedMappings = $(item).find('.mapping-checkbox:checked');
                if (checkedMappings.length < 1) {
                    $('#error-no-checked-mapping-' + itemId).show();
                    isValid = false;
                    if (!errorPage) {
                        errorPage = currentPage;
                    }
                } else {
                    $('#error-no-checked-mapping-' + itemId).hide();
                }

                checkedMappings.each(function (outIndex, outMappingCheckbox) {
                    var outMappingCheckboxId = $(outMappingCheckbox).attr('id');
                    var outImportPath = '';
                    var outElement;
                    if ($(item).attr('isRoot') === 'true') {
                        outElement = $('#' + outMappingCheckboxId.replace('mapping-checkbox', 'import-path-pathfield'));
                        outImportPath = $(outElement).val();
                        $.ajax({
                            url: outImportPath + '.valid.html',
                            type: 'GET',
                            async: false,
                            success: function (xhr) {
                                if (xhr.toString() === "no-valid") {
                                    isValid = false;
                                    if (!errorPage) {
                                        errorPage = currentPage;
                                    }
                                    $('#' + outMappingCheckboxId.replace('mapping-checkbox_', 'error-invalid-import-path-')).show();
                                } else {
                                    $('#' + outMappingCheckboxId.replace('mapping-checkbox_', 'error-invalid-import-path-')).hide();
                                }
                            }
                        });
                    } else {
                        outElement = $('#' + outMappingCheckboxId.replace('mapping-checkbox', 'specify-location-select'));
                        outImportPath = $(outElement).find('option:selected').val();
                    }
                    if (!outImportPath) {
                        $(outElement).addClass('error-validation');
                        isValid = false;
                        if (!errorPage) {
                            errorPage = currentPage;
                        }
                    }
                    checkedMappings.each(function (insideIndex, insideMappingCheckbox) {
                        var insideMappingCheckboxId = $(insideMappingCheckbox).attr('id');
                        var insideImportPath = '';
                        var insideElement;
                        if ($(item).attr('isRoot') === 'true') {
                            insideElement = $('#' + insideMappingCheckboxId.replace('mapping-checkbox', 'import-path-pathfield'));
                            insideImportPath = $(insideElement).val();
                        } else {
                            insideElement = $('#' + insideMappingCheckboxId.replace('mapping-checkbox', 'specify-location-select'));
                            insideImportPath = $(insideElement).find('option:selected').val();
                        }
                        if ((outElement.selector !== insideElement.selector)
                            && insideImportPath && outImportPath.startsWith(insideImportPath)) {
                            $(outElement).addClass('error-validation');
                            $(insideElement).addClass('error-validation');
                            isValid = false;
                            if (!errorPage) {
                                errorPage = currentPage;
                            }
                        }
                    });
                });
            });
            reviewTable.page.len(itemsPerPage).draw();
            if (isValid) {
                reviewTable.page(page).draw('page');
            } else {
                if (!errorPage) {
                    errorPage = 0;
                }
                reviewTable.page(errorPage).draw('page');
            }
            return isValid;
        }

        function isValidSingleImportReviewTable(reviewTable) {
            var isValidTable = true;
            var errorPage;
            var itemsPerPage = reviewTable.page.len();
            var page = reviewTable.page();
            reviewTable.page.len(-1).draw();
            var validatedImportPaths = {};
            reviewTable.rows().nodes().each(function (item) {
                var currentPage = Math.trunc((item.rowIndex - 1) / itemsPerPage);
                var itemId = $(item).attr("itemId");
                if ($(item).attr('isRoot') === 'true') {
                    var importPathSelector = 'import-path-pathfield_' + itemId;
                    var importPath = $('#' + importPathSelector).val().trim();
                    var isValidPath;
                    var errorMessage;
                    if (GCStringUtils.isEmpty(importPath)) {
                        isValidPath = false;
                        errorMessage = "This field is required.";
                    } else {
                        errorMessage = "Path does not exist.";
                        if (validatedImportPaths.hasOwnProperty(importPath)) {
                            isValidPath = validatedImportPaths[importPath];
                        } else {
                            $.ajax({
                                url: importPath + '.valid.html',
                                type: 'GET',
                                async: false,
                                success: function (xhr) {
                                    if (xhr.toString() === "no-valid") {
                                        isValidPath = validatedImportPaths[importPath] = false;
                                    } else {
                                        isValidPath = validatedImportPaths[importPath] = true;
                                    }
                                }
                            });
                        }
                    }
                    if (isValidPath) {
                        $('#' + importPathSelector.replace('import-path-pathfield_', 'error-invalid-import-path-')).html('').hide();
                    } else {
                        isValidTable = false;
                        if (!errorPage) {
                            errorPage = currentPage;
                        }
                        $('#' + importPathSelector.replace('import-path-pathfield_', 'error-invalid-import-path-')).html(errorMessage).show();
                    }
                }
            });
            reviewTable.page.len(itemsPerPage).draw();
            if (isValidTable) {
                reviewTable.page(page).draw('page');
            } else {
                if (!errorPage) {
                    errorPage = 0;
                }
                reviewTable.page(errorPage).draw('page');
            }
            return isValidTable;
        }

        $("#confirm-review").click(function () {
            var reviewTable = $('#review-table').DataTable();

            var isValid = true;
            if (isMultilocation) {
                isValid = isValidMultilocationReviewTable(reviewTable);
            } else {
                isValid = isValidSingleImportReviewTable(reviewTable);
            }
            if (!isValid) {
                return;
            }

            var importData = fetchItemsToImport(reviewTable);

            var options = {
                order: [],
                fnCreatedRow: function (nRow, aData, iDataIndex) {
                    $(nRow).attr("parentId", aData.parentId);
                    $(nRow).attr("itemId", aData.itemId);
                    $(nRow).attr("validName", aData.validName);
                    $(nRow).attr("title", aData.title);
                },
                columns: [
                    GATHERCONTENT.dataTablesColumns.status,
                    GATHERCONTENT.dataTablesColumns.hierarchyTitle,
                    {
                        title: "Specify Mapping", data: 'mappings',
                        render: function (data, type, row) {
                            var mappings = '';
                            data.forEach(function (item, index) {
                                if (item.mappingName) {
                                    mappings += '<input class="mappingsConfirm full-width" disabled type="text" value="' + GCStringUtils.escapeHTML(item.mappingName) + '">'
                                }
                            });
                            return mappings;
                        }
                    },
                    {
                        title: "Import Path", data: 'mappings',
                        render: function (data, type, row) {
                            var mappings = '';
                            data.forEach(function (item, index) {
                                mappings += '<input class="full-width" disabled type="text" value="' + item.importPath + '">'
                            });
                            return mappings;
                        }
                    },
                    GATHERCONTENT.dataTablesColumns.templateName
                ],
                searching: false
            };
            GATHERCONTENT.createAndFillDataTable($('#confirmation-table'), options, importData.items).draw();
            $('#review-table-wrapper').hide();
            $('#confirmation-table-wrapper').show();
        });

        $("#confirm-import").click(function () {
            var itemsToImport = fetchItemsToImport($('#review-table').DataTable());
            itemsToImport = splitMultipleItems(itemsToImport);
            var pagePath = $("#confirm-import").data("pagepath");
            var url = pagePath + ".gcimporter.json";

            $('#confirmation-table-wrapper').hide();
            GATHERCONTENT.detachLeavePageHandlers();
            GATHERCONTENT.sendItemsToProcess(url, pagePath, itemsToImport);
        });

        $("#import-project-select").change(function () {
            let projectid = $(this).find('option:selected').val();
            let path = $('#confirm-import').data('path');
            let spinnerTarget = document.getElementById('import-table');
            let spinner = new Spinner().spin();
            $.ajax({
                url: path + ".ajax.projectId-" + projectid + ".html",
                type: "GET",
                cache: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Accept-Encoding", "gzip, deflate");
                    $(spinnerTarget).addClass('grayout');
                    spinnerTarget.appendChild(spinner.el);
                },
                success: function (data) {
                    $("#import-table").html(data);
                    const table = GATHERCONTENT.initImportUpdateTable('item-list-table');
                    GATHERCONTENT.fillFilter(table, '#status-filter', '.item-status', 'Select Status');
                    GATHERCONTENT.fillFilter(table, '#template-filter', '.item-template', 'Select Template');
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log("error");
                },
                complete: function () {
                    spinner.stop();
                    $(spinnerTarget).removeClass('grayout');
                }
            });
        });

        $("#cancel-review").click(function () {
            $('#review-table-wrapper').hide();
            $('#import-table-wrapper').show();
            GATHERCONTENT.detachLeavePageHandlers();
        });

        $("#cancel-import").click(function () {
            $('#confirmation-table-wrapper').hide();
            $('#review-table-wrapper').show();
        });
    }
);

$('document').ready(function () {
    var table = GATHERCONTENT.initImportUpdateTable('item-list-table');
    GATHERCONTENT.fillFilter(table, '#status-filter', '.item-status', 'Select Status');
    GATHERCONTENT.fillFilter(table, '#template-filter', '.item-template', 'Select Template');
    $('#title-filter').on('keyup', (function () {
        $('#item-list-table').DataTable().column(2).search(this.value, false, false, false).rows().deselect().draw();
        $('#all-items-checkbox').prop('checked', false);
    }));
    $('#status-filter').change(function () {
        $('#item-list-table').DataTable().column(4).search(this.value, false, false, false).rows().deselect().draw();
        $('#all-items-checkbox').prop('checked', false);
    });
    $('#template-filter').change(function () {
        $('#item-list-table').DataTable().column(3).search(this.value, false, false, false).rows().deselect().draw();
        $('#all-items-checkbox').prop('checked', false);
    });
});


