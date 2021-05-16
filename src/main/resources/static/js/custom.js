let stompClient = null;

let execute_link = document.querySelector('#execute-button');
let create_link = document.querySelector('#create-button');
let execute_panel = document.querySelector('#execute-panel');
let create_panel = document.querySelector('#create-panel');
let add_xml = document.querySelector('#add-pipeline-xml');
let add_stage = document.querySelector('#add-stage');
let delete_stage = document.querySelector('#delete-stage');
let pipeline_form = document.querySelector('#pipeline-form');

let displayed_row, pipeline_name;

let stageData = []
let curr_stage = 0;

let notification = (result, pipeline_name) => {
    let notification_header = document.getElementById('notification-header');
    let notification_body = document.getElementById('notification-body');
    let notification_color = document.getElementById('notification-color');

    notification_header.innerText = pipeline_name + ": " + result["status"];
    notification_body.innerText = result["msg"];

    if(result["status"] === "error") {
        notification_color.setAttribute("fill", "red");
        $('.toast').toast('show');
        return;
    }

    notification_color.setAttribute("fill", "green");
    $('.toast').toast('show');
}

execute_link.addEventListener('click', async (e) => {
    execute_panel.style.display = "block";
    create_panel.style.display = "none";

    let response = await fetch('/getpipelines', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        }
    })

    let pipeline_list = await response.json();
    let pipeline_table = document.getElementById("pipeline-list");
    pipeline_table.innerHTML = '';

    let top_row = document.createElement("tr");
    let top_col1 = document.createElement("td");
    let top_col2 = document.createElement("td");
    let strong1 = document.createElement("strong");
    let strong2 = document.createElement("strong");

    strong1.innerText = "Pipelines";
    strong2.innerText = "Operations";

    top_col1.appendChild(strong1);
    top_col2.appendChild(strong2);

    top_row.appendChild(top_col1);
    top_row.appendChild(top_col2);

    pipeline_table.appendChild(top_row);

    for(let id in pipeline_list) {
        let row = document.createElement("tr");

        let col1 = document.createElement("td");
        col1.innerText = pipeline_list[id][0];
        col1.style.verticalAlign = "middle";

        let col2 = document.createElement("td");
        let execute_button = document.createElement("button");
        execute_button.classList.add("btn");
        execute_button.classList.add("btn-primary");
        execute_button.innerText = "Execute";
        execute_button.style.width = "100px";

        let show_button = document.createElement("button");
        show_button.classList.add("btn", "btn-warning", "bi", "bi-eye");
        show_button.innerHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-eye\" viewBox=\"0 0 16 16\">\n" +
            "  <path d=\"M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z\"/>\n" +
            "  <path d=\"M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z\"/>\n" +
            "</svg>";

        show_button.setAttribute( "data-toggle", "modal");
        show_button.setAttribute("data-target", "#myModal");

        show_button.addEventListener('click', e => {
            document.getElementById("modal-heading").innerText = pipeline_list[id][0];
            document.getElementById("modal-content").innerText = pipeline_list[id][1];
        });

        execute_button.addEventListener('click', async(e) => {

            if(displayed_row) {
                displayed_row.style.display = "none";
                displayed_row.children[0].innerHTML = "";
            }

            execute_button.disabled = true;
            execute_button.innerText = "Executing";

            let execution_row = document.getElementById(pipeline_list[id][0]);

            let table = document.createElement("table");
            table.classList.add("table", "table-bordered");

            execution_row.children[0].appendChild(table);

            let thead = document.createElement("thead");
            let row1 = document.createElement("tr");

            let thead_content = ["Stage Number", "Stage Name", "Status"];
            for(let content of thead_content) {
                let col = document.createElement("td");
                col.innerHTML = "<strong>"+content+"</strong>";
                row1.appendChild(col);
            }
            table.appendChild(thead);
            thead.appendChild(row1);
            thead.classList.add("table-primary");

            execution_row.style.display = "table-row";
            displayed_row = execution_row;

            let execution_request = {
                pipeline_id: id,
                pipeline: pipeline_list[id][1],
                validate: 0
            }

            connect(table);
            stageData = [];
            let response = await fetch('/executePipeline', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json;charset=utf-8'
                },
                body: JSON.stringify(execution_request)
            })

            let result = await response.json();
            disconnect();

            notification(result, pipeline_list[id][0]);
            execute_button.disabled = false;
            execute_button.innerText = "Execute";
        });

        col2.appendChild(execute_button);
        col2.appendChild(show_button);

        row.appendChild(col1);
        row.appendChild(col2);

        let row2 = document.createElement("tr");
        row2.setAttribute("id", pipeline_list[id][0]);
        row2.style.display = "none";

        let col = document.createElement("td");
        col.setAttribute("colspan", "10");
        row2.appendChild(col);

        pipeline_table.appendChild(row);
        pipeline_table.appendChild(row2);
    }
});

