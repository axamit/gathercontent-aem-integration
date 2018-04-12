/**
 * Created by pavel.kulikou on 30-Nov-16.
 */
var GATHERCONTENT = GATHERCONTENT || {};
(function (context, $) {
    context.checkIfPreviousURLis = function (checkURL) {
        return document.referrer === checkURL;
    };
    context.getGCItemsJSONTreeServletURL = function (projectId) {
        return window.location.pathname.substring(0, window.location.pathname.indexOf('.')) + ".tree" + "." + projectId + ".json";
    };
    context.fillFilter = function (table, filterInputId, optionClass, defaultValue) {
        $(filterInputId).html(function () {
            var optionsString = "<option value=''>" + defaultValue + "</option>";
            var values = [];
            table.rows().nodes().each(function (item) {
                values.push($(item).find(optionClass).text().trim());
            });
            values = values.filter(function (el, index, arr) {
                return index === arr.indexOf(el);
            });
            values.forEach(function (item) {
                optionsString += "<option value='" + GCStringUtils.escapeHTML(item) + "'>" + item + "</option>";
            });
            return optionsString;
        });
    };

    context.initImportUpdateTable = function (tableId) {
        var table = $('#' + tableId).DataTable({
            destroy: true,
            columnDefs: [{
                orderable: false,
                className: 'select-checkbox',
                targets: 0
            }],
            select: {
                style: 'multi',
                selector: 'td:first-child'
            }
        });
        $('#all-items-checkbox').click(function () {
            if ($('#all-items-checkbox').prop('checked')) {
                table.rows({filter: 'applied'}).select();
            } else {
                table.rows().deselect();
            }
        });
        $('#' + tableId + '_filter').remove();
        return table;
    };

    context.createAndFillDataTable = function (jQueryTableElement, options, items) {
        var table;
        if ($.fn.dataTable.isDataTable(jQueryTableElement)) {
            table = $(jQueryTableElement).DataTable();
            table.clear();
        } else {
            table = $(jQueryTableElement).DataTable(options);
        }
        table.rows.add(items);
        return table;
    };

    context.showFailNotification = function (message) {
        $('#global-error').html(message + ' <a href="/system/console/status-slinglogs" target="_blank">See log</a>');
        $('#global-error-wrapper').show();
    };

    context.unexperctedErrorMessage = 'An unexpected error has occurred. Your request cannot be processed at this time.';

    context.sendItemsToProcess = function (url, pagePath, itemsToImport) {
        $.ajax({
            url: url,
            data: 'data=' + GCStringUtils.utf8_to_b64(JSON.stringify(itemsToImport)),            //old way : btoa(JSON.stringify(itemsToImport)),
            type: "POST",
            dataType: 'json',
            success: function (response) {
                try {
                    if (response.hasOwnProperty("jobId")) {
                        window.location.href = pagePath + ".jobs.html/" + response["jobId"];
                    } else {
                        var errorString = context.unexperctedErrorMessage;
                        if (response.hasOwnProperty("errorString")) {
                            errorString = response["errorString"];
                        }
                        context.showFailNotification(errorString);
                    }
                } catch (e) {
                    context.showFailNotification(context.unexperctedErrorMessage);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                context.showFailNotification(context.unexperctedErrorMessage);
            }
        });
    };

    context.pathFieldEmptyDialogSelectCheck = function (pathField, path) {
        if (GCStringUtils.isEmpty(path)) {
            pathField.setValue("");
        }
    };

    context.dataTablesColumns = {};

    context.dataTablesColumns.status = {
        title: "Status", data: 'status', name: "status",
        render: function (data, type, row) {
            return (data != null && row.color !== null
                ? '<span class="coloredspan" style="background-color:' + row.color + '"></span> <span class="item-status">' + GCStringUtils.escapeHTML(data) + '</span>' : '');
        }
    };

    context.dataTablesColumns.mappingName = {
        title: "Mapping Name", data: 'mappingName', name: "mapping-name",
        render: function (data, type, row) {
            return (data != null ? '<span class="mapping-name">' + GCStringUtils.escapeHTML(data) + '</span>' : '');
        }
    };

    context.dataTablesColumns.templateName = {
        title: "Template Name", data: 'template', name: "template-name",
        render: function (data, type, row) {
            return (data != null ? '<span class="item-template">' + GCStringUtils.escapeHTML(data) + '</span>' : '');
        }
    };

    context.dataTablesColumns.hierarchyTitle = {
        title: "Item Name", data: 'hierarchyTitle', name: "item-name",
        render: function (data, type, row) {
            return (data != null ? '<span class="item-title">' + GCStringUtils.escapeHTML(data) + '</span>' : '');
        }
    };

    context.decodeJQueryElementsValues = function (elementsFromSelector) {
        $.each(elementsFromSelector, function (idx, obj) {
           var escapedValue = $(obj).val();
            $(obj).val(decodeURIComponent(escapedValue));
        });
    };

    var leavePageHandler = function (event) {
        var leavePageModal = $("#leave-page-modal");
        if (leavePageModal.length > 0) {
            event.preventDefault();
            leavePageModal.data('link', this.href).dialog("open");
        }
    };

    context.attachLeavePageHandlers = function () {
        $(".main-menu").on("click", "a", leavePageHandler);
        $(window).on('beforeunload', function () {
            return 'Any unsaved changes will be lost.';
        });
    };

    context.detachLeavePageHandlers = function () {
        $(".main-menu").off("click", "a", leavePageHandler);
        $(window).off('beforeunload');
    };

    $('document').ready(function () {
        $("#leave-page-modal").dialog({
            autoOpen: false,
            modal: true,
            buttons: [{
                text: "Leave",
                click: function () {
                    var path = $(this).data('link');
                    $(window).off('beforeunload');
                    $(location).attr('href', path);
                }
            }, {
                text: "Stay",
                click: function () {
                    $(this).dialog("close");
                }
            }]
        });
    });
})(GATHERCONTENT, jQuery);
