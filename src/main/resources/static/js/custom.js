let stompClient = null;

let create_pipeline_form = document.getElementById('createPipelineForm');

let execute_link = document.querySelector('#execute-button');
let create_link = document.querySelector('#create-button');
let execute_panel = document.querySelector('#execute-panel');
let create_panel = document.querySelector('#create-panel');
let displayed_row;

let stageData = []

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

        let col2 = document.createElement("td");
        let execute_button = document.createElement("button");
        execute_button.classList.add("btn");
        execute_button.classList.add("btn-primary");
        execute_button.innerText = "Execute";
        execute_button.style.width = "100px";
        execute_button.style.margin = "5px";
        execute_button.style.padding = "5px";

        let show_button = document.createElement("button");
        show_button.classList.add("btn", "btn-warning", "bi", "bi-eye");
        show_button.innerHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" fill=\"currentColor\" class=\"bi bi-eye\" viewBox=\"0 0 16 16\">\n" +
            "  <path d=\"M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z\"/>\n" +
            "  <path d=\"M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z\"/>\n" +
            "</svg>";

        show_button.style.margin = "5px";
        show_button.style.padding = "5px";
        show_button.setAttribute("data-toggle", "modal");
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
            table.style.borderRadius = "2px";

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

    document.getElementById('pipeline-name').value = "pipeline_"+d.getTime()+".xml";
});

create_pipeline_form.addEventListener('submit', async (e) => {
    e.preventDefault();
    e.stopPropagation();

    let pipeline_request = {
        name: document.querySelector("#pipeline-name").value,
        pipeline: document.getElementById('pipeline-xml').value
    }

    let validation_request = {
        pipeline_id: 0,
        pipeline: pipeline_request.pipeline,
        validate: 1
    }

    let response = await fetch('/executePipeline', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(validation_request)
    });

    let data = await response.json();
    console.log(data);

    if(data.status === "error") {
        notification(data, pipeline_request.name);
        return;
    }

    if (create_pipeline_form.checkValidity() === true) {
        document.getElementById('pipeline-xml').value = '';
        let response = await fetch('/savePipeline', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: JSON.stringify(pipeline_request)
        });
        let result = await response.json();
        notification(result, pipeline_request.name);
    }
});

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