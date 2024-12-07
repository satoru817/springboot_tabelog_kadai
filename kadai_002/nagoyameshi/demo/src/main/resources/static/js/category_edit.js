// 新規作成モーダルを開く関数
function openCreateModal() {
  $('#createModal').modal('show');
}

// 編集モーダルを開く関数
function openEditModal(element) {
  const categoryId = element.getAttribute('data-id');
  const categoryName = element.getAttribute('data-name');
  $('#editCategoryId').val(categoryId);
  $('#editCategoryName').val(categoryName);
  $('#editModal').modal('show');
}

// 削除モーダルを開く関数
function openDeleteModal(element) {
  const categoryId = element.getAttribute('data-id');
  const categoryName = element.getAttribute('data-name');
  $('#deleteCategoryId').val(categoryId);
  $('#deleteCategoryName').text(categoryName);
  $('#deleteModal').modal('show');
}

//// 右クリックで新規作成モーダルを表示 (イベントの修正)
//$(document).on('contextmenu', '#createNewCategory', function(e) {
//    e.preventDefault();
//    $('#createModal').modal('show');
//});

const categoryBtns = document.getElementsByClassName('category-group');

for (const categoryBtn of categoryBtns) {
  categoryBtn.addEventListener('click', function (e) {
    const hiddenBtns = categoryBtn.querySelectorAll('.hidden-btn');
    for (const hiddenBtn of hiddenBtns) {
      hiddenBtn.classList.toggle('show');
    }
  });
}
