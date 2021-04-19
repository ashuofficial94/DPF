let create_pipeline_form = document.getElementById('createPipelineForm');

let execute_link = document.querySelector('#execute-button');
let create_link = document.querySelector('#create-button');
let execute_panel = document.querySelector('#execute-panel');
let create_panel = document.querySelector('#create-panel');

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
    execute_link.click();
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
        let button = document.createElement("button");
        button.classList.add("btn");
        button.classList.add("btn-primary");
        button.innerText = "Execute";
        button.style.width = "100px";

        button.addEventListener('click', async(e) => {

            button.disabled = true;
            button.innerText = "Executing";

            let execution_request = {
                pipeline_id: id,
                pipeline: pipeline_list[id][1],
                validate: 0
            }

            let response = await fetch('/executePipeline', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json;charset=utf-8'
                },
                body: JSON.stringify(execution_request)
            })

            let result = await response.json();
            notification(result, pipeline_list[id][0]);
        });

        col2.appendChild(button);

        row.appendChild(col1);
        row.appendChild(col2);
        pipeline_table.appendChild(row);
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
