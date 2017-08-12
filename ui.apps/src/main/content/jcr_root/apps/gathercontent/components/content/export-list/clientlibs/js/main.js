(function ($) {
    $.fn.mRenderAEMTitle = function (data, type, full) {
        return '<b>' + GCStringUtils.escapeHTML(full.title) +
            '</b></br>' + GCStringUtils.escapeHTML(full.path);
    };
    $.fn.applyStyleForTable = function (idOfTableDiv) {
        $('#' + idOfTableDiv + '_length').addClass('tableFilter');
    };
    $.fn.clickOnTargetItemDialog = function (id, value) {
        var thisJquery = $('input[id="' + id + '"]');
        var newVal = value;
        var pageLength = $('select[name="confirmation-table_length"]').val();
        var path = id;
        path = path.substr(path.lastIndexOf('-/') + 1);

        var oldVal = $(thisJquery).data('oldval');
        $(thisJquery).data('oldval', newVal);

        var table = $('#confirmation-table').DataTable();
        var pageNumber = table.page.info().page;
        table.page.len(-1).draw();
        $().applyStyleForTable('confirmation-table');
        var children = $('[id^="import-path-' + path + '/"]');
        children.each(function () {
            var val = $(this).val();
            if (oldVal === undefined || oldVal === "") {
                if (newVal !== "") {
                    val = newVal + "/" + val;
                }
            }
            else {
                if (newVal === "") {
                    oldVal = oldVal + '/';
                }
                val = val.replace(oldVal, newVal);
                oldVal = oldVal.substring(0, oldVal.length - 1);
            }
            $(this).val(val);
        });
        table.page.len(pageLength).draw();
        table.page(pageNumber).draw('page');

    };
    $.fn.hierarchyView = function (table) {
        //hierarchy view
        var dataFromTable = table.rows().data();
        var root = table.row(0).data();

        var rows = table.rows();
        $(table.row(0).node()).attr('root', true);

        dataFromTable.each(function (value, index) {
            var test = value.path.indexOf(root.path + '/') !== -1;
            if (test) {
                var restPath = value.path.substring(root.path.length);
                var depth = restPath.replace(/[^/]/g, "").length;
                if (depth > 0) {
                    while (depth > 0) {
                        value.title = '\xa0' + '\xa0' + value.title;
                        value.path = '\xa0' + '\xa0' + value.path;
                        depth = depth - 1;
                    }
                }
                table.row(index).data(value);
            }
            else {
                $(table.row(index).node()).attr('root', true);
                root = value;
            }
            $(table.row(index).node()).attr('data-page-path', $().cleanStringFromHierarchy(value.path));
        });
    };
    $.fn.loadExportTable = function (data) {
        var options = {
            autoWidth: false,
            select: {
                style: 'multi',
                selector: 'td:first-child'
            },
            columns: [
                {
                    orderable: false,
                    className: 'select-checkbox',
                    width: '5%',
                    title: "<input id='all-items-checkbox' type='checkbox' class='check-box-first-step'>",
                    defaultContent: ''
                },
                {
                    title: "AEM Title",
                    mRender: $().mRenderAEMTitle,
                    // mRender: function (data, type, full) {
                    //     return '<b>' + GCStringUtils.escapeHTML(full.title) +
                    //         '</b></br>' + GCStringUtils.escapeHTML(full.path);
                    // },
                    //escapeHTML
                    //$("<div/>").text(full.title).html()
                    defaultContent: ''
                }
            ]
        };

        var table = GATHERCONTENT.createAndFillDataTable($('#export-table'), options, data);
        $('#all-items-checkbox').click(function () {
            if ($('#all-items-checkbox').prop('checked')) {
                table.rows().select();
            } else {
                table.rows().deselect();
            }
        });
        $().hierarchyView(table);
        $().applyStyleForTable('export-table');
        table.draw();
    };

    $.fn.cleanStringFromHierarchy = function (str) {
        var cleanedStr;
        if (!(typeof str === "undefined")) {
            cleanedStr = str.replace(/\xa0/g, "");
        }
        else {
            cleanedStr = str;
        }
        return cleanedStr;
    };
    $.fn.validateExportSelection = function (table, pageLength) {

        var pageNumber = table.page.info().page;
        var length = table.rows().count();
        var validateRes = true;
        var result = null;
        $.each(table.rows().eq(0), function (index) {
            var row = table.row(index);
            var data = row.data();
            var path = $().cleanStringFromHierarchy(data.path);
            var mappingName = "";
            if (data.type === "page") {
                var mapping = $('select[data-path="' + path + '"].mapping');
                mappingName = mapping.val();
                if (mappingName === null || mappingName === 'Choose here') {
                    $('span[id="text-information-' + path + '"]').html("Select mapping.");
                    validateRes = false;
                    problemPage = parseInt(index / pageLength, 10);
                    var screenYPosition = $(table.row(index).node()).offset().top;
                    result = {'problemPage': problemPage, 'screenPos': screenYPosition};
                    return false;
                } else {
                    $('span[id="text-information-' + path + '"]').html("");
                }
            }

        });
        return result;

    };
    $.fn.fetchItemsToFinalExport = function () {
        var table = $('#confirmation-table').DataTable();
        var exportData = {items: []};
        var pageNumber = table.page.info().page;
        var pageLength = $('select[name="confirmation-table_length"]').val();
        table.page.len(-1).draw();
        var newItemNames = $('input[id^="new-item-name-"]');
        var targetItems = $('[id^="import-path-"]');

        var validationResult = $.fn.validateExportSelection(table, pageLength);
        if (validationResult !== null) {
            exportData = null;
        }
        else {
            table.rows().every(function (rowIdx, tableLoop, rowLoop) {
                var data = this.data();
                var path = $().cleanStringFromHierarchy(data.path);
                var title = $().cleanStringFromHierarchy(data.title);
                var mappingName = "";
                var templateName = "Custom Template";
                if (data.type === "page") {
                    var mapping = $('select[data-path="' + path + '"].mapping');
                    mappingName = decodeURIComponent(mapping.val());
                    var choosenOption = mapping.children('[value="' + mapping.val() + '"]');
                    templateName = decodeURIComponent(choosenOption.attr('data-templatename'));
                    var mappingPath = choosenOption.attr('data-mappingPath');
                }
                var pathfieldObject = CQ.Ext.getCmp("import-path-" + path);
                var targetItem = "/";
                var targetItemId;
                targetItem += GCStringUtils.escapeHTML(targetItems.eq(rowIdx).val());

                if (pathfieldObject != null && pathfieldObject.browseDialog != null && pathfieldObject.browseDialog.treePanel != null
                    && pathfieldObject.browseDialog.treePanel.selModel != null && pathfieldObject.browseDialog.treePanel.selModel.selNode != null) {
                    targetItemId = pathfieldObject.browseDialog.treePanel.selModel.selNode.attributes.id;
                }
                else {
                    targetItemId = "";
                }
                var newItemName = GCStringUtils.escapeHTML(newItemNames.eq(rowIdx).val());

                exportData.items.push({
                    "path": path,
                    "title": title,
                    "mappingName": mappingName,
                    "templateName": templateName,
                    "mappingPath": mappingPath,
                    "targetItem": targetItem,
                    "targetItemId": targetItemId,
                    "newItemName": newItemName
                });
            });
        }
        table.page.len(pageLength).draw();
        if (validationResult !== null) {
            table.page(validationResult.problemPage).draw('page');
            $('body').scrollTop(validationResult.screenPos);
        } else {
            table.page(pageNumber).draw('page');
        }
        return exportData;

    };
    $.fn.fetchItemsToExport = function () {
        var exportData = {pages: []};
        var checked = [];
        var table = $('#export-table').DataTable();
        var tableData = table.rows({selected: true, filter: 'applied'}).data();
        var rows = table.rows({selected: true, filter: 'applied'});
        tableData.each(function (value, index) {
            var path = $().cleanStringFromHierarchy(value.path);
            var title = $().cleanStringFromHierarchy(value.title);
            var root = $(table.row(index).node()).attr('root');
            var type = value.type;
            exportData.pages.push({
                "path": path,
                "title": title,
                "root": root,
                "type": type
            });
        });

        var changeStatusSelect = $('#change-status-select').find('option:selected');
        var newStatusId = $(changeStatusSelect).val();
        var newStatusName = $(changeStatusSelect).text();
        var newStatusColor = $(changeStatusSelect).data('color');
        if (newStatusId && newStatusName) {
            exportData.newStatusId = newStatusId;
            exportData.newStatusName = newStatusName;
            exportData.newStatusColor = newStatusColor;
        }
        exportData.projectName = $("#gcproject-select").find("option:selected").text().trim();
        return exportData;
    };

    $.fn.loadProjects = function () {
        var path = $("#final-confirm-export").data("pagepath");

        $.ajax({
            url: path + ".gcprojects.mapped.export.json",
            type: "GET",
            cache: false,
            beforeSend: function () {
                $("#gcproject-select").prop('disabled', true);
            },
            success: function (data) {
                var json = JSON.parse(data);
                var optionsString = "<option value=''>Select Project</option>";
                if (json.gcprojects) {
                    json.gcprojects.forEach(function (item, index) {
                        optionsString += "<option value='" + item.value + "'>" + item.text + "</option>";
                    });
                }
                $("#gcproject-select").html(optionsString);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("error");
            },
            complete: function () {
                $("#gcproject-select").prop('disabled', false);
            }
        });

    };


    $.fn.updateExportTable = function () {
        var inputs = $("[data-info='root-path']");
        var data = [];
        for (var i = 0; i < inputs.length; i++) {
            data.push([inputs.eq(i).val()]);
        }
        var spinnerTarget = document.getElementById('export-table');
        var spinner = new Spinner().spin();
        $.ajax({
            url: window.location.href.substr(0, window.location.href.lastIndexOf('/')) + ".subpages.json",
            type: "post",
            dataType: "json",
            data: {
                data: data
            },
            cache: false,
            beforeSend: function () {
                $(spinnerTarget).addClass('grayout');
                spinnerTarget.appendChild(spinner.el);
            },
            success: function (data) {
                $().loadExportTable(data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("error");
            },
            complete: function () {
                spinner.stop();
                $(spinnerTarget).removeClass('grayout');
            }
        });
    };
}(jQuery));

$(function () {
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

    $("#gcproject-select").change(function () {
        var projectid = $(this).find("option:selected").val().trim();
        var path = $("#final-confirm-export").data("pagepath");

        $.ajax({
            url: path + ".gcstatuses.projectId-" + projectid + ".json",
            type: "GET",
            cache: false,
            beforeSend: function () {
                $("#change-status-select").prop('disabled', true);
            },
            success: function (data) {
                var json = JSON.parse(data);
                var optionsString = "<option value=''>- Don't change status -</option>";
                if (json.gcstatuses) {
                    json.gcstatuses.forEach(function (item, index) {
                        optionsString += "<option data-color='" + item.color + "' value='" + item.id + "'>" + item.name + "</option>";
                    });
                }
                $("#change-status-select").html(optionsString);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("error");
            },
            complete: function () {
                $("#change-status-select").prop('disabled', false);
            }
        });
    });

    $("#cancel-export").click(function () {
        $('#confirm-export').prop('disabled', false);
        $('#confirmation-table-wrapper').hide();
        $('#export-table-wrapper').show();
        GATHERCONTENT.detachLeavePageHandlers();
    });

    $('#confirmation-table').on("change", "select.mapping", (function (e) {
        var value = $(this).val();
        var choosenOption = $(this).children('[value="' + value + '"]');
        $(this).parents('td').next().text(decodeURIComponent(choosenOption.attr('data-templateName'))   );
    }));

    $("#export-items").click(function () {
        if (!($('#gcproject-select').val()) || (!$.fn.DataTable.isDataTable('#export-table')) ||
            $('#export-table').DataTable().rows({selected: true, filter: 'applied'}).count() < 1) {
            $("#empty-list-modal").dialog("open");
            return false;
        }

        var importData = $().fetchItemsToExport();

        var options = {
            bSort: false,
            columns: [
                {
                    title: "AEM Title",
                    mRender: $().mRenderAEMTitle,
                    // mRender: function (data, type, full) {
                    //     return '<b>' + full.title + '</b></br>' + full.path;
                    // },
                    defaultContent: '',
                    width: "20%"
                },
                {
                    title: "<div class='spec-mapping-title'>Specify Mapping<input id='addMappingButtonId' type='button' style='margin-left: 5px' value='+'></div>",
                    data: "",
                    mRender: function (data, type, full) {
                        if (full.type === 'page') {
                            return "<select class='mapping full-width' data-path=" + GCStringUtils.escapeHTML($().cleanStringFromHierarchy(full.path)) + "></select>" +
                                "<br><span id='text-information-" + GCStringUtils.escapeHTML($().cleanStringFromHierarchy(full.path)) + "' class='span-warning'></span>";
                        } else {
                            return "";
                        }
                    },
                    defaultContent: '',
                    width: "10%"

                },
                {
                    title: "Template Name",
                    data: '',
                    defaultContent: '',
                    mRender: function (data, type, full) {
                        if (full.type === 'page') {
                            return "";
                        } else {
                            return "Custom Template";
                        }
                    },
                    width: "10%"

                },
                {
                    title: "Target Root Path",
                    defaultContent: '',
                    mRender: function (data, type, full) {
                        return "<div id='CQ' class='targetItemCQ' ><div class='targetItem' id='" + GCStringUtils.escapeHTML($().cleanStringFromHierarchy(full.path)) + "'></div></div>";
                    },
                    width: "30%"

                },
                {
                    title: "Target Item Name",
                    mRender: function (data, type, full) {
                        var str = $().cleanStringFromHierarchy(full.title);
                        return "<input class='targetNewItem full-width table-text-input' data-oldVal='" + str + "' id='new-item-name-" + GCStringUtils.escapeHTML($().cleanStringFromHierarchy(full.path)) + "'" + " value='" + str + "'>" +
                            "<br><span class='span-name' id='span-item-name-" + GCStringUtils.escapeHTML($().cleanStringFromHierarchy(full.path)) + "'></span>";
                    },
                    defaultContent: '',
                    width: "30%"

                }
            ],
            searching: false,
            "initComplete": function (settings, json) {

            }
        };

        var table = GATHERCONTENT.createAndFillDataTable($('#confirmation-table'), options, importData.pages);

        if (importData.newStatusId && importData.newStatusName) {
            $('#status-change-header').html('Status in GC will be changed to <span class="coloredspan" style="background-color:' + importData.newStatusColor + '"></span> ' + importData.newStatusName);
            $('#status-change-header').show();
        } else {
            $('#status-change-header').hide();
        }

        $('#export-table-wrapper').hide();
        $('#confirmation-table-wrapper').show();
        $().hierarchyView($('#confirmation-table').DataTable());
        $("select.mapping").attr('clicked', false);
        table.page.len(-1).draw();
        var targetItems = $('.targetItem');
        targetItems.each(function (index, element) {
            var isRoot = $(table.row(index).node()).attr('root');
            if (isRoot) {
                CQ.Ext.onReady(function () {
                    var form = new CQ.Ext.form.FormPanel({
                        id: "panel_" + $(element).attr('id'),
                        renderTo: document.getElementById($(element).attr('id')),
                        border: false,
                        hideLabels: true,
                        // width: "90%",
                        autoWidth: true,
                        items: [{

                            id: "import-path-" + $(element).attr('id'),
                            xtype: "GCItemsPathField",
                            //width: "100%",
                            autoWidth: true,
                            "listeners": {
                                'change': function (field, newValue, oldValue) {
                                },
                                'beforeselect': function (combo, record, index) {
                                },
                                'dialogselect': function (pathField, path, anchor) {
                                    GATHERCONTENT.pathFieldEmptyDialogSelectCheck(pathField, path);
                                    $().clickOnTargetItemDialog(pathField.id, path);
                                }
                            }

                        }],
                        "listeners": {

                            "afterrender": function (cmp) {
                            }

                        }
                    });
                });
            }
            else {
                var path = $(element).attr('id');
                var parentPath = path.substr(0, path.lastIndexOf('/'));
                var parentTargetItem = $('[id="import-path-' + parentPath + '"').val();
                var parentNewItemName = $('[id="new-item-name-' + parentPath + '"').val();
                var tryCount = 0;
                while (parentTargetItem === undefined && tryCount < 100) {
                    parentPath = parentPath.substring(0, parentPath.lastIndexOf('/'));
                    parentTargetItem = $('[id="import-path-' + parentPath + '"').val();
                    parentNewItemName = $('[id="new-item-name-' + parentPath + '"').val();
                    tryCount++;
                }
                var childValue = (parentTargetItem === "") ? parentNewItemName : parentTargetItem + "/" + parentNewItemName;
                var el = '<input type="text" size="24" autocomplete="off" id="import-path-' + $(element).attr('id') + '" name="import-path-' + $(element).attr('id')
                    + '" class="importNotRootPathClassImportant x-form-text x-form-field x-form-focus child-import-path full-width" readonly ' +
                    'value = "' + childValue + '">';
                $(element).append(el);
            }

        });
        $('input[id^="import-path-"]').prop('readonly', true);
        var deepMouseenterFunc = function () {
            var jTdElement = $(this);
            var spinner = new Spinner({
                position: 'relative',
                class: 'mappingSpinner',
                top: '50%',
                left: '50%',
                radius: 5
            });
            var spinnerTarget = jTdElement;//.parent();
            var jSelectElement = jTdElement.children('.mapping');
            var oldSelectValue = jSelectElement.val();
            jSelectElement.addClass('hideElement');
            spinnerTarget.prepend(spinner.spin().el);
            var pagePath = $().cleanStringFromHierarchy(jSelectElement.attr('data-path'));
            var projectId = $("#gcproject-select").val();
            var url = $(location).attr('href');
            jTdElement.unbind("mouseenter");
            $.ajax({
                url: window.location.href.substr(0, window.location.href.lastIndexOf('/')) + ".pagemappings.json",
                type: "post",
                dataType: "json",
                data: {
                    pagePath: pagePath,
                    projectId: projectId
                },
                cache: true,

                success: function (data) {
                    var optionsStr = "<option selected disabled hidden>Choose here</option>";
                    for (var i = 0; i < data.length; i++) {
                        optionsStr +=
                            "<option data-mappingPath='" + data[i].mappingPath + "'"
                            + "data-templateId='" + data[i].templateId + "'"
                            + " data-templateName='" + GCStringUtils.fixedEncodeURIComponent(data[i].templateName) + "'"
                            + " value='" + GCStringUtils.fixedEncodeURIComponent(data[i].mappingName) + "'>"
                            + GCStringUtils.escapeHTML(data[i].mappingName) + "(" + data[i].matchedProps + ")"
                            + "</option>";

                    }
                    spinner.stop();
                    jTdElement.mouseenter(deepMouseenterFunc);
                    jSelectElement.removeClass('hideElement');
                    $("select.mapping[data-path='" + pagePath + "']").html(optionsStr);
                    jSelectElement.val(oldSelectValue);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log("error");
                }
            });
        };

        $("#confirmation-table").find("tbody td:nth-child(2)").each(function () {
            $(this).mouseenter(deepMouseenterFunc);
        });
        $().applyStyleForTable('confirmation-table');
        table.columns.adjust().draw();
        table.page.len(10).draw();
        GATHERCONTENT.attachLeavePageHandlers();
    });
    $("#final-back-export").click(function () {
        $("#final-confirmation-table-wrapper").hide();
        $("#confirmation-table-wrapper").show();
    });


    $('#confirmation-table').on('change', '[id^="new-item-name-"]', function () {
        var newVal = $(this).val();
        var path = $(this).attr('id');
        path = path.substr(path.lastIndexOf('-/') + 1);
        if (newVal === "") {
            $('span[id="span-item-name-' + path + '"]').html('This field is required.');
            $('#confirm-export').prop("disabled", true);
        } else {
            $('span[id="span-item-name-' + path + '"]').html('');
            var newItemNames = $('input[id^="new-item-name-"]');
            for (var index = 0; index < newItemNames.length; index++) {
                if (newItemNames.eq(index).val() === "") {
                    $('#confirm-export').prop("disabled", true);
                    return false;
                }
            }
            $('#confirm-export').prop("disabled", false);
        }
        var pageLength = $('select[name="confirmation-table_length"]').val();
        var currentTargetItemInput = $('input[id="import-path-' + path + '"]');
        var parentTargetItemInput = currentTargetItemInput.parents('tr').prevUntil('tr > input:not([readonly]');
        var parentVal = parentTargetItemInput.eq(parentTargetItemInput.length - 1).val();
        var currentTargetItemPath = currentTargetItemInput.val();
        var oldVal = $(this).data('oldval');
        $(this).data('oldval', newVal);
        var isRoot = $(this).parents('tr').attr('root');

        var table = $('#confirmation-table').DataTable();
        var pageNumber = table.page.info().page;
        table.page.len(-1).draw();
        var children = $('[id^="import-path-' + path + '/"]');
        children.each(function () {
            var val = $(this).val();

            var targetItemPathPart;
            if (parentVal && parentVal !== "") {
                targetItemPathPart = val.substr(0, parentVal.length + 1);
                val = val.substr(parentVal.length + 1);
            }

            var beforeChoosenPathPart = val.substr(0, currentTargetItemPath.length);
            if (( beforeChoosenPathPart.length > 0 && isRoot) || !isRoot) {
                beforeChoosenPathPart = beforeChoosenPathPart + '/';
                val = val.substr(currentTargetItemPath.length + 1);
            }
            else {
                val = val.substr(currentTargetItemPath.length);
            }
            val = val.replace(oldVal, newVal);
            targetItemPathPart = targetItemPathPart === undefined ? "" : targetItemPathPart;
            val = targetItemPathPart + beforeChoosenPathPart + val;
            $(this).val(val);
        });
        table.page.len(pageLength).draw();
        table.page(pageNumber).draw('page');
    });
    $('#confirmation-table').on('click', '#addMappingButtonId', function () {
        window.open(GCStringUtils.getAddExportMappingURL(), '_blank');
    });
    $('#confirm-export').click(function () {

        // var itemsToImport = $().fetchItemsToFinalExport();
        // var path = $("#confirm-export").data("path");
        // var url = path + ".gcimporter.gc.json";
        // $.ajax({
        //     url: url,
        //     data: 'data=' + JSON.stringify(itemsToImport),
        //     type: "POST",
        //     success: function (data) {
        //         $().getProgress(path + ".importstatus.json?jobId=" + data);
        //         $('#confirmation-table-wrapper').hide();
        //         $('#progressbar-wrapper').show();
        //     },
        //     error: function (jqXHR, textStatus, errorThrown) {
        //         console.log("error");
        //     }
        // });
        var importData = $().fetchItemsToFinalExport();
        if (!importData) {
            return false;
        }

        var options = {
            bSort: false,
            columns: [
                {
                    title: "AEM Title",
                    mRender: $().mRenderAEMTitle,
                    // mRender: function (data, type, full) {
                    //     return '<b>' + full.title + '</b></br>' + full.path;
                    // },
                    defaultContent: '',
                    "width": "30%"
                },
                {
                    title: "Specify Mapping",
                    data: "",
                    mRender: function (data, type, full) {
                        return full.mappingName;
                    },
                    defaultContent: '',
                    "width": "20%"

                },
                {
                    title: "Template Name",
                    data: 'templateName',
                    mRender: function (data, type, full) {
                        return GCStringUtils.escapeHTML(data);
                    },
                    defaultContent: '',
                    "width": "10%"

                },
                {
                    title: "Target Item",
                    defaultContent: '',
                    mRender: function (data, type, full) {
                        return full.targetItem;
                    },
                    "width": "20%"

                },
                {
                    title: "Target Item Name",
                    mRender: function (data, type, full) {
                        return full.newItemName;
                    },
                    defaultContent: '',
                    "width": "20%"

                }
            ],
            searching: false,
            "initComplete": function (settings, json) {

            }
        };

        var table = GATHERCONTENT.createAndFillDataTable($('#final-confirmation-table'), options, importData.items);
        $().hierarchyView(table);
        $().applyStyleForTable('final-confirmation-table');
        table.draw();

        $('#confirmation-table-wrapper').hide();
        $('#final-confirmation-table-wrapper').show();

    });

    $('#final-confirm-export').click(function () {
        var itemsToExport = $('#final-confirmation-table').DataTable().rows().data();
        var dataForExport = {items: [], projectName: "", projectId: ""};
        itemsToExport.each(function (element, index) {
            var path = $().cleanStringFromHierarchy(element.path);
            var aemTitle = $().cleanStringFromHierarchy(element.title);
            dataForExport.items.push({
                "mappingPath": element.mappingPath,
                "importPath": path,
                "title": element.newItemName,
                "template": element.templateName,
                "gcTargetItemName": element.targetItem,
                "gcTargetItemId": element.targetItemId,
                "aemTitle": aemTitle
            });
        });
        var changeStatusSelect = $('#change-status-select').find('option:selected');
        var newStatusId = $(changeStatusSelect).val();
        var newStatusName = $(changeStatusSelect).text();
        var newStatusColor = $(changeStatusSelect).data('color');
        if (newStatusId && newStatusName) {
            dataForExport.newStatusId = newStatusId;
            dataForExport.newStatusName = newStatusName;
            dataForExport.newStatusColor = newStatusColor;
        }
        dataForExport.projectName = $('#gcproject-select option:selected').text();
        dataForExport.projectId = $('#gcproject-select').val();
        var path = $("#final-confirm-export").data("pagepath");
        var url = path + ".gcimporter.gc.json";

        $('#final-confirmation-table-wrapper').hide();
        GATHERCONTENT.detachLeavePageHandlers();
        GATHERCONTENT.sendItemsToProcess(url, path, dataForExport);
    });
});
CQ.Ext.onReady(function () {
    console.log('ext ready');
    new CQ.Ext.form.FormPanel({
        id: "export-form-panel",
        renderTo: document.getElementById("CQ-EXPORT-FORM-WRAPPER"),
        border: false,
        width: 440,
        orderable: false,
        name: 'multifieldExample',
        items: [
            {
                xtype: "multifield",
                bodyStyle: 'background-color : white;',
                autoWidth: true,
                buttonAlign: "left",
                hideLabel: true,
                fieldConfig: {
                    cls: "required path",
                    name: "export-page-path",
                    info: "root-paths",
                    xtype: "pathfield",

                    "listeners": {

                        "afterrender": function (cmp) {
                            cmp.getEl().set({
                                "data-info": 'root-path'
                            })
                        },
                        "dialogselect": function (pathField, path) {
                            GATHERCONTENT.pathFieldEmptyDialogSelectCheck(pathField, path);
                            $().updateExportTable();
                        },
                        "change": function () {
                            $().updateExportTable();
                        }
                    }
                }
            }]
    });
    $().loadProjects();
    $().loadExportTable([]);
});

