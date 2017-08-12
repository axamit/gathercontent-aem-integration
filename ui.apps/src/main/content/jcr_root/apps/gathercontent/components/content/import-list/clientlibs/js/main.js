$('document').ready(function () {
    var table = $('#import-history-list-table');
    var path = table.data("path") + ".importhistory.json";
    var dataTable = table.DataTable({
        serverSide: true,
        ajax: {
            dataType: 'json',
            url: path,
            data: function (d) {
                return "data=" + JSON.stringify(d);
            },
            type: 'POST'
        },
        "columns": [
            {data: "importId"},
            {data: "jobType"},
            {data: "projectName"},
            {data: "importStartDate"},
            {data: "importEndDate"},
            {
                data: "status"
            },
            {
                data: "historyPath",
                render: function (data, type, row) {
                    return (data !== null) ? '<a href="' + data + '" target="_blank">Open</a>' : '';
                },
                orderable: false
            }
        ],
        "order": []
    });
    $("#refresh-history-table").click(function () {
        dataTable.ajax.reload(null, false);
    });
});