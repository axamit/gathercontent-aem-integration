$(function () {
    $("#add-configuration").click(function () {
        var path = $("#add-configuration").data("path");
        path = path.substring(0, path.lastIndexOf("/jcr:content")) + ".config.config-config_" + Date.now() + ".html";
        location.href = path;
    });

    $(".delete-config").click(function () {
        var path = $(this).data("path");
        $("#delete-confirm-modal").data("path", path);
        $("#delete-confirm-modal").dialog("open");
    });
});

$('document').ready(function () {
    $('#configuration-list-table').DataTable({
        order: [[1, 'desc']],
        columns: [
            {orderable: false},
            null
        ]
    });
});