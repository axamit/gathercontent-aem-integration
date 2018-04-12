/*
 * Axamit, gc.support@axamit.com
 */

$(function () {
    $("#testConnection").click(function () {
        var path = $("#testConnection").data("path");
        $.ajax({
            url: path + ".gcme.json",
            type: "GET",
            cache: false,
            dataType: 'json',
            success: function (response) {
                if (response.success) {
                    $('#testConnectionOutput').addClass("success-connection").removeClass("error-connection");
                    $('#testConnectionOutput').html("Connection successful");
                } else {
                    $('#testConnectionOutput').addClass("error-connection").removeClass("success-connection");
                    $('#testConnectionOutput').html("Invalid credentials");
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $('#testConnectionOutput').addClass("error-connection").removeClass("success-connection");
                $('#testConnectionOutput').html("Connection failed");
            }
        });
    });
});