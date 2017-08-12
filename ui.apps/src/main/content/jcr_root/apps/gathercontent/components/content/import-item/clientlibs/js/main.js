/*
 * Axamit, gc.support@axamit.com
 */

$(function () {
    function showImportFailedNotification(errorString) {
        $('#progressbar-wrapper').hide();
        GATHERCONTENT.showFailNotification(errorString);
    }

    function getProgress(url, progressbar, progressLabel) {
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
                        if (importData.importedPagesCount === importData.totalPagesCount) {
                            showResults(importData);
                            $('#progressbar-wrapper').hide();
                            $('#result-table-wrapper').show();
                        } else {
                            setTimeout(function () {
                                getProgress(url, progressbar, progressLabel);
                            }, 2000);
                        }
                    } else if (json.hasOwnProperty("statusString")) {
                        progressLabel.text(json["statusString"]);
                        setTimeout(function () {
                            getProgress(url, progressbar, progressLabel);
                        }, 2000);
                    } else {
                        var errorString = GATHERCONTENT.unexperctedErrorMessage;
                        if (json.hasOwnProperty("errorString")) {
                            errorString = json["errorString"];
                        }
                        showImportFailedNotification(errorString);
                    }
                } catch (e) {
                    showImportFailedNotification(GATHERCONTENT.unexperctedErrorMessage);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showImportFailedNotification(GATHERCONTENT.unexperctedErrorMessage);
            }
        });
    }

    function showResults(importData) {
        var pagesDataJson = JSON.parse(importData.importedPagesData);
        var importedNumber = pagesDataJson.importedNumber;
        var jobType = "Import";
        var processStatus = "imported";
        var columnTitle = "Import Status";
        var columnValue = "Imported";

        if (importData.hasOwnProperty("jobType")) {
            jobType = importData["jobType"];
            if (jobType === "Export") {
                processStatus = "exported";
                columnTitle = "Export Status";
                columnValue = "Exported";
            } else if (jobType === "Import Update" || jobType === "Export Update") {
                processStatus = "updated";
                columnTitle = "Update Status";
                columnValue = "Updated";
            }
        }

        var header = $('#result-table-header');
        $(header).html($(header).html() + " for " + jobType);

        var columns = [];

        var nameColumn = {
            title: "Item Name", data: 'name',
            render: function (data, type, row) {
                return (data != null ? GCStringUtils.escapeHTML(data) : '');
            }
        };

        var aemTitleColumn = {
            title: "AEM Title", data: 'aemTitle',
            render: function (data, type, row) {
                if (data != null && row.aemLink != null) {
                    return '<b>' + GCStringUtils.escapeHTML(data) + '</b><div>' + GCStringUtils.getAllSpacesBeforeLetter(data) + GCStringUtils.escapeHTML(row.aemLink) + '</div>'
                }
                else {
                    return '';
                }
            }
        };

        var importStatusColumn = {
            title: columnTitle, data: 'importStatus',
            render: function (data, type, row) {
                return (data !== 'Imported') ? 'Not ' + columnValue + '. <a href="/system/console/status-slinglogs" target="_blank">See log</a>' + '.' : columnValue;
            }
        };

        var gcTemplateNameColumn = {
            title: "Template Name", data: 'gcTemplateName',
            render: function (data, type, row) {
                return (data != null ? GCStringUtils.escapeHTML(data) : '');
            }
        };
        var aemLinkColumn = {
            title: "Link in AEM", data: 'aemLink',
            mRender: function (data, type, full) {
                return (data != null && full.type !== "folder") ? '<a href="' + data + '.html" target="_blank">Open</a>' : '';
            }
        };

        var gcLinkColumn = {
            title: "Link in GC", data: 'gcLink',
            render: function (data, type, row) {
                return (data != null) ? '<a href="' + data + '" target="_blank">Open</a>' : '';
            }
        };

        if (jobType === "Export") {
            columns.push(nameColumn);
            columns.push(GATHERCONTENT.dataTablesColumns.mappingName);
            columns.push(importStatusColumn);
            columns.push(gcTemplateNameColumn);
            columns.push(aemLinkColumn);
            columns.push(gcLinkColumn);
        } else {
            columns.push(GATHERCONTENT.dataTablesColumns.status);
            columns.push(nameColumn);
            columns.push(GATHERCONTENT.dataTablesColumns.mappingName);
            columns.push(importStatusColumn);
            columns.push(gcTemplateNameColumn);
            columns.push(aemLinkColumn);
            columns.push(gcLinkColumn);
        }

        if (importedNumber === 0) {
            $('#imported-cnt-wrapper').hide();
        } else {
            $('#imported-cnt')[0].innerHTML = (importedNumber === 1) ? importedNumber + " node " + processStatus : importedNumber + " nodes " + processStatus;
        }

        var failedNumber = pagesDataJson.failedNumber;

        if (failedNumber === 0) {
            $('#failed-cnt-wrapper').hide();
        } else {
            var href_text = '<a href="/system/console/status-slinglogs" target="_blank">See log</a>' + " for details";
            $('#failed-cnt')[0].innerHTML = (failedNumber === 1) ? failedNumber + " node failed. " + href_text : failedNumber + " nodes failed. " + href_text;
        }

        $('#result-table').DataTable({
            data: pagesDataJson.importedPages,
            columns: columns,
            "order": []
        });
    }

    $('document').ready(function () {
        var progressbar = $("#progressbar"),
            progressLabel = $("#progressbar-label");

        progressbar.progressbar({
            value: false,
            change: function () {
                progressLabel.text(progressbar.progressbar("option", "value") + "/" + progressbar.progressbar("option", "max") + " nodes proceeded");
            }
        });
        getProgress($('#import-item-result').data('statuspath'), progressbar, progressLabel);
    })
});
