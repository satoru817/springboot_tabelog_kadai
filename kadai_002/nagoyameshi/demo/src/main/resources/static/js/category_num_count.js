const categoryCheckBoxes = document.querySelectorAll('.category-checkbox');

//カテゴリの選択個数を5個までに抑える
categoryCheckBoxes.forEach((box) => {
  box.addEventListener('change', function () {
    const checkedBox = document.querySelectorAll('.category-checkbox:checked');
    if (checkedBox.length > 5) {
      this.checked = false;
      alert('5つまでしか選択できません');
    }
  });
});