create_link.addEventListener('click', () => {
    execute_panel.style.display = "none";
    create_panel.style.display = "block";
    let d = new Date();
    pipeline_name = "pipeline_" + d.getTime() + ".xml";
    document.getElementById('pipeline-name').innerText = pipeline_name;
});

delete_stage.addEventListener('click', () => {
    let stages = document.querySelector('#stages');
    if(stages.childElementCount > 0) {
        stages.removeChild(stages.children[stages.childElementCount-1]);
        curr_stage--;
    }
})

add_stage.addEventListener('click', () => {
    let stages = document.querySelector('#stages');
    curr_stage++;

    let stage = document.createElement('div');
    stage.classList.add("form-group");

    let stage_number = document.createElement('div');
    stage_number.classList.add('row');

    let stage_number_label = document.createElement("label");
    stage_number_label.setAttribute("for", "stage-number-"+curr_stage);
    stage_number_label.innerHTML = "<strong>Stage Number: </strong>"
    stage_number_label.classList.add("col-md-3")

    let stage_number_input = document.createElement("input");
    stage_number_input.setAttribute("type", "text");
    stage_number_input.setAttribute("id", "stage-number-"+curr_stage);
    stage_number_input.classList.add("form-control", "col-md-8");

    stage_number_input.value = curr_stage+"";
    stage_number_input.disabled = true;

    stage_number.appendChild(stage_number_label);
    stage_number.appendChild(stage_number_input);

    let stage_name = document.createElement('div');
    stage_name.classList.add('row');

    let stage_name_label = document.createElement('label');
    stage_name_label.setAttribute("for", "stage-name-"+curr_stage);
    stage_name_label.innerHTML = "<strong>Stage Name: </strong>"
    stage_name_label.classList.add("col-md-3")

    let stage_name_input = document.createElement("input");
    stage_name_input.setAttribute("type", "text");
    stage_name_input.setAttribute("id", "stage-name-"+curr_stage);
    stage_name_input.classList.add("form-control", "col-md-8");
    stage_name_input.required = true;

    stage_name.appendChild(stage_name_label);
    stage_name.appendChild(stage_name_input);

    let stage_description = document.createElement('div');
    stage_description.classList.add('row');

    let stage_description_label = document.createElement('label');
    stage_description_label.setAttribute("for", "stage-description-"+curr_stage);
    stage_description_label.innerHTML = "<strong>Stage Description: </strong>"
    stage_description_label.classList.add("col-md-3");

    let stage_description_input = document.createElement("input");
    stage_description_input.setAttribute("type", "text");
    stage_description_input.setAttribute("id", "stage-description-"+curr_stage);
    stage_description_input.classList.add("form-control", "col-md-8");

    stage_description.appendChild(stage_description_label);
    stage_description.appendChild(stage_description_input);

    let stage_executable = document.createElement('div');
    stage_executable.classList.add('row');

    let stage_executable_label = document.createElement('label');
    stage_executable_label.setAttribute("for", "stage-executable-"+curr_stage);
    stage_executable_label.innerHTML = "<strong>Executable: </strong>"
    stage_executable_label.classList.add("col-md-3");

    let stage_executable_input = document.createElement("input");
    stage_executable_input.setAttribute("type", "text");
    stage_executable_input.setAttribute("id", "stage-executable-"+curr_stage);
    stage_executable_input.classList.add("form-control", "col-md-8");
    stage_executable_input.required = true;

    stage_executable.appendChild(stage_executable_label);
    stage_executable.appendChild(stage_executable_input);

    stage.appendChild(stage_number);
    stage.appendChild(stage_name);
    stage.appendChild(stage_description);
    stage.appendChild(stage_executable);

    stage.appendChild(document.createElement("hr"));
    stages.appendChild(stage);
})

