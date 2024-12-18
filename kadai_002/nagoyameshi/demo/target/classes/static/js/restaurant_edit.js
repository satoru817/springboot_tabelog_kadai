$('.carousel').slick({
  autoplay: true,
  dots: true,
  infinite: true,
  autoplaySpeed: 5000,
  arrows: true,
  centerMode: true,
  variableWidth: true,
  appendArrows: $('.arrow_box'),
  prevArrow: '<div class="slide-arrow prev-arrow"></div>',
  nextArrow: '<div class="slide-arrow next-arrow"></div>',
});

function deleteImage(restaurantImageId) {
  const deleteImage = document.getElementById(`restaurantImage${restaurantImageId}`);
  const restaurantId = document.getElementById('restaurantId').value;
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

  if (confirm('この画像を削除してもよろしいですか？')) {
    fetch(`../delete-image?restaurantImageId=${restaurantImageId}`, {
      method: 'DELETE',
      headers: {
        [csrfHeader]: csrfToken,
      },
    })
      .then((response) => {
        if (response.ok) {
          deleteImage.remove();
        } else {
          alert('画像の削除に失敗しました。');
        }
      })
      .catch((error) => console.error('エラー:', error));
  }
}
//
//const imagePreview = document.getElementById('imagePreview');
//const fileCountDisplay = document.getElementById('fileCountDisplay');
//const imageInput = document.getElementById('images');
//
//imageInput.addEventListener('change', function (event) {
//  const files = Array.from(this.files);
//  const validFiles = files.filter((file) => file.size <= 1 * 1024 * 1024);
//  const existingImagesCount = document.getElementsByClassName('existingImage').length;
//
//  if (validFiles.length + existingImagesCount > 10) {
//    alert('登録できる画像は合計10枚です');
//    imageInput.value = ''; //リセット
//    fileCountDisplay.textContent = 'アップロードされるファイル数:0';
//    return;
//  }
//
//  imagePreview.innerHTML = '';
//  fileCountDisplay.textContent = `アップロードされるファイル数: ${validFiles.length}`;
//  validFiles.forEach((file) => {
//    const reader = new FileReader();
//    reader.onload = function (e) {
//      const imgElement = document.createElement('img');
//      imgElement.src = e.target.result;
//      imagePreview.appendChild(imgElement);
//    };
//    reader.readAsDataURL(file);
//  });
//});
