/*
 * Axamit, gc.support@axamit.com
 */

$(function () {
    var isFull = false;

    function initAccordion() {
        $('#accordion').accordion({
            active: $('#active-tab').data('tabcount') - 1
        });
        cleanMoveToSelects();
        $("[aria-selected]").click(function () {
            cleanMoveToSelects();
        });
        $(".moveTo").change(function () {
            var from = $(this).data('from');
            var to = $(this).val();

            $("#moveTo-mapping-modal").data('from', from).data('to', to).dialog("open");
            $(this).val('');
        })
    }

    function fetchMappingData() {
        var data = {};
        var mapping = {};
        $(".mapper-element").each(function (index) {
            var id = $(this).data("id");
            var properties = {};
            var selectedValue;
            var selectedField = $(this).find('.mapper-select')[0];
            if (selectedField !== undefined && $(selectedField).val().trim() !== "Don't map") {
                selectedValue = $(selectedField).val().trim();
            } else {
                selectedValue = "";
            }
            properties.path = selectedValue;
            properties.plugin = $(this).find('.carousel-checkbox:checked').length > 0 ?
                'com.axamit.gc.core.services.plugins.CarouselPlugin' : '';
            mapping[id] = properties;
        });
        var metaMapperValue = $('#meta-name-select').val();
        if (metaMapperValue === "Don't map") {
            metaMapperValue = "";
        }
        data.metaMapperStr = JSON.stringify({'META_NAME': metaMapperValue});
        data.mapperStr = JSON.stringify(mapping);
        data.mappingName = $("#mapping-name").val().trim();
        data.templatePath = $("#template-path").val().trim();
        data.importPath = $("#import-path").val().trim();
        data.importDAMPath = $("#import-dampath").val().trim();
        //data.pluginConfigPath = $("#config-select").find("option:selected").val().trim();
        var projectSelect = $("#project-select").find("option:selected");
        data.projectId = projectSelect.val().trim();
        data.projectName = projectSelect.text().trim();
        var type = $("#gc-mapping-type-select").find("option:selected").val();
        var templateSelect = $("#gctemplate-select").find("option:selected");
        if (type === '' || type === '1') {
            type = '1';
        } else if (type === '2') {
            templateSelect = $("#gcentiesitem-select").find("option:selected");
        } else if (type === '3') {
            templateSelect = $("#gccustomitem-select").find("option:selected");
        }
        data.mappingTypeStr = type;
        data.templateId = templateSelect.val().trim();
        data.templateName = templateSelect.text().trim();
        data.lastMapped = Date.now();

        data['sling:resourceType'] = 'gathercontent/components/content/mapping';
        isFull = true;
        return data;
    }

    function reloadMapping() {
        isFull = true;
        var path = $(".saveMapping").data("path");
        path = path.substring(0, path.lastIndexOf("/")) + ".ajax.html";
        var data = {};
        data.templatePath = $("#template-path").val().trim();
        data.projectId = $("#project-select").find("option:selected").val().trim();
        var type = $("#gc-mapping-type-select").find("option:selected").val();
        if (type === '1') {
            data.templateId = $("#gctemplate-select").find("option:selected").val().trim();
        } else if (type === '2') {
            data.templateId = $("#gcentiesitem-select").find("option:selected").val().trim();
        } else if (type === '3') {
            data.templateId = $("#gccustomitem-select").find("option:selected").val().trim();
        }
        data.mappingType = type;
        //data.pluginConfigPath = $("#config-select").find("option:selected").val().trim();

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
                initAccordion();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log("error");
            },
            complete: function () {
                spinner.stop();
                $(spinnerTarget).removeClass('grayout');
            }
        });
    }

    function cleanMoveToSelects() {
        $("[aria-expanded='false']").children('select').hide();
        $("[aria-expanded='true']").children('select').show();
    }

    function updateSaveMappingButton() {
        var templatePath = $("#template-path");
        var mappingName = $("#mapping-name");
        var projectSelect = $("#project-select").find("option:selected");
        var templateSelect = $("#gctemplate-select").find("option:selected");
        var entiesItemSelect = $("#gcentiesitem-select").find("option:selected");
        var customItemSelect = $("#gccustomitem-select").find("option:selected");
        if (templatePath.val() !== "" && mappingName.val() !== "" && projectSelect.val() !== ""
            && (templateSelect.val() !== "" || entiesItemSelect.val() !== "" || customItemSelect.val() !== "")) {
            $('.saveMapping')[0].disabled = false;
            isFull = true;
        } else {
            $('.saveMapping')[0].disabled = true;
        }
    }

    function isValidPath(path) {
        var isValid = true;
        if (!GCStringUtils.isEmpty(path)) {
            if (path.startsWith('/')) {
                $.ajax({
                    url: path + '.valid.html',
                    async: false,
                    type: 'GET',
                    success: function (xhr) {
                        if (xhr.toString() !== "valid") {
                            isValid = false;
                        }
                    },
                    error: function () {
                        alert("ERROR");
                    }
                });
            } else {
                isValid = false;
            }
        }
        return isValid;
    }

    $(function () {
        // $(".moveTo").change(function () {
        //     var from = $(this).data('from');
        //     var to = $(this).val();
        //     $("#moveTo-mapping-modal").dialog("open").data('from', from)
        //         .data('to', to);
        //
        // });

        function resetMapping() {
            $("#mapper-container").html('');
        }

        function resetTemplates() {
            $('#gctemplate-select').html("<option value=''>Select Template</option>");
            $('#gctemplate-form').hide();
            $('#gcentiesitem-select').html("<option value=''>Select Entries Parent</option>");
            $('#gcentiesitem-form').hide();
            $('#gccustomitem-select').html("<option value=''>Select Custom Structure</option>");
            $('#gccustomitem-form').hide();
            resetMapping();
        }

        function resetMappingType() {
            $('#gc-mapping-type-select').val('');
            resetTemplates();
        }

        $("#project-select").change(function () {
            resetMappingType();
        });

        $("#gc-mapping-type-select").change(function () {
            var mappingType = $(this).find("option:selected").val().trim();
            resetTemplates();
            switch (mappingType) {
                case '1':
                    $('#gctemplate-form').show();
                    reloadTemplates(mappingType, '#gctemplate-select', "<option value=''>Select Template</option>");
                    break;
                case '2':
                    $('#gcentiesitem-form').show();
                    reloadTemplates(mappingType, '#gcentiesitem-select', "<option value=''>Select Entries Parent</option>");
                    break;
                case '3':
                    $('#gccustomitem-form').show();
                    reloadTemplates(mappingType, '#gccustomitem-select', "<option value=''>Select Custom Structure</option>");
                    break;
            }
        });

        function reloadTemplates(mappingType, targetSelect, optionsString) {
            var projectId = $("#project-select").find("option:selected").val().trim();
            var path = $(".saveMapping").data("path");
            var spinnerTarget = document.getElementById('mapper-container');
            var spinner = new Spinner().spin();
            $.ajax({
                url: path + ".gctemplates.mappingType-" + mappingType + ".projectId-" + projectId + ".json",
                type: "GET",
                cache: false,
                dataType:'json',
                beforeSend: function () {
                    $(targetSelect).prop('disabled', true);
                    $(spinnerTarget).addClass('grayout');
                    spinnerTarget.appendChild(spinner.el);
                },
                success: function (data) {
                    if (data.gctemplates) {
                        data.gctemplates.forEach(function (item, index) {
                            optionsString += "<option value='" + item.value + "'>" + item.text + "</option>";
                        });
                    }
                    $(targetSelect).html(optionsString);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log("error");
                },
                complete: function () {
                    spinner.stop();
                    $(spinnerTarget).removeClass('grayout');
                    $(targetSelect).prop('disabled', false);
                }
            });
        }


        $("#gctemplate-select, #gccustomitem-select, #gcentiesitem-select").change(function () {
            reloadMapping();
        });

        $('div[id="mapper-container"]').on("change", '.mapper-select, select[id="meta-name-select"]', function () {
            var nameValue = $('#meta-name-select').val();
            var metaNameWInfo = $('#meta-name-information');
            metaNameWInfo.html('');
            if (nameValue !== "Don't map") {
                $('.mapper-select').each(function (index, element) {
                    if ($(element).val() === nameValue) {
                        metaNameWInfo.html("Мultiple GatherContent text fields are mapped to a single AEM property. " +
                            "GC text fields will be concatenated in target AEM property.");
                        return false;
                    }
                });
            }
        });

        $(".saveMapping").click(function () {
            $(window).off('beforeunload');
            var data = fetchMappingData();

            if (!isValidPath(data.templatePath)) {
                $("#empty-template-path").dialog("open");
                return;
            }

            if (!isValidPath(data.importPath)) {
                $("#empty-import-path").dialog("open");
                return;
            }

            if (!isValidPath(data.importDAMPath)) {
                $("#empty-import-dam-path").dialog("open");
                return;
            }

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
                    if (mapperStr[property] !== '') {
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
                    if (xhr.status === 200) {
                        localStorage.setItem("createdUpdatedMapping", '"' + data.mappingName + '" updated');
                    } else if (xhr.status === 201) {
                        localStorage.setItem("createdUpdatedMapping", '"' + data.mappingName + '" successfully created');
                    }
                    window.location.replace(path.substring(0, path.lastIndexOf("/jcr:content")) + ".mapping.html");
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log("error");
                }
            });
        });

        $(".cancelEditOfMapping").click(function () {
            window.location = GCStringUtils.getMappingListUrl();
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
        $("#leave-mapping-modal").dialog({
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

        $("#empty-template-path").dialog({
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

        $("#empty-import-path").dialog({
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

        $("#empty-import-dam-path").dialog({
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

        function getLabelsAndValuesOfTab(allLabelsOfTab, allSelectsOfTab) {
            var data = [];
            for (i = 0; i < allLabelsOfTab.length; i++) {
                var label = allLabelsOfTab.eq(i).text();
                var value = allSelectsOfTab.eq(i).val();
                var isChecked = false;
                var checkbox = allSelectsOfTab.eq(i).parent().find('.carousel-checkbox')[0];
                if (checkbox) {
                    isChecked = $(checkbox).is(':checked');
                }
                data.push([label, value, isChecked]);
            }
            return data;
        }

        function cleanMoveToSelects() {
            $("[aria-expanded='false']").children('select').hide();
            $("[aria-expanded='true']").children('select').show();
        }


        $("#moveTo-mapping-modal").dialog({
            autoOpen: false,
            width: 450,
            resizable: false,
            modal: true,
            buttons: [{
                text: "Move",
                click: function () {
                    var first = $('select.mapper-select option:first-child');

                    //get all labels and selectors from FROM moving tab
                    var divOfTab = $('div[aria-hidden=false]');
                    var allSelectsOfTab = divOfTab.find('.mapper-select');
                    var allLabelsOfTab = divOfTab.find('h4');
                    var allCheckboxesOfTab = divOfTab.find('.carousel-checkbox');
                    var data = getLabelsAndValuesOfTab(allLabelsOfTab, allSelectsOfTab);
                    //clean FROM tab selects
                    allSelectsOfTab.val(first.val());
                    allCheckboxesOfTab.prop('checked', false);

                    //get all labels and selectors from "To" moving tab
                    var to = $("#moveTo-mapping-modal").data('to');
                    var newDiv = $("div[data-label='" + to + "']"); //.find('.mapper-select'); //.next().find('.mapper-select');
                    var allLabelsOfNewTab = newDiv.find('h4');
                    var allSelectsOfNewTab = newDiv.find('.mapper-select');

                    // not clean selects on TO moving tab


                    //moving values
                    for (i = 0; i < allLabelsOfNewTab.length; i++) {
                        var lab = allLabelsOfNewTab.eq(i).text();
                        console.log("finding " + lab);
                        for (j = 0; j < data.length; j++) {
                            if (lab !== "" && data[j][0] === lab) {
                                allSelectsOfNewTab.eq(i).val(data[j][1]);
                                var checkbox = allSelectsOfNewTab.eq(i).parent().find('.carousel-checkbox')[0];
                                $(checkbox).prop('checked', data[j][2]);
                            }
                        }
                    }

                    $("h3[data-label='" + to + "']").click();


                    $(this).dialog("close");
                }
            },
                {
                    text: "Cancel",
                    click: function () {
                        $(this).dialog("close");

                    }
                }],
            open: function (event, ui) {
                var from = $(this).data('from');
                var divOfTab = $('div[aria-hidden=false]');
                var allSelectsOfTab = divOfTab.find('.mapper-select');
                var allLabelsOfTab = divOfTab.find('h4');

                var data = getLabelsAndValuesOfTab(allLabelsOfTab, allSelectsOfTab);

                //get all labels and selectors from "To" moving tab
                var to = $(this).data('to');
                var newDiv = $("div[data-label='" + to + "']");
                var allLabelsOfNewTab = newDiv.find('h4');

                var mappedPropertiesNumber = 0;
                for (i = 0; i < allLabelsOfNewTab.length; i++) {
                    var lab = allLabelsOfNewTab.eq(i).text();
                    for (j = 0; j < data.length; j++) {
                        if (!GCStringUtils.isEmpty(lab) && data[j][0] === lab && data[j][1] !== "Don't map") {
                            mappedPropertiesNumber++;
                        }
                    }
                }

                var unmappedPropertiesNumber = 0;
                for (i = 0; i < data.length; i++) {
                    if (data[i][1] !== "Don't map") {
                        unmappedPropertiesNumber++;
                    }
                }

                $(this).html(mappedPropertiesNumber + ' fields in the tab "' + to + '" will be mapped.</br>' +
                    unmappedPropertiesNumber + ' fields in the tab "' + from + '" will be unmapped.');
                $(this).css('overflow', 'hidden');
            }
        });
        $('a').click(function (event) {
            if (isFull) {
                event.preventDefault();
                $("#leave-mapping-modal").data('link', this.href).dialog("open");
            }
            else {
                $(window).off('beforeunload');
            }
        });
        $(window).on('beforeunload', function () {
            return 'Any unsaved changes will be lost.';
        });

        $('document').ready(function () {
            initAccordion();
            if (GATHERCONTENT.checkIfPreviousURLis(GCStringUtils.getExportURL())) {
                $('#cancel-edit').hide();
            }
            updateSaveMappingButton();
            $(".mapping").on("change", ".required", function () {
                updateSaveMappingButton();
            });
            $.validator.addClassRules({
                required: {
                    required: true
                }
            });
        });


    });
//$("[aria-expanded='true']").children().show();

    CQ.Ext.onReady(function () {
        new CQ.Ext.form.FormPanel({
            id: "mapping-form-panel",
            renderTo: document.getElementById("CQ-MAPPING-FORM-WRAPPER"),
            width: 700,
            border: false,
            items: [
                {
                    cls: "required",
                    id: "mapping-name",
                    fieldDescription: "Add a mapping name. This will be used to distinguish mappings on import dialogs.",
                    fieldLabel: "Mapping Name (*)",
                    xtype: "textfield",
                    width: "184",
                    value: $('#mapper-mapping-name').data('value'),
                    "listeners": {
                        "change": function () {
                            isFull = $("#mapping-name").val() !== '';
                        }
                    }
                },
                {
                    cls: "required path",
                    id: "template-path",
                    fieldDescription: "Select template path. This will be used as a base model for your field mappings.",
                    fieldLabel: "AEM Template Page (*)",
                    rootPath: "/content",
                    xtype: "pathfield",
                    value: $('#mapper-template-path').data('value'),
                    "listeners": {
                        "dialogselect": function (pathField, path) {
                            GATHERCONTENT.pathFieldEmptyDialogSelectCheck(pathField, path);
                            reloadMapping();
                            updateSaveMappingButton();
                        },
                        "change": function () {
                            reloadMapping();
                            updateSaveMappingButton();
                        }
                    }
                },
                {
                    cls: "path",
                    id: "import-path",
                    fieldDescription: "Default Location allows you to specify the node in AEM where imported items will be stored.</br>Default path /content/gathercontent/ will be used if not specified.",
                    fieldLabel: "Default Location",
                    rootPath: "/content",
                    xtype: "pathfield",
                    value: $('#mapper-import-path').data('value'),
                    "listeners": {
                        "dialogselect": function (pathField, path) {
                            GATHERCONTENT.pathFieldEmptyDialogSelectCheck(pathField, path);
                        }
                    }
                },
                {
                    cls: "path",
                    id: "import-dampath",
                    fieldDescription: "Import DAM Path allows you to specify the default location (node) in AEM where imported assets (attachments) will be stored.</br>Default path /content/dam/gathercontent will be used if not specified.",
                    fieldLabel: "Import DAM Path",
                    rootPath: "/content/dam",
                    xtype: "pathfield",
                    value: $('#mapper-importDAM-path').data('value'),
                    "listeners": {
                        "dialogselect": function (pathField, path) {
                            GATHERCONTENT.pathFieldEmptyDialogSelectCheck(pathField, path);
                        }
                    }
                }
            ]
        });
    });

});


