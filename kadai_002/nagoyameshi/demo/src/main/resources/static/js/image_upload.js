document.addEventListener('DOMContentLoaded', function () {
  const dropZone = document.getElementById('dropZone');
  const imageUpload = document.getElementById('imageUpload');
  const imagePreview = document.getElementById('imagePreview');
  const fileCount = document.getElementById('fileCount');

  const MAX_FILES = 10;
  let currentFiles = [];

  dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('drag-over');
  });

  dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('drag-over');
  });

  dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('drag-over');
    const files = Array.from(e.dataTransfer.files).filter((file) => file.type.startsWith('image/'));
    handleFiles(files);
  });

  dropZone.addEventListener('click', () => {
    imageUpload.click();
  });

  imageUpload.addEventListener('change', (e) => {
    const files = Array.from(e.target.files).filter((file) => file.type.startsWith('image/'));//imageのみに絞る
    const validFiles = files.filter((file) => file.size <= 1 * 1024 * 1024);//1MB以下のファイルに絞る（application.propertiesの設定とここは合わせる必要がある。)
    handleFiles(validFiles);
  });

  function handleFiles(files) {
    const existingImage = document.querySelectorAll('.existingImage');//editの場合のみこれも考慮する必要がある。
    const remainingSlots = MAX_FILES - currentFiles.length - existingImage.length;
    const filesToAdd = files.slice(0, remainingSlots);

    if (files.length > remainingSlots) {
      alert(`最大${MAX_FILES}枚までしか保存できません`);
    }

    filesToAdd.forEach((file) => {
      currentFiles.push(file);

      const reader = new FileReader();

      reader.onload = function (e) {
        const previewContainer = document.createElement('div');
        previewContainer.className = 'preview-item';

        const img = document.createElement('img');
        img.src = e.target.result;
        img.className = 'preview-image';

        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'delete-button';
        deleteBtn.innerHTML = 'x';
        deleteBtn.style.backgroundColor = 'red';
        deleteBtn.onclick = function () {
          const index = currentFiles.indexOf(file);
          if (index > -1) {
            currentFiles.splice(index, 1);
            previewContainer.remove();
            updateFileCount();
            updateFormData();
          }
        };

        previewContainer.appendChild(img);
        previewContainer.appendChild(deleteBtn);
        imagePreview.appendChild(previewContainer);
      };
      reader.readAsDataURL(file);
    });
    updateFileCount();
    updateFormData();
  }

  function updateFileCount() {
    fileCount.textContent = currentFiles.length;
  }

  function updateFormData() {
    const dataTransfer = new DataTransfer();

    currentFiles.forEach((file) => {
      dataTransfer.items.add(file);
    });

    imageUpload.files = dataTransfer.files;
  }
});
