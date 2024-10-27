const imageUpload = document.getElementById('imageUpload');
const imagePreview = document.getElementById('imagePreview');
const fileCountDisplay = document.getElementById('fileCountDisplay'); // ファイル数表示用の要素を取得

imageUpload.addEventListener('change', function() {
    const files = Array.from(this.files);
    const validFiles = files.filter(file => file.size <= 1 * 1024 * 1024); // 1MB以下のファイルをフィルタリング

    // 10枚を超えないようにする
    if (validFiles.length > 10) {
        alert("アップロード上限枚数は10です");
        imageUpload.value = ""; // リセットする
        fileCountDisplay.textContent = "アップロードされるファイル数: 0"; // ファイル数表示をリセット
        return;
    }

    // プレビューをクリアする
    imagePreview.innerHTML = "";
    fileCountDisplay.textContent = `アップロードされるファイル数: ${validFiles.length}`; // 有効なファイル数を表示

    // 各画像を表示
    validFiles.forEach(file => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const imgElement = document.createElement("img");
            imgElement.src = e.target.result;
            imagePreview.appendChild(imgElement);
        }
        reader.readAsDataURL(file);
    });
});
