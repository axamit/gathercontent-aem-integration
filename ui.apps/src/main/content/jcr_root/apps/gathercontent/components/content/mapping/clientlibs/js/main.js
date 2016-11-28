/*
 * Axamit, gc.support@axamit.com
 */

(function ($) {
    $.fn.fetchMappingData = function () {
        var data = {
        };
        var mapping = {};
        $(".mapper-element").each(function (index) {
            var id = $(this).data("id");
            var selected = $(this).find('.mapper-select').val().trim();
            if (selected == "Don't map") {
                selected = "";
            }
            mapping[id] = selected;
        });
        var metaMapperValue = $('#meta-name-select').val();
        if (metaMapperValue == "Don't map") {
            metaMapperValue = "";
        }
        data.metaMapperStr = JSON.stringify({'META_NAME': metaMapperValue});
        data.mapperStr = JSON.stringify(mapping);
        data.mappingName = $("#mapping-name").val().trim();
        data.templatePath = $("#template-path").val().trim();
        data.importPath = $("#import-path").val().trim();
        data.importDAMPath = $("#import-dampath").val().trim();
        var projectSelect = $("#project-select").find("option:selected");
        data.projectId = projectSelect.val().trim();
        data.projectName = projectSelect.text().trim();
        var templateSelect = $("#gctemplate-select").find("option:selected");
        data.templateId = templateSelect.val().trim();
        data.templateName = templateSelect.text().trim();
        data.lastMapped = Date.now();

        data['sling:resourceType'] = 'gathercontent/components/content/mapping';
        return data;
    };

    $.fn.reloadMapping = function () {
        var path = $(".saveMapping").data("path");
        path = path.substring(0, path.lastIndexOf("/")) + ".ajax.html";
        var data = {};
        data.templatePath = $("#template-path").val().trim();
        data.projectId = $("#project-select").find("option:selected").val().trim();
        data.templateId = $("#gctemplate-select").find("option:selected").val().trim();

        var spinnerTarget = document.getElementById('mapper-container');
        var spinner = new Spinner().spin();
        $.ajax({
            url: path,
            data: data,
            type: "GET",
            cache: false,
            beforeSend: function () {
                $(spinnerTarget).addClass('grayout');
                spinnerTarget.appendChild(spinner.el);
            },
            success: function (data) {
                $("#mapper-container").html(data);
                $('#accordion').accordion();
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

    $.fn.updateSaveMappingButton = function () {
        var templatePath = $("#template-path");
        var mappingName = $("#mapping-name");
        var projectSelect = $("#project-select").find("option:selected");
        var templateSelect = $("#gctemplate-select").find("option:selected");
        if (templatePath.val() != "" && mappingName.val() != "" && projectSelect.val() != "" && templateSelect.val() != "") {
            $('.saveMapping')[0].disabled = false;
        } else {
            $('.saveMapping')[0].disabled = true;
        }
    };
}(jQuery));

$(function () {
    $("#project-select").change(function () {
        var projectid = $(this).find("option:selected").val().trim();
        var path = $(".saveMapping").data("path");
        //var options = CQ.Util.eval(CQ.HTTP.get(path + ".gctemplates.projectId-" + projectid + ".json"));
        var spinnerTarget = document.getElementById('mapper-container');
        var spinner = new Spinner().spin();
        $.ajax({
            url: path + ".gctemplates.projectId-" + projectid + ".json",
            type: "GET",
            cache: false,
            beforeSend: function () {
                $("#gctemplate-select").prop('disabled', true);
                $(spinnerTarget).addClass('grayout');
                spinnerTarget.appendChild(spinner.el);
            },
            success: function (data) {
                var json = JSON.parse(data);
                var optionsString = "<option value=''>Select Template</option>";
                if (json.gctemplates) {
                    json.gctemplates.forEach(function (item, index) {
                        optionsString += "<option value='" + item.value + "'>" + item.text + "</option>";
                    });
                }
                $("#gctemplate-select").html(optionsString);
                $().reloadMapping();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("error");
            },
            complete: function () {
                spinner.stop();
                $(spinnerTarget).removeClass('grayout');
                $("#gctemplate-select").prop('disabled', false);
            }
        });
    });

    $("#gctemplate-select").change(function () {
        $().reloadMapping();
    });

    $('div[id="mapper-container"]').on("change", '.mapper-select, select[id="meta-name-select"]', function () {
        var nameValue = $('#meta-name-select').val();
        var metaNameWInfo = $('#meta-name-information');
        metaNameWInfo.html('');
        if (nameValue != "Don't map") {
            $('.mapper-select').each(function (index, element) {
                if ($(element).val() == nameValue) {
                    metaNameWInfo.html("Мultiple GatherContent text fields are mapped to a single AEM property. " +
                        "GC text fields will be concatenated in target AEM property.");
                    return false;
                }
            });
        }
    });

    $(".saveMapping").click(function () {
        var data = $().fetchMappingData();
        var mapperStr = JSON.parse(data.mapperStr);
        var emptyFields = true;
        var invalidForm = false;
        $('form').each(function () {
            if (!($(this).valid())) {
                invalidForm = true;
            }
        });
        if (invalidForm) {
            window.scrollTo(0, 0);
            return false;
        }
        for (var property in mapperStr) {
            if (mapperStr.hasOwnProperty(property)) {
                if (mapperStr[property] != '') {
                    emptyFields = false;
                }
            }
        }
        if (emptyFields) {
            $("#empty-mapping-modal").dialog("open");
            return false;
        }
        var path = $(".saveMapping").data("path");

        $.ajax({
            url: path,
            data: data,
            type: "POST",
            success: function (responseData, textStatus, xhr) {
                if (xhr.status == 200) {
                    localStorage.setItem("createdUpdatedMapping", '"' + data.mappingName + '" updated');
                } else if (xhr.status == 201) {
                    localStorage.setItem("createdUpdatedMapping", '"' + data.mappingName + '" successfully created');
                }
                window.location.replace(path.substring(0, path.lastIndexOf("/jcr:content")) + ".mapping.html");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("error");
            }
        });
    });

    $("#empty-mapping-modal").dialog({
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
});

$('document').ready(function () {
    $('#accordion').accordion();
});