add_xml.addEventListener('click', async (e) => {

    add_xml.innerText = "Adding";
    add_xml.disabled = true;

    let pipeline_request = {
        name: pipeline_name,
        pipeline: document.getElementById('pipeline-xml').value
    }

    console.log(pipeline_request.pipeline);

    let validation_request = {
        pipeline_id: 0,
        pipeline: pipeline_request.pipeline,
        validate: 1
    }

    let message = await fetch('/executePipeline', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(validation_request)
    });

    let data = await message.json();
    if(data.status === "error") {
        document.querySelector('#close-add-pipeline').click();
        add_xml.innerText = "Add Pipeline";
        add_xml.disabled = false;
        notification(data, pipeline_request.name);
        return;
    }

    let response = await fetch('/savePipeline', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(pipeline_request)
    });

    let result = await response.json();
    execute_link.click();
    document.querySelector('#close-add-pipeline').click();
    add_xml.innerText = "Add Pipeline";
    add_xml.disabled = false;
    notification(result, pipeline_request.name);
});

pipeline_form.addEventListener('submit', async(e) => {
    e.preventDefault();
    e.stopPropagation();

    let db_url = document.querySelector('#db-url').value;
    let db_name = document.querySelector('#db-name').value;
    let db_user = document.querySelector('#db-user').value;
    let db_pass = document.querySelector('#db-pass').value;

    // <DBURL>localhost:3306</DBURL>
    // <DBName>employees</DBName>
    // <DBUserName>root</DBUserName>
    // <DBPassword>password</DBPassword>

    let feed_component = "<Feed>" +
        "<DBURL>" + db_url + "</DBURL>" +
        "<DBName>" + db_name + "</DBName>" +
        "<DBUserName>" + db_user + "</DBUserName>" +
        "<DBPassword>" + db_pass + "</DBPassword>" +
        "</Feed>"

    let stage_numbers = [];
    let stage_names = [];
    let stage_descriptions = [];
    let stage_executables = [];

    for(let index=1; index<=curr_stage; index++) {
        stage_numbers.push(index);
        stage_names.push(document.querySelector('#stage-name-'+index).value);
        stage_descriptions.push(document.querySelector('#stage-description-'+index).value);
        stage_executables.push(document.querySelector('#stage-executable-'+index).value);
    }

    let stage_components = "";

    for(let index in stage_numbers) {
        let stage_number = "'"+stage_numbers[index]+"'"
        stage_components +=
            "<stage number="+stage_number+">" +
                "<stageName>"+stage_names[index]+"</stageName>" +
                "<stageDesciption>"+stage_descriptions[index]+"</stageDesciption>" +
                "<sqlProcessing>"+stage_executables[index]+"</sqlProcessing>" +
                "<output>File</output>" +
            "</stage>"
    }

    let xml_header = "<?xml version='1.0' encoding='UTF-8'?>";
    let xml = xml_header +
        "<Pipelines xmlns='uri:pipelineSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "+
        "xsi:schemaLocation='uri:pipelineSchema pipeline.xsd'>" +
            "<Pipeline pipelineName='EmployeePipeline'>" +
                feed_component +
                "<Stages>"+stage_components+"</Stages>" +
            "</Pipeline>" +
        "</Pipelines>";

    xml = xml.replaceAll("><", ">\n<");

    let xml_request = {
        name: pipeline_name,
        pipeline: xml
    }

    let xml_validation_request = {
        pipeline_id: 0,
        pipeline: xml_request.pipeline,
        validate: 1
    }

    let message = await fetch('/executePipeline', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(xml_validation_request)
    });

    let data = await message.json();
    if(data.status === "error") {
        notification(data, xml_request.name);
        return;
    }

    let response = await fetch('/savePipeline', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(xml_request)
    });

    let result = await response.json();
    execute_link.click();
    notification(result, xml_request.name);
})

window.onload = () => {
    execute_link.click();
}

function connect(table) {
    let socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/result/stage', function (stageResultMessage) {
            let stage_info = JSON.parse(stageResultMessage.body);
            let row = document.createElement("tr");

            for(let property in stage_info) {
                let col = document.createElement("td");
                col.innerText = stage_info[property];
                row.appendChild(col);
                if(stage_info["status"] === "success") row.classList.add("table-success");
                else row.classList.add("table-danger");
            }

            table.appendChild(row);
        });
    });
}
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}