/*
 * Axamit, gc.support@axamit.com
 */

var checkedItems = [];
(function ($) {
    $.fn.initImportTable = function () {
        $().setImportPath();
        $().setOnChangeListeners();
        var table = $('#item-list-table').DataTable({
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
        $('#item-list-table_filter').remove();
        $().fillFilter('#status-filter', '.item-status', 'Select Status');
        $().fillFilter('#template-filter', '.item-template', 'Select Template');
    };

    $.fn.fetchItemsToImport = function () {
        var importData = {};
        var checked = [];
        var emptyField = 0;
        $('#item-list-table').DataTable().rows({selected: true, filter: 'applied'}).nodes().each(function (item) {
            var selected = {};
            selected.itemId = $(item).attr("itemId");
            selected.parentId = $(item).attr("parentId");
            selected.mappingPath = $(item).find('.item-mapping-select').find('option:selected').val();
            if (!selected.mappingPath) {
                selected.mappingPath = $(item).attr("mappingPath");
            } else {
                selected.mappingPath = selected.mappingPath.trim();
            }
            selected.importPath = $(item).find('input.item-import-path').val();
            if (!selected.importPath) {
                selected.importPath = $(item).attr("importPath");
            } else {
                selected.importPath = selected.importPath.trim();
            }
            selected.title = $(item).find('.item-title').text().trim();
            selected.status = $(item).find('.item-status').text().trim();
            selected.template = $(item).find('.item-template').text().trim();
            selected.color = $(item).find(".coloredspan").css("background-color");
            if (!selected.importPath && selected.parentId == 0) {
                emptyField++;
            }
            if (!selected.importPath && selected.parentId != 0) {
                if (checked.filter(function (value) {
                        return value.itemId == selected.parentId;
                    }).length == 0) {
                    emptyField++;
                }
            }
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
        importData.emptyItems = emptyField;
        return importData;
    };

    $.fn.setImportPath = function () {
        $('.item-mapping-select').find('option:selected').each(function () {
            var importPath = $(this).attr('importPath');
            $(this).closest("tr").find('.item-import-path').val(importPath);
        });
    };

    $.fn.setOnChangeListeners = function () {
        $(".item-mapping-select").change(function () {
            $().setImportPath();
        });
    };

    $.fn.fillFilter = function (filterInputId, optionClass, defaultValue) {
        $(filterInputId).html(function () {
            var optionsString = "<option value=''>" + defaultValue + "</option>";
            var values = [];
            $('#item-list-table').DataTable().rows().nodes().each(function (item) {
                values.push($(item).find(optionClass).text().trim());
            });
            values = values.filter(function (el, index, arr) {
                return index === arr.indexOf(el);
            });
            values.forEach(function (item) {
                optionsString += "<option value='" + item + "'>" + item + "</option>";
            });
            return optionsString;
        });
    };
}(jQuery));

$(function () {
    var progressbar = $("#progressbar"),
        progressLabel = $("#progressbar-label");

    var isUpdate = document.URL.indexOf("update") != -1;
    var processStatus = "imported";
    var columnTitle = "Import Status";
    var columnValue = "Imported";
    if (isUpdate) {
        processStatus = "updated";
        columnTitle = "Update Status";
        columnValue = "Updated";
    }

    progressbar.progressbar({
        value: false,
        change: function () {
            progressLabel.text(progressbar.progressbar("option", "value") + "/" + progressbar.progressbar("option", "max") + " nodes " + processStatus);
        }
    });

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

    $("#empty-field-modal").dialog({
        autoOpen: false,
        modal: true,
        resizable: false,
        width: 400,
        buttons: [
            {
                text: "Ok",
                click: function () {
                    $(this).dialog("close");
                    $("#import-items").data("empty", true);
                    $("#import-items").click();
                }
            }, {
                text: "Cancel",
                click: function () {
                    $(this).dialog("close");
                }
            }
        ]
    });

    function showResults(data) {
        var json = JSON.parse(data);
        var importedNumber = json.importedNumber;

        if (importedNumber == 0) {
            $('#imported-cnt-wrapper').hide();
        } else {
            var importedNumberText = (importedNumber == 1) ? importedNumber + " node " + processStatus : importedNumber + " nodes " + processStatus;
            $('#imported-cnt')[0].innerHTML = importedNumberText;
        }

        var failedNumber = json.failedNumber;

        if (failedNumber == 0) {
            $('#failed-cnt-wrapper').hide();
        } else {
            var href_text = '<a href="/system/console/status-slinglogs" target="_blank">See log</a>' + " for details";
            var failedNumberText = (failedNumber == 1) ? failedNumber + " node failed. " + href_text : failedNumber + " nodes failed. " + href_text;
            $('#failed-cnt')[0].innerHTML = failedNumberText;
        }

        $('#result-table').DataTable({
            data: json.importedPages,
            columns: [
                {
                    title: "Status", data: 'status',
                    render: function (data, type, row) {
                        return '<span class="coloredspan" style="display: inline-block;width: 15px;height: 15px;position: relative;border-radius: 50px; background-color:' + row.color + '"></span> ' + data;
                    }
                },
                {title: "Item Name", data: 'name'},
                {
                    title: columnTitle, data: 'importStatus',
                    render: function (data, type, row) {
                        return (data != 'Imported') ? data + '. <a href="/system/console/status-slinglogs" target="_blank">See log</a>' + '.' : columnValue;
                    }
                },
                {title: "Template Name", data: 'gcTemplateName'},
                {
                    title: "Link in AEM", data: 'aemLink',
                    render: function (data, type, row) {
                        return (data != null) ? '<a href="' + data + '.html" target="_blank">Open</a>' : '';
                    }
                },
                {
                    title: "Link in GC", data: 'gcLink',
                    render: function (data, type, row) {
                        return (data != null) ? '<a href="' + data + '" target="_blank">Open</a>' : '';
                    }
                }
            ]
        });
    }

    function getProgress(url) {
        $.ajax({
            url: url,
            cache: false,
            success: function (data) {
                try {
                    var json = JSON.parse(data);
                    if (json.hasOwnProperty("importData")) {
                        var importData = JSON.parse(json["importData"]);
                        progressbar.progressbar("option", "max", importData.totalPagesCount);
                        progressbar.progressbar("option", "value", importData.importedPagesCount);
                        if (importData.importedPagesCount == importData.totalPagesCount) {
                            showResults(importData.importedPagesData);
                            $('#progressbar-wrapper').hide();
                            $('#result-table-wrapper').show();
                        } else {
                            setTimeout(function () {
                                getProgress(url)
                            }, 2000);
                        }
                    } else if (json.hasOwnProperty("statusString")) {
                        progressLabel.text(json["statusString"]);
                        setTimeout(function () {
                            getProgress(url)
                        }, 2000);
                    } else {
                        var errorString = "An unexpected error has occurred. Your request cannot be processed at this time.";
                        if (json.hasOwnProperty("errorString")) {
                            errorString = json["errorString"];
                        }
                        showImportFailedNotification(errorString);
                    }
                } catch (e) {
                    showImportFailedNotification("An unexpected error has occurred. Your request cannot be processed at this time.");
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showImportFailedNotification("An unexpected error has occurred. Your request cannot be processed at this time.");
            }
        });
    }

    $("#confirm-import").click(function () {
        var itemsToImport = $().fetchItemsToImport();
        var path = $("#confirm-import").data("path");
        var isUpdate = $("#confirm-import").data("isupdate");
        var url;
        if (isUpdate) {
            url = path + ".gcimporter.update.json";
        } else {
            url = path + ".gcimporter.json";
        }

        $.ajax({
            url: url,
            data: 'data=' + btoa(JSON.stringify(itemsToImport)),
            type: "POST",
            success: function (data) {
                try {
                    var response = JSON.parse(data);
                    if (response.hasOwnProperty("jobId")) {
                        getProgress(path + ".importstatus.json?jobId=" + response["jobId"]);
                        $('#confirmation-table-wrapper').hide();
                        $('#progressbar-wrapper').show();
                    } else {
                        var errorString = "An unexpected error has occurred. Your request cannot be processed at this time.";
                        if (response.hasOwnProperty("errorString")) {
                            errorString = response["errorString"];
                        }
                        showImportFailedNotification(errorString);
                    }
                } catch (e) {
                    showImportFailedNotification("An unexpected error has occurred. Your request cannot be processed at this time.");
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showImportFailedNotification("An unexpected error has occurred. Your request cannot be processed at this time.");
            }
        });
    });

    function showImportFailedNotification(message) {
        $('#confirmation-table-wrapper').hide();
        $('#progressbar-wrapper').hide();
        $('#imported-cnt-wrapper').hide();
        $('#failed-cnt').html(message + ' <a href="/system/console/status-slinglogs" target="_blank">See log</a>');
        $('#result-table-wrapper').show();
    }

    $("#import-project-select").change(function () {
        var projectid = $(this).find("option:selected").val();
        var path = $("#confirm-import").data("path");
        var spinnerTarget = document.getElementById('import-table');
        var spinner = new Spinner().spin();
        $.ajax({
            url: path + ".ajax.projectId-" + projectid + ".html",
            type: "GET",
            cache: false,
            beforeSend: function () {
                $(spinnerTarget).addClass('grayout');
                spinnerTarget.appendChild(spinner.el);
            },
            success: function (data) {
                $("#import-table").html(data);
                $().initImportTable();
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

    $("#import-items").click(function () {
        if ($('#item-list-table').DataTable().rows({selected: true, filter: 'applied'}).count() < 1) {
            $("#empty-list-modal").dialog("open");
            return false;
        }
        var importData = $().fetchItemsToImport();

        if (!$("#import-items").data("empty")) {
            if (importData.emptyItems > 0) {
                $("#empty-field").html(" <b>" + importData.emptyItems + "</b> " +
                    "page(s) with empty Import Path are found. The pages will be imported to " +
                    "<b>&#8220;" + defaultImportPath + "&#8221;</b>" +
                    " by default.");
                $("#empty-field-modal").dialog("open");
                return false;
            }
        }
        if ($.fn.dataTable.isDataTable('#confirmation-table')) {
            $('#confirmation-table').DataTable().destroy();
        }
        $('#confirmation-table').DataTable({
            data: importData.items,
            columns: [
                {
                    title: "Status", data: 'status',
                    render: function (data, type, row) {
                        return '<span class="coloredspan" style="display: inline-block;width: 15px;height: 15px;position: relative;border-radius: 50px; background-color:' + row.color + '"></span> ' + data;
                    }
                },
                {title: "Item Name", data: 'title'},
                {title: "Template Name", data: 'template'}
            ],
            searching: false
        });
        if (importData.newStatusId && importData.newStatusName) {
            $('#status-change-header').html('Status in GC will be changed to <span class="coloredspan" style="display: inline-block;width: 15px;height: 15px;position: relative;border-radius: 50px; background-color:' + importData.newStatusColor + '"></span> ' + importData.newStatusName);
            $('#status-change-header').show();
        } else {
            $('#status-change-header').hide();
        }
        $('#import-table-wrapper').hide();
        $('#confirmation-table-wrapper').show();
    });

    $("#cancel-import").click(function () {
        $('#confirmation-table-wrapper').hide();
        $("#import-items").data("empty", false);
        $('#import-table-wrapper').show();
    });

    $("#import-table").on("click", ".select-checkbox, #all-items-checkbox", function () {

        var table = $('#item-list-table').DataTable();
        var selectedData = table.rows({selected: true, filter: 'applied'}).nodes();
        if ($(this).prop('id') == 'all-items-checkbox' || selectedData.length == 0) {
            table.rows().nodes().each(function (element) {
                $(element).find('input.item-import-path').removeClass('gc-import-hide-children-import-path');
            });
            return;
        }
        var itemContainer = $(this).parent('tr');

        if (!itemContainer.hasClass("selected")) {
            var findItemImport = itemContainer.find('input.item-import-path');
            findItemImport.removeClass('gc-import-hide-children-import-path');
        }

        var selectedItemIds = selectedData.toArray().map(function (element) {
            return $(element).attr("itemId");
        });

        selectedData.each(function (element) {
            var findLocalItem = $(element).find('input.item-import-path');
            var parentId = $(element).attr('parentId');
            if (selectedItemIds.indexOf(parentId) > -1) {
                findLocalItem.addClass('gc-import-hide-children-import-path');
            }
            else {
                findLocalItem.removeClass('gc-import-hide-children-import-path');
            }
        });

    });
});

