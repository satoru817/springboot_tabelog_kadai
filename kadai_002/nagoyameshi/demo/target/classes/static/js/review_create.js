console.log("review_create.js is loaded");

const selectedFiles = [];//選択したファイルを保持する配列

let selectedPhotoIndex = -1;//削除対象の写真のindex

function handleFiles(event){
    const files = Array.from(event.target.files);
    const maxFiles = 10;
    const shownFiles = document.getElementsByClassName('existing-img');

    //１つのレビューあたりのファイル数が10を超えないように調整する
    if(selectedFiles.length + files.length + shownFiles.length > maxFiles){
        alert('You are allowed to upload up to 10 images');
        return;
    }

    files.forEach(file=>{
        //新しいfileを追加
        if(!selectedFiles.includes(file)){
            selectedFiles.push(file);
            preview(file);
        }
    });

    event.target.value = "";//ここはemptyにしかセットできない仕様。
}

//フォーム送信時にファイルを追加
document.getElementById("uploadForm").addEventListener("submit",function(event){
    console.log("eventListener is called");
    const form = event.target;

    //既存のfile inputを削除
    const existingInput = form.querySelector("input[name='images']");
    existingInput.remove();

    //selectedFilesの内容を新しいfile input としてフォームに追加
    selectedFiles.forEach(file=>{
        const fileInput = document.createElement('input');
        fileInput.type='file';
        fileInput.name="images";
        fileInput.files = createFileList(file);
        fileInput.style.display = 'none';
        form.appendChild(fileInput);
    });

});

//FileListオブジェクトを生成する関数
function createFileList(file){
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(file);
    return dataTransfer.files;
}

//プレビューするための関数
function preview(file){
    const reader = new FileReader();
    reader.onload = (e) => {
        const imgContainer = document.createElement("div");
        imgContainer.className = "position-relative";

        const img = document.createElement("img");
        img.src=e.target.result;

        img.classList.add('preview-img');
        img.classList.add('delete_target');

        img.addEventListener('click',()=>{
            //クリックされた画像のindexを保存
            selectedPhotoIndex = Array.from(document.querySelectorAll('.delete_target')).indexOf(img);

            //モーダル内の画像プレビューを更新
            const deleteTargetPreview = document.getElementById('deleteTargetPreview');
            deleteTargetPreview.src = img.src;

            console.log(`selectedPhotoIndex from click event ${selectedPhotoIndex}`)

            //モーダルを表示
            const deleteModal = new bootstrap.Modal(document.getElementById('deletePhotoModal'));
            deleteModal.show();
        });

        imgContainer.appendChild(img);
        document.getElementById("photoPreview").appendChild(imgContainer);
    };
    reader.readAsDataURL(file);
}

function deleteSelectedPhoto(){
    if(selectedPhotoIndex !== -1){
        console.log(`selectedPhotoIndex from deleteSelectedPhoto ${selectedPhotoIndex}`);
        selectedFiles.splice(selectedPhotoIndex,1);

        const previewContainer = document.getElementById("photoPreview");
        const previewImages = previewContainer.querySelectorAll('.position-relative');
        previewImages[selectedPhotoIndex].remove();

        const modal = bootstrap.Modal.getInstance(document.getElementById('deletePhotoModal'));
        modal.hide();

        selectedPhotoIndex = -1;

    }
}



