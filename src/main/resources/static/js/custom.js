let create_pipeline_form = document.getElementById('createPipelineForm');

let execute_link = document.querySelector('#execute-button');
let create_link = document.querySelector('#create-button');
let execute_panel = document.querySelector('#execute-panel');
let create_panel = document.querySelector('#create-panel');

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

    // <tr>
    //     <td><strong>Pipeline</strong></td>
    //     <td><strong>Operations</strong></td>
    // </tr>

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
        col1.innerText = pipeline_list[id];

        let col2 = document.createElement("td");
        let button = document.createElement("button");
        button.classList.add("btn");
        button.classList.add("btn-primary");
        button.innerText = "Execute";

        button.addEventListener('click', async(e) => {

            let response = await fetch('/executePipeline', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json;charset=utf-8'
                },
                body: id
            })

            let result = await response.json();

            let notification_header = document.getElementById('notification-header');
            let notification_body = document.getElementById('notification-body');
            let notification_color = document.getElementById('notification-color');

            notification_header.innerText = pipeline_list[id] + ": " + result["status"];
            notification_body.innerText = result["msg"];

            if(result["status"] === "error")
                notification_color.setAttribute("fill", "red");

            else
                notification_color.setAttribute("fill", "green");

            $('.toast').toast('show');
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

window.onload = () => {
    execute_link.click();
}
//
create_pipeline_form.addEventListener('submit', async (e) => {
    // document.getElementById("pipeline-Success").style.display = "none";
    // document.getElementById("pipeline-Error").style.display = "none";
    // $("body").addClass("loading");
    e.preventDefault();
    e.stopPropagation();

    let pipeline_request = {
        name: document.querySelector("#pipeline-name").value,
        pipeline: document.getElementById('pipeline-xml').value
    }

    if (create_pipeline_form.checkValidity() === true) {
        let response = await fetch('/savePipeline', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: JSON.stringify(pipeline_request)
        });
        let result = await response.json();

        let notification_header = document.getElementById('notification-header');
        let notification_body = document.getElementById('notification-body');

        notification_header.innerText = result["status"];
        notification_body.innerText = result["msg"];

        execute_link.click();
        $('.toast').toast('show');
    }
});
//
// function hideAlerts(){
//     document.getElementById("pipeline-Success").style.display = "none";
//     document.getElementById("pipeline-Error").style.display = "none";
// }