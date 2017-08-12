/*
 * Axamit, gc.support@axamit.com
 */

$(function () {
    $("#delete-confirm-modal").dialog({
        autoOpen: false,
        modal: true,
        resizable: false,
        width: 400,
        buttons: [
            {
                text: "Delete",
                click: function () {
                    var path = $(this).data("path");
                    var data = {};
                    data[":operation"] = "delete";
                    $.ajax({
                        url: path,
                        data: data,
                        type: "POST",
                        success: function (data) {
                            location.reload();
                        },
                        error: function (jqXHR, textStatus, errorThrown) {
                            console.log("error");
                        }
                    });
                }
            },
            {
                text: "Cancel",
                click: function () {
                    $(this).dialog("close");
                }
            }
        ]
    });

    var curl = $("#add-mapping").data("pagepath") + ".copy.html";
    var name = $('#newMappingName').val();
    var dialog = $("#dialog").dialog({autoOpen: false, resizable: false});
    var res = dialog.data('path');


    $(".copyLink").click(function () {
        var path = $(this).data('about');
        var prefilledName = $(this).data('mappingname')+"(copy)";
        dialog.data("path", path).dialog("open");
        hideCopyDialogErrors();
        $("#newMappingName").val(prefilledName);
    });

    var getDialogParameters = function () {
        var res = $(dialog).data('path');
        var name = $('#newMappingName').val();
        return {
            'path': res,
            'name': name
        };
    };

    var checkIfSameMappingNameExists = function (checkName) {
        checkName = checkName.trim();
        var table = $("#mapping-list-table").DataTable();
        var isDuplicate = false;
        $.each(table.rows().eq(0), function (index) {
                var row = table.row(index);
                var data = row.data();
                if (data[2].trim() === checkName) {
                    isDuplicate = true;
                    return false;
                }
                return true;
            }
        );
        return isDuplicate;
    };
    var hideCopyDialogErrors = function () {
        $('#empty-name-error').hide();
        $("#duplicate-name-error").hide();
    };
    var validateForm = function () {
        hideCopyDialogErrors();
        var newMappingName = $('#newMappingName').val();
        if ( GCStringUtils.isBlank(newMappingName)) {
            $('#empty-name-error').show();
            return false;
        }
        else{
            if ((checkIfSameMappingNameExists(newMappingName))){
                $("#duplicate-name-error").show();
                return false;
            }
        }
        return true;
    };
    $('#newMappingNameSubmit').click(function () {
        var dialogParameters = getDialogParameters();

        if (validateForm()) {
            $.ajax
            ({
                url: curl,
                data: {
                    resourcePath: dialogParameters['path'],
                    newName: dialogParameters['name']
                },
                type: 'POST',
                dataType: 'json',
                success: function (result) {
                    location.reload();
                }
            });
        }
    });

    $('#newMappingNameSubmitAndOpen').click(function () {
        var dialogParameters = getDialogParameters();

        if (validateForm()) {
            $.ajax
            ({
                url: curl,
                data: {
                    resourcePath: dialogParameters['path'],
                    newName: dialogParameters['name']
                },
                type: 'POST',
                dataType: 'json',
                success: function (result) {
                    window.location = result['editLink'];
                }
            });
        }
    });

    $("#add-mapping").click(function () {
        var path = $(this).data("path");
        var sideSelector = $(this).data("sideselector");
        path = path.substring(0, path.lastIndexOf("/jcr:content")) + "." + sideSelector + ".mapping-mapping_" + Date.now() + ".html";
        location.href = path;
    });

    $(".delete-mapping").click(function () {
        var path = $(this).data("path");
        $("#delete-confirm-modal").data("path", path);
        $("#delete-confirm-modal").dialog("open");
    });
});

$('document').ready(function () {
    var table = $('#mapping-list-table').DataTable();
    $('#mapping-list-table_filter').find('input[type="search"]').on('keyup', (function () {
        table.search(this.value, false, false, false).draw();
    }));
    var createdUpdatedMapping = localStorage.getItem("createdUpdatedMapping");
    if (createdUpdatedMapping) {
        localStorage.removeItem("createdUpdatedMapping");
        $('#mapping-updated-cnt').html(createdUpdatedMapping);
        $('#mapping-updated-cnt-wrapper').show();
        setTimeout(function () {
            $('#mapping-updated-cnt-wrapper').hide();
        }, 5000);
    }
});