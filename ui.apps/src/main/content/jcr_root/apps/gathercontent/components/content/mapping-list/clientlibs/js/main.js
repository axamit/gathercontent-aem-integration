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

    $("#add-mapping").click(function () {
        var path = $("#add-mapping").data("path");
        path = path.substring(0, path.lastIndexOf("/jcr:content")) + ".mapping.mapping-mapping_" + Date.now() + ".html";
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
});