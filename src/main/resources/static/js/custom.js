let create_pipeline_form = document.getElementById('createPipelineForm');

let execute_link = document.querySelector('#execute-button');
let create_link = document.querySelector('#create-button');
let execute_panel = document.querySelector('#execute-panel');
let create_panel = document.querySelector('#create-panel');
let pipeline_name = document.querySelector("#pipeline-name");

execute_link.addEventListener('click', () => {
    execute_panel.style.display = "block";
    create_panel.style.display = "none";
});

create_link.addEventListener('click', () => {
    execute_panel.style.display = "none";
    create_panel.style.display = "block";

    let d = new Date();

    pipeline_name.value = "pipeline_"+d.getTime()+".xml";
});

// window.onload = () => {
//     hideAlerts();
// }
//
create_pipeline_form.addEventListener('submit', async (e) => {
    // document.getElementById("pipeline-Success").style.display = "none";
    // document.getElementById("pipeline-Error").style.display = "none";
    // $("body").addClass("loading");
    e.preventDefault();
    e.stopPropagation();

    let pipeline_request = {
        name: pipeline_name.value,
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
        // let result = await response;
        // console.log(result["status"]);
        // $("body").removeClass("loading");
        // if(result['status'] == "success"){
        //     document.getElementById("pipeline-Success").style.display = "block";
        //     document.getElementById("pipeline-Success-msg").innerText = result['msg'];
        //
        // } else if(result['status'] == "error"){
        //     document.getElementById("pipeline-Error").style.display = "block";
        //     document.getElementById("pipeline-Error-msg").innerText = result['msg'];
        // }
        console.log(result);
    }
});
//
// function hideAlerts(){
//     document.getElementById("pipeline-Success").style.display = "none";
//     document.getElementById("pipeline-Error").style.display = "none";
// }