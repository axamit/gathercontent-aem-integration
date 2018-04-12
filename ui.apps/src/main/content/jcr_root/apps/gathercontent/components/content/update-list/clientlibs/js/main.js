$(function () {
    function fetchItemsToUpdate() {
        var updateData = {};
        var checked = [];
        $('#update-list-table').DataTable().rows({selected: true}).nodes().each(function (item) {
            var selected = {};
            selected.itemId = $(item).attr("itemId");
            selected.parentId = $(item).attr("parentId");
            selected.mappingName = $(item).find('.mapping-name').text().trim();
            selected.mappingPath = $(item).attr("mappingPath");
            selected.importPath = $(item).attr("importPath");
            selected.title = $(item).find('.item-title').text();
            selected.status = $(item).find('.item-status').text().trim();
            selected.template = $(item).attr("itemTemplate");
            selected.color = $(item).find(".coloredspan").css("background-color");
            checked.push(selected);
        });
        updateData.items = checked;
        var changeStatusSelect = $('#change-status-select').find('option:selected');
        var newStatusId = $(changeStatusSelect).val();
        var newStatusName = $(changeStatusSelect).text();
        var newStatusColor = $(changeStatusSelect).data('color');
        if (newStatusId && newStatusName) {
            updateData.newStatusId = newStatusId;
            updateData.newStatusName = newStatusName;
            updateData.newStatusColor = newStatusColor;
        }
        updateData.projectName = $("#import-project-select").find("option:selected").text().trim();
        return updateData;
    }

    function getImportSide() {
        var e = document.getElementById("update-filter");
        var importSide = e !== null ? e.options[e.selectedIndex].value : null;
        if (importSide === "aem") {
            return "aem";
        } else {
            return "gc";
        }
    }

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

    $("#confirm-update").click(function () {
        var itemsToImport = fetchItemsToUpdate();
        var pagePath = $("#confirm-update").data("pagepath");

        var url;
        if (getImportSide() === "aem") {
            url = pagePath + ".gcimporter.update.json";
        } else {
            url = pagePath + ".gcimporter.update.gc.json";
        }

        GATHERCONTENT.sendItemsToProcess(url, pagePath, itemsToImport);
    });

    $("#update-project-select, #update-filter").change(function () {
        var projectid = $("#update-project-select").find("option:selected").val();
        var path = $("#confirm-update").data("path");
        var spinnerTarget = document.getElementById('update-table');
        var spinner = new Spinner().spin();
        var side = "import";
        if (getImportSide() === "gc") {
            side = "export";
        }
        $.ajax({
            url: path + ".ajax.projectId-" + projectid + "." + side + ".html",
            type: "GET",
            cache: false,
            beforeSend: function () {
                $(spinnerTarget).addClass('grayout');
                spinnerTarget.appendChild(spinner.el);
            },
            success: function (data) {
                $("#update-table").html(data);
                var table = GATHERCONTENT.initImportUpdateTable('update-list-table');
                GATHERCONTENT.fillFilter(table, '#status-filter', '.item-status', 'Select Status');
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

    $("#update-project-select").change(function () {
        var projectId = $(this).find("option:selected").val().trim();
        var path = $("#confirm-update").data("path");

        $.ajax({
            url: path + ".gcstatuses.projectId-" + projectId + ".json",
            type: "GET",
            cache: false,
            dataType: 'json',
            beforeSend: function () {
                $("#change-status-select").prop('disabled', true);
            },
            success: function (json) {
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

    $("#update-items").click(function () {
        if ($('#update-list-table').DataTable().rows({selected: true}).count() < 1) {
            $("#empty-list-modal").dialog("open");
            return false;
        }
        var importData = fetchItemsToUpdate();

        var options = {
            columns: [
                GATHERCONTENT.dataTablesColumns.status,
                {
                    title: "Item Name", data: 'title', name: "item-name",
                    render: function (data, type, row) {
                        return (data != null ? '<span class="item-title">' + GCStringUtils.escapeHTML(data) + '</span>' : '');
                    }
                },
                GATHERCONTENT.dataTablesColumns.mappingName,
                GATHERCONTENT.dataTablesColumns.templateName
            ],
            searching: false
        };

        GATHERCONTENT.createAndFillDataTable($('#confirmation-table'), options, importData.items).draw();

        if (importData.newStatusId && importData.newStatusName) {
            $('#status-change-header').html('Status in GC will be changed to <span class="coloredspan" style="background-color:' + importData.newStatusColor + '"></span> ' + importData.newStatusName);
            $('#status-change-header').show();
        } else {
            $('#status-change-header').hide();
        }
        $('#update-table-wrapper').hide();
        $('#confirmation-table-wrapper').show();
        $('#confirmation-table-wrapper').show();
    });

    $("#cancel-update").click(function () {
        $('#confirmation-table-wrapper').hide();
        $('#update-table-wrapper').show();
    });
});

$('document').ready(function () {
    var table = GATHERCONTENT.initImportUpdateTable('update-list-table');
    GATHERCONTENT.fillFilter(table, '#status-filter', '.item-status', 'Select Status');
    $('#title-filter').on('keyup', (function () {
        $('#update-list-table').DataTable().column(2).search((this.value), false, false, false).rows().deselect().draw();
        $('#all-items-checkbox').prop('checked', false);
    }));
    $('#status-filter').change(function () {
        $('#update-list-table').DataTable().column(1).search((this.value), false, false, false).rows().deselect().draw();
        $('#all-items-checkbox').prop('checked', false);
    });
});
