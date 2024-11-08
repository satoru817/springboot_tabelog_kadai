function previewFiles(event,reservationId){
    const files = event.target.files;
    const preview = document.getElementById(`photoPreview${reservationId}`);
    preview.innerHTML = "";

    Array.from(files).forEach(file=>{
        const reader = new FileReader();
        reader.onload = e =>{
            const img = document.createElement("img");
            img.src = e.target.result;
            img.classList.add("preview-img");
            preview.appendChild(img);
        };
        reader.readAsDataURL(file);
    });
}

//TODO:validationをつけたほうがいいかもしれない。
//todo:すでにレビューをしていた場合、そのデータがformに入るようにしないといけない。