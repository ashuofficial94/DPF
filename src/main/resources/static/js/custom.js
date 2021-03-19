let create_pipeline_form = document.getElementById('createPipelineForm');
window.onload = function(){
    console.log("test");
    hideAlerts();
}

create_pipeline_form.addEventListener('submit', async (e) => {
    document.getElementById("pipeline-Success").style.display = "none";
    document.getElementById("pipeline-Error").style.display = "none";
    if (create_pipeline_form.checkValidity() === true) {
        let response = await fetch('/getXML', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: document.getElementById('pipelineXML').value,

        });
        let result = await response.json();
        // let result = await response;
        console.log(result["status"]);

        if(result['status'] == "success"){
            document.getElementById("pipeline-Success").style.display = "block";
            document.getElementById("pipeline-Success-msg").innerText = result['msg'];

        } else if(result['status'] == "error"){
            document.getElementById("pipeline-Error").style.display = "block";
            document.getElementById("pipeline-Error-msg").innerText = result['msg'];
        }
    }

});

function hideAlerts(){
    document.getElementById("pipeline-Success").style.display = "none";
    document.getElementById("pipeline-Error").style.display = "none";
}