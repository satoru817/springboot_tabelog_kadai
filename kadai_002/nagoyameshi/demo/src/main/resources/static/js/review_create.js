function previewFiles(event){
    const files = event.target.files;
    const preview = document.getElementById(`photoPreview`);
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

